package com.atom.wyz.base

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.map.WorldWindow
import com.atom.map.layer.BackgroundLayer
import com.atom.map.layer.BlueMarbleLandsatLayer
import com.atom.wyz.worldwind.R

open class BasicGlobeActivity :  AppCompatActivity(){

    protected lateinit var wwd: WorldWindow
    protected var layoutResourceId: Int = R.layout.activity_turse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutResourceId)
        createWorldWindow()
    }

    protected open fun createWorldWindow() : WorldWindow{
        wwd = WorldWindow(this)
        val globeLayout = findViewById<FrameLayout>(R.id.globe)
        globeLayout.addView(wwd)
        wwd.layers.addLayer(BackgroundLayer())
        wwd.layers.addLayer(BlueMarbleLandsatLayer())
        return wwd
    }

}