package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.CartesianLayer
import com.atom.wyz.worldwind.layer.ShowTessellationLayer

class CartesianActivity : BasicGlobeActivity(){
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        return wwd
    }
}