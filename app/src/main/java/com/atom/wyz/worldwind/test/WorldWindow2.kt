package com.atom.wyz.worldwind.test

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.test.WorldWindowHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class WorldWindow2 : GLSurfaceView, GLSurfaceView.Renderer, FrameCallback,
    WorldWindowHelper.Listener {

    val helper: WorldWindowHelper =
        WorldWindowHelper(this)

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, configChooser: EGLConfigChooser) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        super.surfaceCreated(holder)
        helper.onCreate()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        super.surfaceDestroyed(holder)
        helper.onDestroyed()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return helper.onTouch(this, event)
    }

    override fun onResume() {
        super.onResume()
        helper.onResume()
    }

    override fun onPause() {
        super.onPause()
        helper.onPause()
    }

    fun addLayer(layer: Layer) {
        helper.addLayer(layer)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        helper.create()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        helper.sizeChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        helper.draw()
    }

    override fun registerRedraw() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun unregisterRedraw() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        helper.doFrame(frameTimeNanos)
    }

}