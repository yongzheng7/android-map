package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.TileMatrix
import com.atom.wyz.worldwind.render.ImageSource
import java.util.*

class Wcs201TileFactory(
    var serviceAddress: String,
    var coverageId: String
) : TiledElevationCoverage.TileFactory {

    override fun createTileSource(tileMatrix: TileMatrix, row: Int, column: Int): ImageSource {
        val urlString: String = this.urlForTile(tileMatrix, row, column)
        return ImageSource.fromUrl(urlString)
    }

    protected fun urlForTile(tileMatrix: TileMatrix, row: Int, col: Int): String {
        val url = StringBuilder(serviceAddress)
        val sector: Sector = tileMatrix.tileSector(row, col)
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
        url.append("&VERSION=2.0.1")
        url.append("&REQUEST=GetCoverage")
        url.append("&COVERAGEID=").append(coverageId)
        url.append("&FORMAT=image/tiff")
        url.append("&SUBSET=Lat(")
        url.append(sector.minLatitude).append(",").append(sector.maxLatitude).append(")")
        url.append("&SUBSET=Long(")
        url.append(sector.minLongitude).append(",").append(sector.maxLongitude).append(")")
        url.append("&SCALESIZE=")
        url.append("http://www.opengis.net/def/axis/OGC/1/i(").append(tileMatrix.tileWidth)
            .append("),")
        url.append("http://www.opengis.net/def/axis/OGC/1/j(").append(tileMatrix.tileHeight)
            .append(")")
        url.append("&OVERVIEWPOLICY=NEAREST")
        return url.toString()
    }
}