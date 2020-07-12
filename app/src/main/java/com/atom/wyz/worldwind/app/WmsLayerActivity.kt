package com.atom.wyz.worldwind.app

import android.util.Log
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerFactory

class WmsLayerActivity : BasicGlobeActivity() {
    override fun createWorldWindow(): WorldWindow {
        val wwd : WorldWindow  =super.createWorldWindow()
        // Configure an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        val layerFactory = LayerFactory()
        val layer = layerFactory.createFromWms(
            "http://neowms.sci.gsfc.nasa.gov/wms/wms",
            "MOD_LSTD_CLIM_M",
            object : LayerFactory.Callback {
                override fun creationSucceeded(factory: LayerFactory, layer: Layer) {
                    Log.d("gov.nasa.worldwind", "MOD_LSTD_CLIM_M created successfully")
                }

                override fun creationFailed(
                    factory: LayerFactory,
                    layer: Layer,
                    ex: Throwable?
                ) {
                    Log.e(
                        "gov.nasa.worldwind",
                        "MOD_LSTD_CLIM_M failed: " + (ex?.toString() ?: "")
                    )
                }
            }
        )

        wwd.layers.addLayer(layer)

        return wwd
    }
}