package com.atom.wyz

import android.os.Bundle
import android.view.Choreographer
import com.atom.map.layer.ShowTessellationLayer
class BasicStressTestActivity : BasicWorldWindActivity() , Choreographer.FrameCallback {
    protected var cameraDegreesPerSecond = 0.1

    protected var lastFrameTimeNanos: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add the ShowTessellation layer to provide some visual feedback regardless of texture details
        getWorldWindow().layers.addLayer(ShowTessellationLayer())


        val navigator = getWorldWindow().navigator
        navigator.altitude = (1e3) // 1 km
        navigator.heading = (90.0) // looking east
        navigator.tilt = (75.0) // looking at the horizon
    }
    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) {
            val frameDurationSeconds = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-9
            val cameraDegrees = frameDurationSeconds * cameraDegreesPerSecond
            val navigator= getWorldWindow().navigator
            navigator.longitude = (navigator.longitude + cameraDegrees)
            getWorldWindow().requestRedraw()
        }
        Choreographer.getInstance().postFrameCallback(this)

        lastFrameTimeNanos = frameTimeNanos
    }

    override fun onPause() {
        super.onPause()
        lastFrameTimeNanos = 0
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun onResume() {
        super.onResume()
        Choreographer.getInstance().postFrameCallback(this)
        lastFrameTimeNanos = 0
    }
}