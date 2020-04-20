package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.layer.AtmosphereLayer
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.BlueMarbleLandsatLayer

@SuppressLint("Registered")
open class BasicWorldWindActivity : AppCompatActivity() {
    protected lateinit var wwd: WorldWindow
    protected var layoutResourceId: Int = R.layout.activity_main
    open protected fun getWorldWindow(): WorldWindow {
        return wwd
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutResourceId)
        wwd = WorldWindow(this)
        val globeLayout = findViewById(R.id.globe) as FrameLayout
        globeLayout.addView(wwd)
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