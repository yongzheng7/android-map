package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.WmsLayer
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.util.Logger

class LandsatLayer : WmsLayer {
    /**
     * Constructs a Landsat image layer with the WMS at http://worldwind25.arc.nasa.gov/wms.
     */
    constructor():super("https://worldwind25.arc.nasa.gov/wms")

    /**
     * Constructs a Landsat image layer with the WMS at a specified address.
     *
     * @param serviceAddress a URL string specifying the WMS address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    constructor(serviceAddress: String? )  {
        if (serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LandsatLayer", "constructor", "missingServiceAddress"))
        }
        val config: WmsLayerConfig = WmsLayerConfig()
        config.serviceAddress = serviceAddress
        config.wmsVersion = "1.3.0"
        config.layerNames = "esat"
        config.coordinateSystem = "EPSG:4326"
        this.displayName = ("Landsat")
        this.setConfiguration(Sector().setFullSphere(), 15.0, config) // 15m resolution on Earth
    }
}