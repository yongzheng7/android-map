package com.atom.wyz.worldwind.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.InputEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.atom.wyz.worldwind.*
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.navigator.Navigator
import com.atom.wyz.worldwind.navigator.NavigatorEvent
import com.atom.wyz.worldwind.navigator.NavigatorListener

@SuppressLint("Registered")
class NavigatorEventActivity : BasicWorldWindActivity() , FrameCallback {
    protected var latView: TextView? = null

    protected var lonView: TextView? = null

    protected var altView: TextView? = null

    private val lookAt: LookAt = LookAt()

    private val camera: Camera = Camera()

    protected var crosshairs: ImageView? = null

    protected var overlay: ViewGroup? = null

    private var lastEventTime: Long = 0

    private var animatorSet: AnimatorSet? = null

    private var crosshairsActive = false

    // Globe rotation onFrame animation settings
    private val currentLocation: Location = Location()

    private val targetLocation: Location = Location()

    private var lastLocation // lazily allocated
            : Location? = null

    private var radiansPerMillisecond = 0.0

    private var azimuth = 0.0

    private val COAST_DURATION_MILLIS = 3000.0

    private var coastTimeRemainingMillis = 0.0

    private var lastFrameTimeNanos: Long = 0

    private val coasting = false

    // Track the state of this activity to start/stope the globe animation
    private var activityPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crosshairs = findViewById(R.id.globe_crosshairs) as ImageView
        overlay = findViewById(R.id.globe_status) as ViewGroup
        crosshairs!!.visibility = View.VISIBLE
        overlay!!.visibility = View.VISIBLE

        latView = findViewById(R.id.lat_value) as TextView
        lonView = findViewById(R.id.lon_value) as TextView
        altView = findViewById(R.id.alt_value) as TextView

        val fadeOut = ObjectAnimator.ofFloat(crosshairs, "alpha", 0f).setDuration(1500)
        fadeOut.startDelay = 500
        animatorSet = AnimatorSet()
        animatorSet?.play(fadeOut)

