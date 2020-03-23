package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.layer.ShowTessellationLayer

class ShowTessellationActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a layer that displays the globe's tessellation geometry.
        val layer = ShowTessellationLayer()

        // Add the WMS layer to the World Window before the Atmosphere layer.
        // Add the WMS layer to the World Window before the Atmosphere layer.
        val index: Int = getWorldWindow().layers.indexOfLayerNamed("Atmosphere")
        getWorldWindow().layers.addLayer(index, layer)
    }
}