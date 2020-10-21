package com.atom.wyz.layer

import android.util.Log
import com.atom.map.WorldWindow
import com.atom.map.layer.Layer
import com.atom.map.layer.LayerFactory
import com.atom.map.layer.ShowTessellationLayer
import com.atom.wyz.base.BasicGlobeActivity

class WmsLayerActivity : BasicGlobeActivity() {
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(ShowTessellationLayer())
        // Create an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        LayerFactory().createFromWms(
            "https://neo.sci.gsfc.nasa.gov/wms/wms",  // WMS server URL
            "MOD_LSTD_CLIM_M",  // WMS layer name
            object : LayerFactory.Callback {
                override fun creationSucceeded(factory: LayerFactory, layer: Layer) {
                    // Add the finished WMS layer to the World Window.
                    wwd.layers.addLayer(layer)
                    Log.i("gov.nasa.worldwind", "WMS layer creation succeeded")
                }

                override fun creationFailed(
                    factory: LayerFactory,
                    layer: Layer,
                    ex: Throwable?
                ) {
                    // Something went wrong connecting to the WMS server.
                    Log.e("gov.nasa.worldwind", "WMS layer creation failed", ex)
                }
            }
        )
        return wwd
    }
}