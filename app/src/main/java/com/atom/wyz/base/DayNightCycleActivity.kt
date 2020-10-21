package com.atom.wyz.base

import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import com.atom.map.geom.Location
import com.atom.map.layer.AtmosphereLayer
import com.atom.map.layer.LayerList
import com.atom.map.navigator.Navigator

class DayNightCycleActivity : BasicWorldWindActivity(), FrameCallback {

    protected var sunLocation: Location = Location(0.0, -100.0)

    protected var atmosphereLayer: AtmosphereLayer? = null

    protected var cameraDegreesPerSecond = 2.0

    protected var lightDegreesPerSecond = 6.0

    protected var activityPaused = false

    protected var lastFrameTimeNanos: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layers: LayerList = getWorldWindow().layers
        atmosphereLayer = layers.getLayer(layers.indexOfLayerNamed("Atmosphere")) as AtmosphereLayer
        atmosphereLayer?.lightLocation = sunLocation
        val navigator: Navigator = getWorldWindow().navigator
        navigator.latitude = (20.0)
        navigator.longitude = (sunLocation.longitude)
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) {
            val frameDurationSeconds = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-9
            val cameraDegrees = frameDurationSeconds * cameraDegreesPerSecond
            val lightDegrees = frameDurationSeconds * lightDegreesPerSecond
            val navigator = getWorldWindow().navigator
            navigator.longitude = (navigator.longitude - cameraDegrees)
            sunLocation.set(sunLocation.latitude, sunLocation.longitude - lightDegrees)
            atmosphereLayer?.lightLocation = (sunLocation)
            getWorldWindow().requestRedraw()
        }

        if (!activityPaused) {
            // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this)
        }

        lastFrameTimeNanos = frameTimeNanos
    }

    override fun onPause() {
        super.onPause()
        activityPaused = true
        lastFrameTimeNanos = 0
    }

    override fun onResume() {
        super.onResume()
        // Resume the day-night cycle animation.
        activityPaused = false
        lastFrameTimeNanos = 0
        Choreographer.getInstance().postFrameCallback(this)
    }

}