package com.atom.wyz.layer

import android.util.Log
import com.atom.map.WorldWindow
import com.atom.map.layer.CartesianLayer
import com.atom.map.layer.Layer
import com.atom.map.layer.LayerFactory
import com.atom.map.layer.ShowTessellationLayer
import com.atom.wyz.base.BasicGlobeActivity

class WtmsLayerActivity : BasicGlobeActivity() {
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        // Create an OGC Web Map Tile Service (WMTS) layer to display Global Hillshade based on GMTED2010
        LayerFactory().createFromWmts(
            "https://tiles.geoservice.dlr.de/service/wmts",  // WMTS server URL
            "hillshade",  // WMTS layer identifier
            object : LayerFactory.Callback {
                override fun creationSucceeded(factory: LayerFactory, layer: Layer) {
                    // Add the finished WMTS layer to the World Window.
                    wwd.layers.addLayer(layer)
                    Log.i("gov.nasa.worldwind", "WMTS layer creation succeeded")
                }

                override fun creationFailed(
                    factory: LayerFactory,
                    layer: Layer,
                    ex: Throwable?
                ) {
                    // Something went wrong connecting to the WMTS server.
                    Log.e("gov.nasa.worldwind", "WMTS layer creation failed", ex)
                }
            }
        )
        return wwd
    }
}