        val listener: NavigatorListener = object :
            NavigatorListener {
            override fun onNavigatorEvent(wwd: WorldWindow, event: NavigatorEvent) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastEventTime

                val eventAction: Int = event.action
                val receivedUserInput =
                    eventAction == WorldWind.NAVIGATOR_MOVED && event.lastInputEvent != null
                val lastInputEvent= event.lastInputEvent

                if (eventAction == WorldWind.NAVIGATOR_STOPPED || elapsedTime > 50) { // Get the current navigator state
                    updateOverlayContents(lookAt, camera)
                    updateOverlayColor(eventAction , lastInputEvent)

                    updateInertiaSettings(lookAt, elapsedTime)
                    lastEventTime = currentTime
                }
                if (receivedUserInput) {
                    showCrosshairs()
                } else {
                    fadeCrosshairs()
                }
            }
        }
        getWorldWindow().addNavigatorListener(listener)
    }

    /**
     * Updates the settings for user gesture inertia.
     *
     * @param lookAt        The current lookAt
     * @param elapsedTimeMs The time elapsed since the last user action
     */
    protected fun updateInertiaSettings(lookAt: LookAt, elapsedTimeMs: Long) {
        if (lastLocation == null) {
            lastLocation = Location(lookAt.latitude, lookAt.longitude)
        }
        // Compute the direction used for the coasting inertia
        currentLocation.set(lookAt.latitude, lookAt.longitude)
        azimuth = lastLocation!!.greatCircleAzimuth(currentLocation)
        // Compute the velocity used for the coasting
        radiansPerMillisecond = lastLocation!!.greatCircleDistance(currentLocation) / elapsedTimeMs
        // Reset the coasting period on each user action
        coastTimeRemainingMillis = COAST_DURATION_MILLIS
        lastLocation!!.set(currentLocation)
    }

    /**
     * Makes the crosshairs visible.
     */
    protected fun showCrosshairs() {
        if (animatorSet!!.isStarted) {
            animatorSet!!.cancel()
        }
        crosshairs!!.alpha = 1.0f
        crosshairsActive = true
    }

    /**
     * Fades the crosshairs using animation.
     */
    protected fun fadeCrosshairs() {
        if (crosshairsActive) {
            crosshairsActive = false
            if (!animatorSet!!.isStarted) {
                animatorSet!!.start()
            }
        }
    }

    /**
     * Displays navigator state information in the status overlay views.
     *
     * @param lookAt Where the navigator is looking
     * @param camera Where the camera is positioned
     */
    protected fun updateOverlayContents(lookAt: LookAt, camera: Camera) {
        latView!!.text = formatLatitude(lookAt.latitude)
        lonView!!.text = formatLongitude(lookAt.longitude)
        //altView!!.text = formatAltitude(camera.altitude)
        altView!!.text = formatElevation(camera.altitude)
    }

    /**
     * Brightens the colors of the overlay views when when user input occurs.
     *
     * @param eventAction    The action associated with this navigator event
     * @param lastInputEvent The last user input event; will be null if no user input was detected during this navigator
     * event
     */
    protected fun updateOverlayColor(@WorldWind.NavigatorAction eventAction: Int, lastInputEvent: InputEvent?) {
        val color =
            if (eventAction == WorldWind.NAVIGATOR_STOPPED) -0x5f000100 /*semi-transparent yellow*/ else Color.YELLOW
        latView!!.setTextColor(color)
        lonView!!.setTextColor(color)
        altView!!.setTextColor(color)
    }

    protected fun formatLatitude(latitude: Double): String? {
        val sign = Math.signum(latitude).toInt()
        return String.format("%6.3f°%s", latitude * sign, if (sign >= 0.0) "N" else "S")
    }

    protected fun formatLongitude(longitude: Double): String? {
        val sign = Math.signum(longitude).toInt()
        return String.format("%7.3f°%s", longitude * sign, if (sign >= 0.0) "E" else "W")
    }

    protected fun formatAltitude(altitude: Double): String {
        return String.format(
            "Eye: %,.0f %s",
            if (altitude < 100000) altitude else altitude / 1000,
            if (altitude < 100000) "m" else "km"
        )
    }
    protected fun formatElevation(elevation: Double): String {
        return String.format(
            "Elev: %,.0f %s",
            if (elevation < 100000) elevation else elevation / 1000,
            if (elevation < 100000) "m" else "km"
        )
    }

    override fun onPause() {
        super.onPause()
        // Stop running the globe rotation animation when this activity is activityPaused.
        // Stop running the globe rotation animation when this activity is activityPaused.
        activityPaused = true
        lastFrameTimeNanos = 0
    }
    override fun onResume() {
        super.onResume()
        // Resume the globe rotation animation
        activityPaused = false
        lastFrameTimeNanos = 0
        Choreographer.getInstance().postFrameCallback(this)
    }
    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) { // Compute the frame duration in milliseconds.
            val frameDurationMillis = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-6
            // Move the navigator to simulate inertia from the user's last move gesture
            if (coastTimeRemainingMillis > 0) {
                val navigator: Navigator = getWorldWindow().navigator
                // Compute the distance to move in this frame
                val distanceRadians = radiansPerMillisecond * frameDurationMillis
                currentLocation.set(navigator.latitude, navigator.longitude)
                currentLocation.greatCircleLocation(azimuth, distanceRadians, targetLocation)
                navigator.latitude = (targetLocation.latitude)
                navigator.longitude = (targetLocation.longitude)
                // Dampen the inertia
                coastTimeRemainingMillis -= frameDurationMillis
                if (coastTimeRemainingMillis > 0) {
                    radiansPerMillisecond *= coastTimeRemainingMillis / COAST_DURATION_MILLIS
                }
                // Redraw the World Window to display the above changes.
                getWorldWindow().requestRedraw()
            }
        }

        if (!activityPaused) { // stop animating when this Activity is activityPaused
            Choreographer.getInstance().postFrameCallback(this)
        }

        lastFrameTimeNanos = frameTimeNanos
    }
}