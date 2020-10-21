package com.atom.wyz.math

import android.os.Bundle
import com.atom.map.layer.ShowTessellationLayer
import com.atom.wyz.base.BasicWorldWindActivity

class ShowTessellationActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layer = ShowTessellationLayer()
        getWorldWindow().layers.addLayer(layer)
    }
}