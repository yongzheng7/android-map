package com.atom.wyz.worldwind.app

import android.os.Bundle
import android.view.Choreographer

class BasicStressTestActivity : BasicWorldWindActivity() , Choreographer.FrameCallback {
    protected var cameraDegreesPerSecond = 0.1

    protected var activityPaused = false

    protected var lastFrameTimeNanos: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigator = getWorldWindow().navigator
        navigator.altitude = (1e3) // 1 km
        navigator.heading = (90.0) // looking east
        navigator.tilt = (75.0) // looking at the horizon
        // Use this Activity's Choreographer to animate the Navigator.
        Choreographer.getInstance().postFrameCallback(this)
    }
    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) {
            val frameDurationSeconds = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-9
            val cameraDegrees = frameDurationSeconds * cameraDegreesPerSecond
            val navigator= getWorldWindow().navigator
            navigator.longitude = (navigator.longitude + cameraDegrees)
            getWorldWindow().requestRedraw()
        }

        if (!activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this)
        }

        lastFrameTimeNanos = frameTimeNanos
    }

    override fun onPause() {
        super.onPause()
        activityPaused = true
    }

    override fun onResume() {
        super.onResume()
        // Resume the Navigator animation.
        activityPaused = false
        Choreographer.getInstance().postFrameCallback(this)
    }
}