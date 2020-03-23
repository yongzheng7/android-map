package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileUrlFactory
import com.atom.wyz.worldwind.util.Logger
import java.lang.IllegalArgumentException

class WmsGetMapUrlFactory : TileUrlFactory {

    /**
     * The WMS service address used to build Get Map URLs.
     * 用于构建“获取地图URL”的WMS服务地址。
     */
     var serviceAddress: String? = null

    /**
     * The WMS protocol version.
     */
     var wmsVersion: String? = null

    /**
     * The comma-separated list of WMS layer names.
     */
     var layerNames: String? = null

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
     * Constructs a WMS Get Map URL builder with specified WMS service parameters.
     *
     * @param serviceAddress the WMS service address
     * @param wmsVersion     the WMS protocol version
     * @param layerNames     comma-separated list of WMS layer names
     * @param styleNames     comma-separated list of WMS style names, may be null in which case the default style is
     * assumed
     *
     * @throws IllegalArgumentException If any of the service address, the WMS protocol version, or the layer names are
     * null
     */
    constructor(serviceAddress: String?, wmsVersion: String?, layerNames: String?, styleNames: String?) {
        if (serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingServiceAddress"))
        }
        if (wmsVersion == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingVersion"))
        }
        if (layerNames == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingLayerNames"))
        }
        this.serviceAddress = serviceAddress
        this.wmsVersion = wmsVersion
        this.layerNames = layerNames
        this.styleNames = styleNames
    }

    /**
     * Constructs a level set with a specified configuration. The configuration's service address, WMS protocol version,
     * layer names and coordinate reference system must be non-null. The style names may be null, in which case the
     * default style is assumed. The time string may be null, in which case no time parameter is included.
     *
     * @param config the configuration for this URL builder
     *
     * @throws IllegalArgumentException If the configuration is null, or if any configuration value is invalid
     */
    constructor(config: WmsLayerConfig?) {
        if (config == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingConfig"))
        }
        if (config.serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingServiceAddress"))
        }
        if (config.wmsVersion == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingVersion"))
        }
        if (config.layerNames == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingLayerNames"))
        }
        if (config.coordinateSystem == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "constructor", "missingCoordinateSystem"))
        }
        serviceAddress = config.serviceAddress
        wmsVersion = config.wmsVersion
        layerNames = config.layerNames
        styleNames = config.styleNames
        coordinateSystem = config.coordinateSystem
        transparent = config.transparent
        timeString = config.timeString
    }

    /**
     * 根据tile 和 图片格式 返回url
     */
    override fun urlForTile(tile: Tile?, imageFormat: String?): String {
        if (tile == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "urlForTile", "missingTile"))
        }
        if (imageFormat == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsGetMapUrlFactory", "urlForTile", "missingFormat"))
        }
        val url = StringBuilder(serviceAddress!!)
        var index = url.indexOf("?")
        if (index < 0) { // if service address contains no query delimiter
            url.append("?") // add one
        } else if (index != url.length) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&")
            if (index != url.length - 1) {
                url.append("&") // add a parameter delimiter
            }
        }
        index = serviceAddress!!.toUpperCase().indexOf("SERVICE=WMS")
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