package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.CartesianLayer
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerFactory

class WmsLayerActivity : BasicGlobeActivity() {
    override fun createWorldWindow(): WorldWindow {
        val wwd : WorldWindow  =super.createWorldWindow()
        wwd.layers.addLayer(CartesianLayer())
        val layerFactory = LayerFactory()
        layerFactory.createFromWms(
            "http://neowms.sci.gsfc.nasa.gov/wms/wms",  // WMS server URL
            "MOD_LSTD_CLIM_M",  // WMS layer name
            object : LayerFactory.Callback {
                override fun creationSucceeded(factory: LayerFactory, layer: Layer) {
                    // Add the finished WMS layer to the World Window.
                    wwd.layers.addLayer(layer)
                    wwd.requestRedraw()
                }

                override fun creationFailed(
                    factory: LayerFactory,
                    layer: Layer,
                    ex: Throwable?
                ) {
                }
            }
        )

        return wwd
    }
}