package com.atom.wyz.worldwind.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.test.EarthHelper
import com.atom.wyz.worldwind.layer.AtmosphereLayer
import com.atom.wyz.worldwind.layer.BackgroundLayer
import com.atom.wyz.worldwind.layer.BlueMarbleLandsatLayer

@SuppressLint("Registered")
open class BasicTextureWorldWindActivity : AppCompatActivity() {

    var helper: EarthHelper? = null

    private val callback1 = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder?) {
            helper?.addLayer(BackgroundLayer())
            helper?.addLayer(BlueMarbleLandsatLayer())
            helper?.addLayer(AtmosphereLayer())
        }

        override fun surfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            helper?.setSurface(holder?.surface)
            helper?.setPreviewSize(width, height)
            helper?.open()
            helper?.startPreview()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            helper?.close()
        }
    }

    val callback2 = object : SurfaceHolder.Callback2 {
        override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {
        }

        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_texture)
        val globeLayout = findViewById<SurfaceView>(R.id.mSurfaceView)
        helper = EarthHelper()
        globeLayout.setOnTouchListener(helper)
        globeLayout.holder.addCallback(callback1)
    }
}