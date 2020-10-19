package com.atom.wyz

import android.os.Bundle
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import com.atom.wyz.BasicWorldWindActivity
import com.atom.map.navigator.Navigator
import com.atom.map.geom.Location
import com.atom.map.layer.AtmosphereLayer
import com.atom.map.layer.LayerList

class DayNightCycleActivity : BasicWorldWindActivity(), FrameCallback {

    protected var sunLocation: Location = Location(0.0, -100.0)

    protected var atmosphereLayer: AtmosphereLayer? = null

    protected var cameraDegreesPerSecond = 2.0

    protected var lightDegreesPerSecond = 6.0

    protected var activityPaused = false

    protected var lastFrameTimeNanos: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the Atmosphere layer's light location to our custom location. By default the light location is
        // always behind the viewer.
        val layers: LayerList = getWorldWindow().layers
        atmosphereLayer = layers.getLayer(layers.indexOfLayerNamed("Atmosphere")) as AtmosphereLayer
        atmosphereLayer!!.lightLocation = sunLocation

        // Initialize the Navigator so that the sun is behind the viewer.
        val navigator: Navigator = getWorldWindow().navigator
        navigator.latitude = (20.0)
        navigator.longitude = (sunLocation.longitude)

        // Use this Activity's Choreographer to animate the day-night cycle.
        Choreographer.getInstance().postFrameCallback(this)

    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) { // Compute the frame duration in seconds.
            val frameDurationSeconds = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-9
            val cameraDegrees = frameDurationSeconds * cameraDegreesPerSecond
            val lightDegrees = frameDurationSeconds * lightDegreesPerSecond
            // Move the navigator to simulate the Earth's rotation about its axis.
            val navigator = getWorldWindow().navigator
            navigator.longitude = (navigator.longitude - cameraDegrees)
            // Move the sun location to simulate the Sun's rotation about the Earth.
            sunLocation.set(sunLocation.latitude, sunLocation.longitude - lightDegrees)
            atmosphereLayer?.lightLocation = (sunLocation)
            // Redraw the World Window to display the above changes.
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