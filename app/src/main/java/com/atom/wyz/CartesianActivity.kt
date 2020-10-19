package com.atom.wyz

import com.atom.map.WorldWindow
import com.atom.map.layer.CartesianLayer
import com.atom.map.layer.ShowTessellationLayer

class CartesianActivity : BasicGlobeActivity(){
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        return wwd
    }
}