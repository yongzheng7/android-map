package com.atom.wyz.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.geom.observer.Camera
import com.atom.map.layer.AtmosphereLayer
import com.atom.map.layer.BackgroundLayer
import com.atom.map.layer.BlueMarbleLandsatLayer
import com.atom.wyz.worldwind.R

@SuppressLint("Registered")
open class BasicWorldWindActivity : AppCompatActivity() {

    companion object {
        protected const val CAMERA_LATITUDE = "latitude"

        protected const val CAMERA_LONGITUDE = "longitude"

        protected const val CAMERA_ALTITUDE = "altitude"

        protected const val CAMERA_ALTITUDE_MODE = "altitude_mode"

        protected const val CAMERA_HEADING = "heading"

        protected const val CAMERA_TILT = "tilt"

        protected const val CAMERA_ROLL = "roll"
    }

    protected var savedInstanceState: Bundle? = null
    private lateinit var wwd: WorldWindow
    protected open var layoutResourceId: Int = R.layout.activity_main
    protected open fun getWorldWindow(): WorldWindow {
        return wwd
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        setContentView(this.layoutResourceId)
        wwd = WorldWindow(this)
        val globeLayout = findViewById<FrameLayout>(R.id.globe)
        globeLayout.addView(wwd)
        wwd.layers.addLayer(BackgroundLayer())
        wwd.layers.addLayer(BlueMarbleLandsatLayer())
        wwd.layers.addLayer(AtmosphereLayer())
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) { // Save the WorldWindow's current navigator state
        this.saveNavigatorState(savedInstanceState)
        super.onSaveInstanceState(savedInstanceState)
    }

    protected open fun saveNavigatorState(savedInstanceState: Bundle) {
        val wwd = getWorldWindow()
        val camera = wwd.navigator.getAsCamera(wwd.globe,
            Camera()
        )
        // Write the camera data
        savedInstanceState.putDouble(CAMERA_LATITUDE, camera.latitude)
        savedInstanceState.putDouble(CAMERA_LONGITUDE, camera.longitude)
        savedInstanceState.putDouble(CAMERA_ALTITUDE, camera.altitude)
        savedInstanceState.putDouble(CAMERA_HEADING, camera.heading)
        savedInstanceState.putDouble(CAMERA_TILT, camera.tilt)
        savedInstanceState.putDouble(CAMERA_ROLL, camera.roll)
        savedInstanceState.putInt(CAMERA_ALTITUDE_MODE, camera.altitudeMode)
    }

    protected open fun restoreNavigatorState(savedInstanceState: Bundle) {
        val wwd = getWorldWindow()
        val lat =
            savedInstanceState.getDouble(CAMERA_LATITUDE)
        val lon =
            savedInstanceState.getDouble(CAMERA_LONGITUDE)
        val alt =
            savedInstanceState.getDouble(CAMERA_ALTITUDE)
        val heading =
            savedInstanceState.getDouble(CAMERA_HEADING)
        val tilt = savedInstanceState.getDouble(CAMERA_TILT)
        val roll = savedInstanceState.getDouble(CAMERA_ROLL)
        @WorldWind.AltitudeMode val altMode =
            savedInstanceState.getInt(CAMERA_ALTITUDE_MODE)
        // Restore the camera state.
        val camera = Camera(
            lat,
            lon,
            alt,
            altMode,
            heading,
            tilt,
            roll
        )
        wwd.navigator.setAsCamera(wwd.globe, camera)
    }

    override fun onStart() {
        super.onStart()
        savedInstanceState?.let { this.restoreNavigatorState(it) }
    }

    override fun onPause() {
        super.onPause()
        wwd.onPause() // pauses the rendering thread
    }

    override fun onResume() {
        super.onResume()
        wwd.onResume() // resumes a paused rendering thread
    }
}