package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.WmsLayer
import com.atom.wyz.worldwind.ogc.WmsLayerConfig

class WmsLayerActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config: WmsLayerConfig = WmsLayerConfig()
        config.serviceAddress = "http://neowms.sci.gsfc.nasa.gov/wms/wms"
        config.wmsVersion = "1.1.1" // NEO server works best with WMS 1.1.1
        config.layerNames = "MYD28M" // Sea surface temperature (MODIS)

        val layer: WmsLayer = WmsLayer(Sector().setFullSphere(), 1e3, config) // 1km resolution


        val index: Int = getWorldWindow().layers.indexOfLayerNamed("Atmosphere")
        getWorldWindow().layers.addLayer(index, layer)
    }
}