package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileUrlFactory
import com.atom.wyz.worldwind.util.Logger
import java.util.*

class WmsGetMapUrlFactory : TileUrlFactory {

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

        styleNames = config.styleNames
        transparent = config.transparent
        timeString = config.timeString
    }

    /**
     * 根据tile 和 图片格式 返回url
     */
    override fun urlForTile(tile: Tile, imageFormat: String): String {
        val url = StringBuilder(serviceAddress!!)
        var index = url.indexOf("?")
        if (index < 0) { // if service address contains no query delimiter
            url.append("?") // add one
        } else if (index != url.length- 1) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&")
            if (index != url.length - 1) {
                url.append("&") // add a parameter delimiter
            }
        }
        index = serviceAddress!!.toUpperCase(Locale.US).indexOf("SERVICE=WMS")
        if (index < 0) {
            url.append("SERVICE=WMS")
        }
        url.append("&VERSION=").append(wmsVersion)
        url.append("&REQUEST=GetMap")
        url.append("&LAYERS=").append(layerNames)
        url.append("&STYLES=").append(if (styleNames != null) styleNames else "")
        val sector: Sector = tile.sector
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
        url.append("&WIDTH=").append(tile.level.tileWidth)
        url.append("&HEIGHT=").append(tile.level.tileHeight)
        url.append("&FORMAT=").append(imageFormat)
        url.append("&TRANSPARENT=").append(if (transparent) "TRUE" else "FALSE")
        if (timeString != null) {
            url.append("&TIME=").append(timeString)
        }
        return url.toString()
    }
}