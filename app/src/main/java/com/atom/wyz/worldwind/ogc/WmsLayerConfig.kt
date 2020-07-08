package com.atom.wyz.worldwind.ogc

class WmsLayerConfig {
    /**
     * The WMS service address used to build Get Map URLs.
     * 用于构建“获取地图URL”的WMS服务地址。
     */
    var serviceAddress: String = "https://worldwind25.arc.nasa.gov/wms"

    /**
     * The WMS protocol version.
     * WMS协议版本。
     */
    var wmsVersion: String = "1.3.0"

    /**
     * The comma-separated list of WMS layer names.
     * WMS图层名称的逗号分隔列表。
     */
    var layerNames: String = "BlueMarble-200405"

    /**
     * The comma-separated list of WMS style names.
     * WMS样式名称的逗号分隔列表。
     */
    var styleNames: String? = null

    /**
     * The coordinate reference system to use when requesting layers.
     * 请求图层时使用的坐标参考系统。
     */
    var coordinateSystem: String? = "EPSG:4326"

    /**
     * Indicates whether Get Map requests should include transparency.
     * 指示“获取地图”请求是否应包括透明度。
     */
    var transparent = true

    /**
     * The time parameter to include in Get Map requests.
     * 要包含在“获取地图”请求中的时间参数。
     */
    var timeString: String? = null

    /**
     * Constructs a WMS layer configuration with values all null (or false).
     */
    constructor() {}

    /**
     * Constructs a WMS layer configuration with specified values.
     *
     * @param serviceAddress   the WMS service address
     * @param wmsVersion       the WMS protocol version
     * @param layerNames       comma-separated list of WMS layer names
     * @param styleNames       comma-separated list of WMS style names
     * @param coordinateSystem the coordinate reference system to use when requesting layers
     * @param transparent      indicates whether Get Map requests should include transparency
     * @param timeString       the time parameter to include in Get Map requests
     */
    constructor(serviceAddress: String, wmsVersion: String, layerNames: String, styleNames: String?, coordinateSystem: String?, transparent: Boolean, timeString: String?) {
        this.serviceAddress = serviceAddress
        this.wmsVersion = wmsVersion
        this.layerNames = layerNames
        this.styleNames = styleNames
        this.coordinateSystem = coordinateSystem
        this.transparent = transparent
        this.timeString = timeString
    }

    constructor(serviceAddress: String , layerNames: String) {
        this.serviceAddress = serviceAddress
        this.layerNames = layerNames
    }
}