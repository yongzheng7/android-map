package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.WmsLayer
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.util.Logger

class BlueMarbleLayer : WmsLayer {

    constructor() : this("https://worldwind25.arc.nasa.gov/wms")

    constructor(serviceAddress: String?) : super("Blue Marble"){
        if (serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleLayer", "constructor", "missingServiceAddress"))
        }
        val config: WmsLayerConfig = WmsLayerConfig()
        config.serviceAddress = serviceAddress
        config.wmsVersion = "1.3.0"
        config.layerNames = "BlueMarble-200405"
        config.coordinateSystem = "EPSG:4326"
        config.transparent = false // the BlueMarble layer is opaque

        setConfiguration(Sector().setFullSphere(), 500.0, config)
    }
}