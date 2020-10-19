package com.atom.wyz.worldwind.controller

import android.view.MotionEvent
import com.atom.wyz.worldwind.WorldHelper
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.observer.LookAt
import com.atom.wyz.worldwind.gesture.*
import com.atom.wyz.worldwind.util.WWMath
import java.util.*

open class BasicWorldWindowController : WorldWindowController, GestureListener {


    override var world: WorldHelper? = null

    protected var lastX = 0f

    protected var lastY = 0f

    protected var lastRotation = 0f

    protected var lookAt: LookAt =
        LookAt()

    protected var beginLookAt: LookAt =
        LookAt()

    protected var activeGestures = 0

    protected var panRecognizer = PanRecognizer()

    protected var pinchRecognizer = PinchRecognizer()

    protected var rotationRecognizer = RotationRecognizer()

    protected var tiltRecognizer = PanRecognizer()

    protected var allRecognizers = Arrays.asList(
        panRecognizer, pinchRecognizer, rotationRecognizer, tiltRecognizer
    )


    constructor() {
        panRecognizer.addListener(this)
        pinchRecognizer.addListener(this)
        rotationRecognizer.addListener(this)
        tiltRecognizer.addListener(this)

        tiltRecognizer.minNumberOfPointers = (4)
        panRecognizer.maxNumberOfPointers = (2)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        for (idx in 0 until allRecognizers.size) {
            handled = handled or allRecognizers[idx].onTouchEvent(event)
        }
        return handled
    }

    protected open fun handlePan(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val dx: Float = recognizer.translationX
        val dy: Float = recognizer.translationY


        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin()
            lastX = 0f
            lastY = 0f
        } else if (state == WorldWind.CHANGED) {
            var lat = lookAt.latitude
            var lon = lookAt.longitude
            val rng = lookAt.range

            val metersPerPixel: Double = wwd.pixelSizeAtDistance(rng)

            val forwardMeters = (dy - lastY) * metersPerPixel
            val sideMeters = -(dx - lastX) * metersPerPixel

            lastX = dx
            lastY = dy

            val globeRadius: Double = wwd.globe().getRadiusAt(lat, lon)

            val forwardDegrees = Math.toDegrees(forwardMeters / globeRadius)
            val sideDegrees = Math.toDegrees(sideMeters / globeRadius)

            val heading: Double = this.lookAt.heading;
            val headingRadians = Math.toRadians(heading)
            val sinHeading = Math.sin(headingRadians)
            val cosHeading = Math.cos(headingRadians)

            lat += forwardDegrees * cosHeading - sideDegrees * sinHeading
            lon += forwardDegrees * sinHeading + sideDegrees * cosHeading

            if (lat < -90 || lat > 90) {
                this.lookAt.latitude = Location.normalizeLatitude(lat)
                this.lookAt.longitude = Location.normalizeLongitude(lon + 180)
                this.lookAt.heading = WWMath.normalizeAngle360(heading + 180)
            } else if (lon < -180 || lon > 180) {
                this.lookAt.latitude = lat
                this.lookAt.longitude = Location.normalizeLongitude(lon)
            } else {
                this.lookAt.latitude = lat
                this.lookAt.longitude = lon
            }

            wwd.navigator().setAsLookAt(wwd.globe(), lookAt)
            wwd.requestRedraw()
            lastX = dx
            lastY = dy
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    protected open fun handlePinch(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val scale: Float = (recognizer as PinchRecognizer).scale()

        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
        } else if (state == WorldWind.CHANGED) {
            if (scale != 0f) { // Apply the change in pinch scale to this navigator's altitude, relative to the altitude when the
                // Apply the change in scale to the navigator, relative to when the gesture began.
                this.lookAt.range = beginLookAt.range / scale
                this.applyLimits(lookAt)
                wwd.navigator().setAsLookAt(wwd.globe(), lookAt)
                wwd.requestRedraw()
            }
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    protected open fun handleRotate(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val rotation: Float = (recognizer as RotationRecognizer).rotation()

        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
            lastRotation = 0f
        } else if (state == WorldWind.CHANGED) {

            val headingDegrees = lastRotation - rotation.toDouble()
            lookAt.heading = WWMath.normalizeAngle360(lookAt.heading + headingDegrees)
            lastRotation = rotation

            wwd.navigator().setAsLookAt(wwd.globe(), lookAt)
            wwd.requestRedraw()
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    protected open fun handleTilt(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val dx: Float = recognizer.translationX
        val dy: Float = recognizer.translationY
        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
            lastRotation = 0f
        } else if (state == WorldWind.CHANGED) { // Apply the change in tilt to the navigator, relative to when the gesture began.
            val headingDegrees: Double = 180 * dx / wwd.getWidth().toDouble()
            val tiltDegrees: Double = -180 * dy / wwd.getHeight().toDouble()

            lookAt.heading = WWMath.normalizeAngle360(beginLookAt.heading + headingDegrees)

            lookAt.tilt = beginLookAt.tilt + tiltDegrees
            this.applyLimits(lookAt)

            wwd.navigator().setAsLookAt(wwd.globe(), lookAt)
            wwd.requestRedraw()
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    protected open fun applyLimits(lookAt: LookAt) {
        val wwd = this.world ?: return
        val distanceToExtents: Double = wwd.distanceToViewGlobeExtents()
        val minRange = 10.0
        val maxRange = distanceToExtents * 2
        lookAt.range = WWMath.clamp(lookAt.range, minRange, maxRange)
//        val minTiltRange = distanceToExtents * 0.1
//        val maxTiltRange = distanceToExtents * 0.9
//        val tiltAmount: Double = WWMath.clamp((lookAt.range - minTiltRange) / (maxTiltRange - minTiltRange), 0.0, 1.0)
        val maxTilt = 80.0
        lookAt.tilt = WWMath.clamp(lookAt.tilt, 0.0, maxTilt) //* (1 - tiltAmount)
    }


    override fun gestureStateChanged(event: MotionEvent, recognizer: GestureRecognizer) {
        if (recognizer === panRecognizer) {
            handlePan(recognizer)
        } else if (recognizer === pinchRecognizer) {
            handlePinch(recognizer)
        } else if (recognizer === rotationRecognizer) {
            handleRotate(recognizer)
        } else if (recognizer === tiltRecognizer) {
            this.handleTilt(recognizer)
        }
    }

    protected open fun gestureDidBegin() {
        if (activeGestures++ == 0) {
            this.world?.navigator()?.getAsLookAt(this.world!!.globe(), beginLookAt)
            lookAt.set(beginLookAt)
        }
    }

    protected open fun gestureDidEnd() {
        if (activeGestures > 0) { // this should always be the case, but we check anyway
            activeGestures--
        }
    }
}