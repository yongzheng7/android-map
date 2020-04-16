package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.AtmosphereLayer
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.BlueMarbleLandsatLayer

@SuppressLint("Registered")
open class BasicWorldWindActivity : AppCompatActivity() {
    protected lateinit var wwd: WorldWindow

    open protected fun getWorldWindow(): WorldWindow {
        return wwd
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the World Window and set it as the content view for this activity.
        wwd = WorldWindow(this)
        this.setContentView(wwd)

        wwd.layers.addLayer(BackgroundLayer())
        wwd.layers.addLayer(BlueMarbleLandsatLayer())
        wwd.layers.addLayer(AtmosphereLayer())


    }

    override fun onPause() {
        super.onPause()
        wwd.onPause() // pauses the rendering thread
    }

    override fun onResume() {
        super.onResume()
        wwd.onResume() // resumes a paused rendering thread
    }
}