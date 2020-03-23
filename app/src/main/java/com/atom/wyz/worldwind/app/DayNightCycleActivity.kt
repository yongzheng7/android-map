package com.atom.wyz.worldwind.app

import android.os.Bundle
import android.os.Handler
import com.atom.wyz.worldwind.Navigator
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.layer.AtmosphereAndGroundLayer
import com.atom.wyz.worldwind.layer.LayerList

class DayNightCycleActivity : BasicWorldWindActivity() ,  Runnable {

    protected var sunLocation: Location = Location(0.0, -100.0)

    protected var atmosphereLayer: AtmosphereAndGroundLayer? = null

    protected var dayNightHandler = Handler()

    protected var pauseHandler = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the Atmosphere layer's light location to our custom location. By default the light location is
        // always behind the viewer.
        val layers: LayerList = getWorldWindow().layers
        atmosphereLayer = layers.getLayer(layers.indexOfLayerNamed("Atmosphere")) as AtmosphereAndGroundLayer
        atmosphereLayer!!.lightLocation = sunLocation

        // Initialize the Navigator so that the sun is behind the viewer.
        val navigator: Navigator = getWorldWindow().navigator
        navigator.setLatitude(20.0)
        navigator.setLongitude(sunLocation.longitude)

        // Set up an Android Handler to change the day-night cycle.
        dayNightHandler.postDelayed(this, 500)
    }
    override fun run() {

        val navigator: Navigator = getWorldWindow().navigator
        navigator.setLongitude(navigator.getLongitude() - 0.03)

        sunLocation.set(sunLocation.latitude, sunLocation.longitude - 0.1)
        atmosphereLayer!!.lightLocation = (sunLocation)

        getWorldWindow().requestRender()

        if (!pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            dayNightHandler.postDelayed(this, 30)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseHandler = true
    }

    override fun onResume() {
        super.onResume()
        pauseHandler = false
        dayNightHandler.postDelayed(this, 500)
    }
}