package com.atom.wyz.math

import com.atom.map.WorldWindow
import com.atom.map.layer.CartesianLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.wyz.base.BasicGlobeActivity

class CartesianActivity : BasicGlobeActivity(){
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        return wwd
    }
}