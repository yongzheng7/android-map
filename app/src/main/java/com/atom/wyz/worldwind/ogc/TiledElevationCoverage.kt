package com.atom.wyz.worldwind.ogc

import android.os.Handler
import android.os.Looper
import android.util.LongSparseArray
import android.util.SparseIntArray
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.TileMatrix
import com.atom.wyz.worldwind.geom.TileMatrixSet
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.LruMemoryCache
import com.atom.wyz.worldwind.util.Retriever
import com.atom.wyz.worldwind.util.WWMath
import java.net.SocketTimeoutException
import java.nio.ShortBuffer
import java.util.*

open abstract class TiledElevationCoverage : AbstractElevationCoverage,
    Retriever.Callback<ImageSource, Void, ShortBuffer> {

    interface TileFactory {
        fun createTileSource(tileMatrix: TileMatrix, row: Int, column: Int): ImageSource
    }

    companion object {
        fun tileKey(tileMatrix: TileMatrix, row: Int, column: Int): Long {
            val lord = (tileMatrix.ordinal.toLong() and 0xFFL) // 8 bits
            val lrow = (row.toLong() and 0xFFFFFFFL)// 28 bits
            val lcol = (column.toLong() and 0xFFFFFFFL) // 28 bits
            val key = lord shl 56 or (lrow shl 28) or lcol
            return key
        }

        protected val GET_HEIGHT_LIMIT_SAMPLES = 8
    }

    var tileMatrixSet: TileMatrixSet = TileMatrixSet() // empty tile matrix set
        set(value) {
            field = value
            this.invalidateTiles()
        }

    var coverageSource: LruMemoryCache<Long, ImageSource>

    var coverageCache: LruMemoryCache<ImageSource, ShortArray>

    var coverageHandler: Handler

    var enableRetrieval = false

    var coverageRetriever: ElevationRetriever

    var tileFactory: TileFactory? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }

    open fun invalidateTiles() {
        coverageSource.clear()
        coverageCache.clear()
        updateTimestamp()
    }


    constructor() {
        coverageSource = LruMemoryCache(200)
        coverageRetriever = ElevationRetriever(4)
        coverageCache = LruMemoryCache(1024 * 1024 * 8)
        coverageHandler = Handler(Looper.getMainLooper(), Handler.Callback { false })
        Logger.log(
            Logger.DEBUG, java.lang.String.format(
                Locale.US, "Coverage cache initialized  %,.0f KB",
                coverageCache.capacity / 1024.0
            )
        )
    }

    override fun doGetHeight(latitude: Double, longitude: Double, result: FloatArray) {
        if (tileMatrixSet.sector.contains(latitude, longitude)) {
            return  // no coverage in the specified location
        }
    }

    override fun doGetHeightGrid(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        result: FloatArray
    ) {
        if (!tileMatrixSet.sector.intersects(gridSector)) {
            return  // no coverage in the specified sector
        }
        val targetPixelSpan = gridSector.deltaLatitude() / gridHeight
        val targetIdx: Int = tileMatrixSet.indexOfMatrixNearest(targetPixelSpan)
        val tileBlock = TileBlock()
        for (idx in targetIdx downTo 0) {
            this.enableRetrieval =
                (idx == targetIdx || idx == 0) // enable retrieval of the target matrix and the first matrix
            val tileMatrix = tileMatrixSet.matrix(idx) ?: continue
            if (this.fetchTileBlock(gridSector, gridWidth, gridHeight, tileMatrix, tileBlock)) {
                readHeightGrid(gridSector, gridWidth, gridHeight, tileBlock, result)
                return
            }
        }
    }

    override fun doGetHeightLimits(
        sector: Sector,
        result: FloatArray
    ) {
        if (!tileMatrixSet.sector.intersects(sector)) {
            return  // no coverage in the specified sector
        }
        val targetPixelSpan =
            sector.deltaLatitude() / GET_HEIGHT_LIMIT_SAMPLES
        val targetIdx: Int = tileMatrixSet.indexOfMatrixNearest(targetPixelSpan)
        val tileBlock = TileBlock()

        for (idx in targetIdx downTo 0) {
            this.enableRetrieval =
                (idx == targetIdx || idx == 0) // enable retrieval of the target matrix and the first matrix
            val tileMatrix = tileMatrixSet.matrix(idx) ?: continue
            if (this.fetchTileBlock(sector, tileMatrix, tileBlock)) {
                scanHeightLimits(sector, tileBlock, result)
                return
            }
        }
    }

    override fun retrievalSucceeded(
        retriever: Retriever<ImageSource, Void, ShortBuffer>,
        key: ImageSource,
        options: Void?,
        value: ShortBuffer
    ) {
        val finalArray = ShortArray(value.remaining())
        value[finalArray]

        coverageHandler.post {
            coverageCache.put(key, finalArray, finalArray.size * 2)
            updateTimestamp()
            WorldWind.requestRedraw()
        }

        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Coverage retrieval succeeded \'$key\'")
        }
    }

    override fun retrievalFailed(
        retriever: Retriever<ImageSource, Void, ShortBuffer>,
        key: ImageSource,
        ex: Throwable?
    ) {
        if (ex is SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
            Logger.log(Logger.ERROR, "Coverage retrieval Socket timeout \'$key\'")
        } else if (ex != null) { // log checked exceptions with the entire stack trace
            Logger.log(
                Logger.ERROR,
                "Coverage retrieval failed with exception \'$key\'  ${ex.localizedMessage}",
                ex
            )
        } else {
            Logger.log(Logger.ERROR, "Coverage retrieval failed \'$key\'")
        }
    }

    override fun retrievalRejected(
        retriever: Retriever<ImageSource, Void, ShortBuffer>,
        key: ImageSource,
        msg: String
    ) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Coverage retrieval rejected \'$key\'")
        }
    }

    protected open fun fetchTileBlock(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        tileMatrix: TileMatrix,
        result: TileBlock
    ): Boolean {
        val tileWidth = tileMatrix.tileWidth
        val tileHeight = tileMatrix.tileHeight
        val rasterWidth = tileMatrix.matrixWidth * tileWidth
        val rasterHeight = tileMatrix.matrixHeight * tileHeight
        val matrixMinLat: Double = tileMatrix.sector.minLatitude
        val matrixMaxLat: Double = tileMatrix.sector.maxLatitude
        val matrixMinLon: Double = tileMatrix.sector.minLongitude
        val matrixMaxLon: Double = tileMatrix.sector.maxLongitude
        val matrixDeltaLat = tileMatrix.sector.deltaLatitude()
        val matrixDeltaLon = tileMatrix.sector.deltaLongitude()
        val sMin = 1.0 / (2.0 * rasterWidth)
        val sMax = 1.0 - sMin
        val tMin = 1.0 / (2.0 * rasterHeight)
        val tMax = 1.0 - tMin

        result.tileMatrix = tileMatrix
        result.clear()
        var lon: Double = gridSector.minLongitude
        val deltaLon = gridSector.deltaLongitude() / (gridWidth - 1)
        var uidx = 0
        while (uidx < gridWidth) {
            if (uidx == gridWidth - 1) {
                lon =
                    gridSector.maxLongitude // explicitly set the last lon to the max longitude to ensure alignment
            }

            if (lon in matrixMinLon..matrixMaxLon) {
                val s = (lon - matrixMinLon) / matrixDeltaLon
                var u: Double
                var i0: Int
                var i1: Int
                if (tileMatrix.sector.isFullSphere()) {
                    u = rasterWidth * WWMath.fract(s) // wrap the horizontal coordinate
                    i0 = WWMath.mod(Math.floor(u - 0.5).toInt(), rasterWidth)
                    i1 = WWMath.mod(i0 + 1, rasterWidth)
                } else {
                    u = rasterWidth * WWMath.clamp(s, sMin, sMax) // clamp the horizontal coordinate
                    i0 = WWMath.clamp(
                        Math.floor(u - 0.5),
                        0.0,
                        (rasterWidth - 1).toDouble()
                    ).toInt()
                    i1 = WWMath.clamp((i0 + 1).toDouble(), 0.0, (rasterWidth - 1).toDouble()).toInt()
                }
                val col0 = i0 / tileWidth
                val col1 = i1 / tileWidth
                result.cols.append(col0, 0)
                result.cols.append(col1, 0)
            }

            uidx++
            lon += deltaLon
        }

        var lat: Double = gridSector.minLatitude
        val deltaLat = gridSector.deltaLatitude() / (gridHeight - 1)
        var vidx = 0
        while (vidx < gridHeight) {
            if (vidx == gridHeight - 1) {
                lat =
                    gridSector.maxLatitude // explicitly set the last lat to the max latitude to ensure alignment
            }
            if (lat in matrixMinLat..matrixMaxLat) {
                val t = (matrixMaxLat - lat) / matrixDeltaLat
                val v = rasterHeight * WWMath.clamp(
                    t,
                    tMin,
                    tMax
                ) // clamp the vertical coordinate to the raster edge
                val j0 =
                    WWMath.clamp(Math.floor(v - 0.5), 0.0, (rasterHeight - 1).toDouble()).toInt()
                val j1 =
                    WWMath.clamp((j0 + 1).toDouble(), 0.0, (rasterHeight - 1).toDouble()).toInt()
                val row0 = j0 / tileHeight
                val row1 = j1 / tileHeight
                result.rows.append(row0, 0)
                result.rows.append(row1, 0)
            }
            vidx++
            lat += deltaLat
        }

        var ridx = 0
        val rlen = result.rows.size()
        while (ridx < rlen) {
            var cidx = 0
            val clen = result.cols.size()
            while (cidx < clen) {
                val row = result.rows.keyAt(ridx)
                val col = result.cols.keyAt(cidx)
                val tileArray = this.fetchTileArray(tileMatrix, row, col)
                if (tileArray != null) {
                    result.putTileArray(row, col, tileArray)
                } else {
                    return false
                }
                cidx++
            }
            ridx++
        }

        return true
    }


    protected open fun fetchTileBlock(
        sector: Sector,
        tileMatrix: TileMatrix,
        result: TileBlock
    ): Boolean {
        val tileWidth = tileMatrix.tileWidth
        val tileHeight = tileMatrix.tileHeight
        val rasterWidth = tileMatrix.matrixWidth * tileWidth
        val rasterHeight = tileMatrix.matrixHeight * tileHeight
        val matrixMaxLat: Double = tileMatrix.sector.maxLatitude
        val matrixMinLon: Double = tileMatrix.sector.minLongitude
        val matrixDeltaLat = tileMatrix.sector.deltaLatitude()
        val matrixDeltaLon = tileMatrix.sector.deltaLongitude()

        val intersection = Sector(tileMatrix.sector)
        intersection.intersect(sector)

        val sMin: Double = (intersection.minLongitude - matrixMinLon) / matrixDeltaLon
        val sMax: Double = (intersection.maxLongitude - matrixMinLon) / matrixDeltaLon
        val uMin = Math.floor(rasterWidth * sMin)
        val uMax = Math.ceil(rasterWidth * sMax)
        val iMin = WWMath.clamp(uMin, 0.0, (rasterWidth - 1).toDouble()).toInt()
        val iMax = WWMath.clamp(uMax, 0.0, (rasterWidth - 1).toDouble()).toInt()
        val colMin = iMin / tileWidth
        val colMax = iMax / tileWidth

        val tMin: Double = (matrixMaxLat - intersection.maxLatitude) / matrixDeltaLat
        val tMax: Double = (matrixMaxLat - intersection.minLatitude) / matrixDeltaLat
        val vMin = Math.floor(rasterHeight * tMin)
        val vMax = Math.ceil(rasterHeight * tMax)
        val jMin = WWMath.clamp(vMin, 0.0, (rasterHeight - 1).toDouble()).toInt()
        val jMax = WWMath.clamp(vMax, 0.0, (rasterHeight - 1).toDouble()).toInt()
        val rowMin = jMin / tileHeight
        val rowMax = jMax / tileHeight

        result.tileMatrix = tileMatrix
        result.clear()
        for (row in rowMin..rowMax) {
            for (col in colMin..colMax) {
                val tileArray = fetchTileArray(tileMatrix, row, col)
                if (tileArray != null) {
                    result.rows.put(row, 0)
                    result.cols.put(col, 0)
                    result.putTileArray(row, col, tileArray)
                } else {
                    return false
                }
            }
        }
        return true
    }

    protected open fun fetchTileArray(tileMatrix: TileMatrix, row: Int, column: Int): ShortArray? {
        val key = tileKey(tileMatrix, row, column)
        var tileSource = coverageSource.get(key)
        if (tileSource == null) {
            tileSource = tileFactory!!.createTileSource(tileMatrix, row, column)
            coverageSource.put(key, tileSource, 1)
        }
        val tileArray = coverageCache.get(tileSource)
        if (tileArray == null && this.enableRetrieval) {
            coverageRetriever.retrieve(tileSource, null, this)
        }
        return tileArray
    }

    protected open fun readHeightGrid(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        tileBlock: TileBlock,
        result: FloatArray
    ) {
        val tileWidth: Int = tileBlock.tileMatrix.tileWidth
        val tileHeight: Int = tileBlock.tileMatrix.tileHeight
        val rasterWidth: Int = tileBlock.tileMatrix.matrixWidth * tileWidth
        val rasterHeight: Int = tileBlock.tileMatrix.matrixHeight * tileHeight
        val matrixMinLat: Double = tileBlock.tileMatrix.sector.minLatitude
        val matrixMaxLat: Double = tileBlock.tileMatrix.sector.maxLatitude
        val matrixMinLon: Double = tileBlock.tileMatrix.sector.minLongitude
        val matrixMaxLon: Double = tileBlock.tileMatrix.sector.maxLongitude
        val matrixDeltaLat: Double = tileBlock.tileMatrix.sector.deltaLatitude()
        val matrixDeltaLon: Double = tileBlock.tileMatrix.sector.deltaLongitude()
        val sMin = 1.0 / (2.0 * rasterWidth)
        val sMax = 1.0 - sMin
        val tMin = 1.0 / (2.0 * rasterHeight)
        val tMax = 1.0 - tMin
        var ridx = 0

        var lat: Double = gridSector.minLatitude
        val deltaLat = gridSector.deltaLatitude() / (gridHeight - 1)

        var hidx = 0
        while (hidx < gridHeight) {
            if (hidx == gridHeight - 1) {
                lat =
                    gridSector.maxLatitude // explicitly set the last lat to the max latitude to ensure alignment
            }

            val t = (matrixMaxLat - lat) / matrixDeltaLat
            val v = rasterHeight * WWMath.clamp(
                t,
                tMin,
                tMax
            ) // clamp the vertical coordinate to the raster edge
            val b = WWMath.fract(v - 0.5).toFloat()
            val j0 =
                WWMath.clamp(Math.floor(v - 0.5), 0.0, (rasterHeight - 1).toDouble()).toInt()
            val j1 =
                WWMath.clamp((j0 + 1).toDouble(), 0.0, (rasterHeight - 1).toDouble()).toInt()
            val row0 = j0 / tileHeight
            val row1 = j1 / tileHeight

            var lon: Double = gridSector.minLongitude
            val deltaLon = gridSector.deltaLongitude() / (gridWidth - 1)

            var widx = 0
            while (widx < gridWidth) {
                if (widx == gridWidth - 1) {
                    lon =
                        gridSector.maxLongitude // explicitly set the last lon to the max longitude to ensure alignment
                }
                val s = (lon - matrixMinLon) / matrixDeltaLon
                var u: Double
                var i0: Int
                var i1: Int
                if (tileBlock.tileMatrix.sector.isFullSphere()) {
                    u = rasterWidth * WWMath.fract(s) // wrap the horizontal coordinate
                    i0 = WWMath.mod(Math.floor(u - 0.5).toInt(), rasterWidth)
                    i1 = WWMath.mod(i0 + 1, rasterWidth)
                } else {
                    u = rasterWidth * WWMath.clamp(s, sMin, sMax) // clamp the horizontal coordinate
                    i0 = WWMath.clamp(
                        Math.floor(u - 0.5),
                        0.0,
                        (rasterWidth - 1).toDouble()
                    ).toInt()
                    i1 = WWMath.clamp((i0 + 1).toDouble(), 0.0, (rasterWidth - 1).toDouble()).toInt()
                }
                val a = WWMath.fract(u - 0.5).toFloat()
                val col0 = i0 / tileWidth
                val col1 = i1 / tileWidth

                if (lat in matrixMinLat..matrixMaxLat && lon in matrixMinLon..matrixMaxLon) {
                    val i0j0 =
                        tileBlock.readTexel(row0, col0, i0 % tileWidth, j0 % tileHeight).toShort()
                    val i1j0 =
                        tileBlock.readTexel(row0, col1, i1 % tileWidth, j0 % tileHeight).toShort()
                    val i0j1 =
                        tileBlock.readTexel(row1, col0, i0 % tileWidth, j1 % tileHeight).toShort()
                    val i1j1 =
                        tileBlock.readTexel(row1, col1, i1 % tileWidth, j1 % tileHeight).toShort()
                    result[ridx] =
                        (1 - a) * (1 - b) * i0j0 + a * (1 - b) * i1j0 + (1 - a) * b * i0j1 + a * b * i1j1
                }
                ridx++
                widx++
                lon += deltaLon
            }
            hidx++
            lat += deltaLat
        }
    }

    protected open fun scanHeightLimits(
        sector: Sector,
        tileBlock: TileBlock,
        result: FloatArray
    ) {
        val tileWidth: Int = tileBlock.tileMatrix.tileWidth
        val tileHeight: Int = tileBlock.tileMatrix.tileHeight
        val rasterWidth: Int = tileBlock.tileMatrix.matrixWidth * tileWidth
        val rasterHeight: Int = tileBlock.tileMatrix.matrixHeight * tileHeight
        val matrixMaxLat: Double = tileBlock.tileMatrix.sector.maxLatitude
        val matrixMinLon: Double = tileBlock.tileMatrix.sector.minLongitude
        val matrixDeltaLat: Double = tileBlock.tileMatrix.sector.deltaLatitude()
        val matrixDeltaLon: Double = tileBlock.tileMatrix.sector.deltaLongitude()

        val intersection = Sector(tileBlock.tileMatrix.sector)
        intersection.intersect(sector)

        val sMin: Double = (intersection.minLongitude - matrixMinLon) / matrixDeltaLon
        val sMax: Double = (intersection.maxLongitude - matrixMinLon) / matrixDeltaLon
        val uMin = Math.floor(rasterWidth * sMin)
        val uMax = Math.ceil(rasterWidth * sMax)
        val iMin = WWMath.clamp(uMin, 0.0, (rasterWidth - 1).toDouble()).toInt()
        val iMax = WWMath.clamp(uMax, 0.0, (rasterWidth - 1).toDouble()).toInt()

        val tMin: Double = (matrixMaxLat - intersection.maxLatitude) / matrixDeltaLat
        val tMax: Double = (matrixMaxLat - intersection.minLatitude) / matrixDeltaLat
        val vMin = Math.floor(rasterHeight * tMin)
        val vMax = Math.ceil(rasterHeight * tMax)
        val jMin = WWMath.clamp(vMin, 0.0, (rasterHeight - 1).toDouble()).toInt()
        val jMax = WWMath.clamp(vMax, 0.0, (rasterHeight - 1).toDouble()).toInt()

        var ridx = 0
        val rlen = tileBlock.rows.size()
        while (ridx < rlen) {
            val row = tileBlock.rows.keyAt(ridx)
            val rowjMin = row * tileHeight
            val rowjMax = rowjMin + tileHeight - 1
            val j0 = WWMath.clamp(jMin.toDouble(), rowjMin.toDouble(), rowjMax.toDouble())
                .toInt() % tileHeight
            val j1 = WWMath.clamp(jMax.toDouble(), rowjMin.toDouble(), rowjMax.toDouble())
                .toInt() % tileHeight
            var cidx = 0
            val clen = tileBlock.cols.size()
            while (cidx < clen) {
                val col = tileBlock.cols.keyAt(cidx)
                val coliMin = col * tileWidth
                val coliMax = coliMin + tileWidth - 1
                val i0 = WWMath.clamp(iMin.toDouble(), coliMin.toDouble(), coliMax.toDouble())
                    .toInt() % tileWidth
                val i1 = WWMath.clamp(iMax.toDouble(), coliMin.toDouble(), coliMax.toDouble())
                    .toInt() % tileWidth
                val tileArray = tileBlock.getTileArray(row, col)

                for (j in j0..j1) {
                    for (i in i0..i1) {
                        val pos = i + j * tileWidth
                        tileArray?.get(pos)?.let {
                            if (result[0] > it) {
                                result[0] = it.toFloat()
                            }
                            if (result[1] < it) {
                                result[1] = it.toFloat()
                            }
                        }
                    }
                }
                cidx++
            }
            ridx++
        }
    }

    protected class TileBlock {
        lateinit var tileMatrix: TileMatrix
        var rows = SparseIntArray()
        var cols = SparseIntArray()
        var arrays = LongSparseArray<ShortArray>()
        private var texelRow = -1
        private var texelCol = -1
        private var texelArray: ShortArray? = null
        fun clear() {
            rows.clear()
            cols.clear()
            arrays.clear()
            texelRow = -1
            texelCol = -1
            texelArray = null
        }

        fun putTileArray(row: Int, column: Int, array: ShortArray) {
            val key = tileKey(tileMatrix, row, column)
            arrays.put(key, array)
        }

        fun getTileArray(row: Int, column: Int): ShortArray? {
            if (texelRow != row || texelCol != column) {
                val key = tileKey(tileMatrix, row, column)
                texelRow = row
                texelCol = column
                texelArray = arrays[key]
            }
            return texelArray
        }

        fun readTexel(row: Int, column: Int, i: Int, j: Int): Short {
            val array = getTileArray(row, column)
            val pos: Int = i + j * tileMatrix.tileWidth
            return array!![pos]
        }
    }
}