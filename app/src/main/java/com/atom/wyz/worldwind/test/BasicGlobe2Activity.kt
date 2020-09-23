package com.atom.wyz.worldwind.test

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.test.WorldWindow2
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.BlueMarbleLandsatLayer

open class BasicGlobe2Activity :  AppCompatActivity(){

    protected lateinit var wwd: WorldWindow2

    protected var layoutResourceId: Int = R.layout.activity_turse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutResourceId)
        createWorldWindow()
    }

    protected open fun createWorldWindow() : WorldWindow2 {
        wwd = WorldWindow2(this)
        val globeLayout = findViewById<FrameLayout>(R.id.globe)
        globeLayout.addView(wwd)
        wwd.addLayer(BackgroundLayer())
        wwd.addLayer(BlueMarbleLandsatLayer())
        return wwd
    }

}