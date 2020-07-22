package com.atom.wyz.worldwind.ogc

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.TileMatrixSet
import com.atom.wyz.worldwind.ogc.gml.GmlAbstractGeometry
import com.atom.wyz.worldwind.ogc.gml.GmlRectifiedGrid
import com.atom.wyz.worldwind.ogc.ows.OwsExceptionReport
import com.atom.wyz.worldwind.ogc.ows.OwsXmlParser
import com.atom.wyz.worldwind.ogc.wcs.Wcs201CoverageDescription
import com.atom.wyz.worldwind.ogc.wcs.Wcs201CoverageDescriptions
import com.atom.wyz.worldwind.ogc.wcs.WcsXmlParser
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL

class Wcs201ElevationCoverage : TiledElevationCoverage {

    protected var handler = Handler(Looper.getMainLooper())


    constructor(
        sector: Sector,
        numLevels: Int,
        serviceAddress: String,
        coverage: String
    ) {
        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "invalidNumLevels")
            )
        }
        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "constructor",
                    "The number of levels must be greater than 0"
                )
            )
        }
        val matrixWidth = if (sector.isFullSphere()) 2 else 1
        val matrixHeight = 1
        val tileWidth = 256
        val tileHeight = 256
        this.tileMatrixSet = (
            TileMatrixSet.fromTilePyramid(
                sector,
                matrixWidth,
                matrixHeight,
                tileWidth,
                tileHeight,
                numLevels
            )
        )
        this.tileFactory = (Wcs201TileFactory(serviceAddress, coverage))
    }


    /**
     * Attempts to construct a Web Coverage Service (WCS) elevation coverage with the provided service address and
     * coverage id. This constructor initiates an asynchronous request for the DescribeCoverage document and then uses
     * the information provided to determine a suitable Sector and level count. If the coverage id doesn't match the
     * available coverages or there is another error, no data will be provided and the error will be logged.
     *
     * @param serviceAddress the WCS service address
     * @param coverage       the WCS coverage name
     *
     * @throws IllegalArgumentException If any argument is null
     */
    constructor(serviceAddress: String, coverage: String) {
        // Fetch the DescribeCoverage document and determine the bounding box and number of levels
        val finalServiceAddress: String = serviceAddress
        val finalCoverageId: String = coverage
        WorldWind.taskService.execute(Runnable {
            try {
                initAsync(finalServiceAddress, finalCoverageId)
            } catch (logged: Throwable) {
                Logger.logMessage(
                    Logger.ERROR,
                    "Wcs201ElevationCoverage",
                    "constructor",
                    "Exception initializing WCS coverage serviceAddress:$finalServiceAddress coverage:$finalCoverageId",
                    logged
                )
            }
        })
    }

    @Throws(Exception::class)
    protected fun initAsync(serviceAddress: String, coverage: String) {
        val coverageDescriptions: Wcs201CoverageDescriptions? = describeCoverage(serviceAddress, coverage)
        val coverageDescription: Wcs201CoverageDescription =
            coverageDescriptions?.getCoverageDescription(coverage)
                ?: throw Exception(
                    Logger.makeMessage(
                        "Wcs201ElevationCoverage",
                        "initAsync",
                        "WCS coverage is undefined: $coverage"
                    )
                )
        val factory: TileFactory = Wcs201TileFactory(serviceAddress!!, coverage)
        val matrixSet =
            tileMatrixSetFromCoverageDescription(coverageDescription)
        handler.post {
            tileFactory = (factory)
            tileMatrixSet = (matrixSet)
            WorldWind.requestRedraw()
        }
    }

    @Throws(Exception::class)
    protected fun tileMatrixSetFromCoverageDescription(coverageDescription: Wcs201CoverageDescription): TileMatrixSet {
        val srsName = coverageDescription.boundedBy?.envelope?.srsName
        if (srsName == null || !srsName.contains("4326")) {
            throw Exception(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "tileMatrixSetFromCoverageDescription",
                    "WCS Envelope SRS is incompatible: $srsName"
                )
            )
        }
        val lowerCorner: DoubleArray? =
            coverageDescription.boundedBy?.envelope?.lowerCorner?.values
        val upperCorner: DoubleArray? =
            coverageDescription.boundedBy?.envelope?.upperCorner?.values
        if (lowerCorner == null || upperCorner == null || lowerCorner.size != 2 || upperCorner.size != 2) {
            throw Exception(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "tileMatrixSetFromCoverageDescription",
                    "WCS Envelope is invalid"
                )
            )
        }

        // Determine the number of data points in the i and j directions
        val geometry: GmlAbstractGeometry? = coverageDescription.domainSet?.geometry
        if (geometry !is GmlRectifiedGrid) {
            throw Exception(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "tileMatrixSetFromCoverageDescription",
                    "WCS domainSet Geometry is incompatible:$geometry"
                )
            )
        }
        val grid: GmlRectifiedGrid = geometry as GmlRectifiedGrid
        val gridLow: IntArray? = grid.limits?.gridEnvelope?.low?.values
        val gridHigh: IntArray? = grid.limits?.gridEnvelope?.high?.values
        if (gridLow == null || gridHigh == null || gridLow.size != 2 || gridHigh.size != 2) {
            throw Exception(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "tileMatrixSetFromCoverageDescription",
                    "WCS GridEnvelope is invalid"
                )
            )
        }
        val boundingSector = Sector.fromDegrees(
            lowerCorner[0],
            lowerCorner[1],
            upperCorner[0] - lowerCorner[0],
            upperCorner[1] - lowerCorner[1]
        )
        val tileWidth = 256
        val tileHeight = 256
        val gridHeight = gridHigh[1] - gridLow[1]
        val level =
            Math.log(gridHeight / tileHeight.toDouble()) / Math.log(2.0) // fractional level address
        var levelNumber = Math.ceil(level).toInt() // ceiling captures the resolution
        if (levelNumber < 0) {
            levelNumber = 0 // need at least one level, even if it exceeds the desired resolution
        }
        val numLevels = levelNumber + 1 // convert level number to level count
        return TileMatrixSet.fromTilePyramid(
            boundingSector,
            if (boundingSector.isFullSphere()) 2 else 1,
            1,
            tileWidth,
            tileHeight,
            numLevels
        )
    }

    @Throws(Exception::class)
    protected fun describeCoverage(
        serviceAddress: String,
        coverageId: String
    ): Wcs201CoverageDescriptions? {
        var inputStream: InputStream? = null
        var responseXml: Any? = null
        try {
            // Build the appropriate request Uri given the provided service address
            val serviceUri = Uri.parse(serviceAddress).buildUpon()
                .appendQueryParameter("VERSION", "2.0.1")
                .appendQueryParameter("SERVICE", "WCS")
                .appendQueryParameter("REQUEST", "DescribeCoverage")
                .appendQueryParameter("COVERAGEID", coverageId)
                .build()

            // Open the connection as an input stream
            val conn = URL(serviceUri.toString()).openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000

            // Throw an exception when the service responded with an error
            val exceptionReport = OwsXmlParser.parseErrorStream(conn)
            if (exceptionReport != null) {
                throw OgcException(exceptionReport)
            }

            // Parse and read the input stream
            inputStream = BufferedInputStream(conn.getInputStream())
            responseXml = WcsXmlParser.parse(inputStream)
            if (responseXml is OwsExceptionReport) {
                throw OgcException(responseXml as OwsExceptionReport?)
            } else if (responseXml !is Wcs201CoverageDescriptions) {
                throw Exception(
                    Logger.makeMessage(
                        "Wcs201ElevationCoverage",
                        "describeCoverage",
                        "Response is not a WCS DescribeCoverage document"
                    )
                )
            }
        } finally {
            WWUtil.closeSilently(inputStream)
        }
        return responseXml as Wcs201CoverageDescriptions?
    }
}