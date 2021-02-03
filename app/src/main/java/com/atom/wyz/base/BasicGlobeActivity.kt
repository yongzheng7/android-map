package com.atom.wyz.base

import android.graphics.*
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

    protected fun drawText(text: String, width : Int, height : Int): Bitmap {
        val canvas = Canvas()
        val scratchBounds = Rect()
        val paint = Paint().apply {
            this.isAntiAlias = true
            this.textAlign  = Paint.Align.LEFT
            this.style = Paint.Style.FILL
            this.color = Color.YELLOW
        }
        val p = Paint().apply {
            this.style = (Paint.Style.STROKE)
            this.color = Color.GREEN
            this.isAntiAlias = true
        }
        paint.getTextBounds(text, 0, text.length, scratchBounds)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        canvas.setBitmap(bitmap)
        canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()) , p )
        canvas.drawText(text, (width/2)-(scratchBounds.width()/2).toFloat(), (height/2)-(scratchBounds.height()/2).toFloat(), paint)
        canvas.setBitmap(null)
        return bitmap
    }

}