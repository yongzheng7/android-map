package com.atom.wyz

import android.os.Bundle
import com.atom.wyz.BasicWorldWindActivity
import com.atom.map.layer.ShowTessellationLayer

class ShowTessellationActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layer = ShowTessellationLayer()
        getWorldWindow().layers.addLayer(layer)
    }
}