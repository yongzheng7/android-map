package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.layer.ShowTessellationLayer

class ShowTessellationActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layer = ShowTessellationLayer()
        getWorldWindow().layers.addLayer(layer)
    }
}