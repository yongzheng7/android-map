package com.atom.map.ogc

import com.atom.map.geom.TileMatrix
import com.atom.map.layer.render.ImageSource
import java.util.*

class Wcs100TileFactory : TiledElevationCoverage.TileFactory {
    protected var serviceAddress: String

    protected var coverage: String

    constructor(
        serviceAddress: String,
        coverage: String
    ) {
        this.serviceAddress = serviceAddress
        this.coverage = coverage
    }

    override fun createTileSource(tileMatrix: TileMatrix, row: Int, column: Int): ImageSource {
        val urlString = urlForTile(tileMatrix, row, column)
        return ImageSource.fromUrl(urlString)
    }

    protected fun urlForTile(tileMatrix: TileMatrix, row: Int, col: Int): String? {
        val url = StringBuilder(serviceAddress)
        val sector = tileMatrix.tileSector(row, col)
        var index = url.indexOf("?")
        if (index < 0) { // if service address contains no query delimiter
            url.append("?") // add one
        } else if (index != url.length - 1) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&")
            if (index != url.length - 1) {
                url.append("&") // add a parameter delimiter
            }
        }
        index = serviceAddress.toUpperCase(Locale.US).indexOf("SERVICE=WCS")
        if (index < 0) {
            url.append("SERVICE=WCS")
        }
        url.append("&VERSION=1.0.0")
        url.append("&REQUEST=GetCoverage")
        url.append("&COVERAGE=").append(coverage)
        url.append("&CRS=EPSG:4326")
        url.append("&BBOX=")
            .append(sector.minLongitude).append(",")
            .append(sector.minLatitude).append(",")
            .append(sector.maxLongitude).append(",")
            .append(sector.maxLatitude)
        url.append("&WIDTH=").append(tileMatrix.tileWidth)
        url.append("&HEIGHT=").append(tileMatrix.tileHeight)
        url.append("&FORMAT=image/tiff")
        return url.toString()
    }

}