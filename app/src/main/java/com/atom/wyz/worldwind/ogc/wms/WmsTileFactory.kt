package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.ImageTile
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.Logger
import java.util.*

class WmsTileFactory : TileFactory {

     var serviceAddress: String

    /**
     * The WMS protocol version.
     */
     var wmsVersion: String
    /**
     * The comma-separated list of WMS layer names.
     */
     var layerNames: String

    /**
     * The comma-separated list of WMS style names. May be null in which case the default style is assumed.
     */
     var styleNames: String? = null

    /**
     * The coordinate reference system to use in Get Map URLs. Defaults to EPSG:4326.
     * 在“获取地图URL”中使用的坐标参考系统。 默认为EPSG：4326。
     */
     var coordinateSystem: String? = "EPSG:4326"

    /**
     * Indicates whether Get Map URLs should include transparency.
     * 指示“获取地图URL”是否应包括透明度。
     */
     var transparent = true

    /**
     * The time parameter to include in Get Map URLs. May be null in which case no time parameter is included.
     * 要包含在“获取地图URL”中的时间参数。 在没有时间参数的情况下，可以为null。
     */
     var timeString: String? = null

    /**
     * The image MIME format to use in Get Map URLs. May be null in which case a default format is assumed.
     */
     var imageFormat: String? = null


    constructor(serviceAddress: String, wmsVersion: String, layerNames: String, styleNames: String?) {
        this.serviceAddress = serviceAddress
        this.wmsVersion = wmsVersion
        this.layerNames = layerNames

        this.styleNames = styleNames
    }

    constructor(config: WmsLayerConfig) {
        serviceAddress = config.serviceAddress
        wmsVersion = config.wmsVersion
        layerNames = config.layerNames
        coordinateSystem = config.coordinateSystem
        imageFormat = config.imageFormat
        styleNames = config.styleNames
        transparent = config.transparent
        timeString = config.timeString
    }


    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        if (sector == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsTileFactory", "createTile", "missingSector")
            )
        }

        if (level == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsTileFactory", "createTile", "missingLevel")
            )
        }

        val tile = ImageTile(sector, level, row, column)
        val urlString = urlForTile(sector, level.tileWidth, level.tileHeight)
        tile.imageSource = (ImageSource.fromUrl(urlString))
        return tile
    }
    /**
     * 根据tile 和 图片格式 返回url
     */
     fun urlForTile(sector: Sector , width: Int , height:Int ): String {
        require(!(width < 1 || height < 1)) {
            Logger.logMessage(
                Logger.ERROR,
                "WmsTileFactory",
                "urlForTile",
                "invalidWidthOrHeight"
            )
        }
        val url = StringBuilder(serviceAddress)
        var index = url.indexOf("?")
        if (index < 0) { // if service address contains no query delimiter
            url.append("?") // add one
        } else if (index != url.length- 1) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&")
            if (index != url.length - 1) {
                url.append("&") // add a parameter delimiter
            }
        }
        index = serviceAddress.toUpperCase(Locale.US).indexOf("SERVICE=WMS")
        if (index < 0) {
            url.append("SERVICE=WMS")
        }
        url.append("&VERSION=").append(wmsVersion)
        url.append("&REQUEST=GetMap")
        url.append("&LAYERS=").append(layerNames)
        url.append("&STYLES=").append(if (styleNames != null) styleNames else "")
        if (wmsVersion == "1.3.0") {
            url.append("&CRS=").append(coordinateSystem)
            url.append("&BBOX=")
            if (coordinateSystem == "CRS:84") {
                url.append(sector.minLongitude).append(",").append(sector.minLatitude).append(",")
                url.append(sector.maxLongitude).append(",").append(sector.maxLatitude)
            } else {
                url.append(sector.minLatitude).append(",").append(sector.minLongitude).append(",")
                url.append(sector.maxLatitude).append(",").append(sector.maxLongitude)
            }
        } else {
            url.append("&SRS=").append(coordinateSystem)
            url.append("&BBOX=")
            url.append(sector.minLongitude).append(",").append(sector.minLatitude).append(",")
            url.append(sector.maxLongitude).append(",").append(sector.maxLatitude)
        }
        url.append("&WIDTH=").append(width)
        url.append("&HEIGHT=").append(height)
        url.append("&FORMAT=").append(this.imageFormat ?: "image/png")
        url.append("&TRANSPARENT=").append(if (transparent) "TRUE" else "FALSE")
        if (timeString != null) {
            url.append("&TIME=").append(timeString)
        }
        return url.toString()
    }

}