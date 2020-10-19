package com.atom.map.ogc

class WmsLayerConfig {
    /**
     * The WMS service address used to build Get Map URLs.
     */
   lateinit var serviceAddress: String

    /**
     * The WMS protocol version. Defaults to 1.3.0.
     */
    var wmsVersion = "1.3.0"

    /**
     * The comma-separated list of WMS layer names.
     */
    lateinit var layerNames: String

    /**
     * The comma-separated list of WMS style names.
     */
    var styleNames: String? = null

    /**
     * The coordinate reference system to use when requesting layers. Defaults to EPSG:4326.
     */
    var coordinateSystem = "EPSG:4326"

    /**
     * Indicates whether Get Map requests should include transparency.
     */
    var transparent = true

    /**
     * The image content type to use in Get Map requests.
     */
    var imageFormat: String? = null

    /**
     * The time parameter to include in Get Map requests.
     */
    var timeString: String? = null

    constructor() {}

    constructor(
        serviceAddress: String,
        wmsVersion: String,
        layerNames: String,
        styleNames: String?,
        coordinateSystem: String,
        imageFormat: String?,
        transparent: Boolean,
        timeString: String?
    ) {
        this.serviceAddress = serviceAddress
        this.wmsVersion = wmsVersion
        this.layerNames = layerNames
        this.styleNames = styleNames
        this.coordinateSystem = coordinateSystem
        this.imageFormat = imageFormat
        this.transparent = transparent
        this.timeString = timeString
    }
}