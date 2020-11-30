package com.atom.map.controller

import com.atom.map.WorldWind
import com.atom.map.geom.Camera
import com.atom.map.geom.Location
import com.atom.map.gesture.GestureRecognizer
import com.atom.map.gesture.PinchRecognizer
import com.atom.map.gesture.RotationRecognizer
import com.atom.map.util.WWMath

open class CustomWorldWindowCameraController : BasicWorldWindowController() {

    protected var camera: Camera =
        Camera()

    protected var beginCamera: Camera =
        Camera()

    override fun handlePan(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val dx: Float = recognizer.translationX
        val dy: Float = recognizer.translationY
        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
            lastX = 0f
            lastY = 0f
        } else if (state == WorldWind.CHANGED) {
            var lat: Double = camera.latitude
            var lon: Double = camera.longitude
            val alt: Double = camera.altitude

            val metersPerPixel: Double = wwd.pixelSizeAtDistance(alt)
            val forwardMeters = (dy - lastY) * metersPerPixel
            val sideMeters = -(dx - lastX) * metersPerPixel
            lastX = dx
            lastY = dy
            val globeRadius: Double = wwd.globe().getRadiusAt(lat, lon)
            val forwardDegrees = Math.toDegrees(forwardMeters / globeRadius)
            val sideDegrees = Math.toDegrees(sideMeters / globeRadius)
            // Adjust the change in latitude and longitude based on the navigator's heading.
            val heading: Double = camera.heading
            val headingRadians = Math.toRadians(heading)
            val sinHeading = Math.sin(headingRadians)
            val cosHeading = Math.cos(headingRadians)
            lat += forwardDegrees * cosHeading - sideDegrees * sinHeading
            lon += forwardDegrees * sinHeading + sideDegrees * cosHeading
            // If the navigator has panned over either pole, compensate by adjusting the longitude and heading to move
            // the navigator to the appropriate spot on the other side of the pole.
            if (lat < -90 || lat > 90) {
                camera.latitude = Location.normalizeLatitude(lat)
                camera.longitude = Location.normalizeLongitude(lon + 180)
            } else if (lon < -180 || lon > 180) {
                camera.latitude = lat
                camera.longitude = Location.normalizeLongitude(lon)
            } else {
                camera.latitude = lat
                camera.longitude = lon
            }
            //this.camera.heading = WWMath.normalizeAngle360(heading + sideDegrees * 1000);
            wwd.navigator().setAsCamera(wwd.globe(), camera)
            wwd.requestRedraw()
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    override fun handlePinch(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val scale: Float = (recognizer as PinchRecognizer).scale()
        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
        } else if (state == WorldWind.CHANGED) {
            if (scale != 0f) { // Apply the change in scale to the navigator, relative to when the gesture began.
                camera.altitude = camera.altitude + if (scale > 1) scale * 1000 else -1 / scale * 1000
                this.applyLimits(camera)
                wwd.navigator().setAsCamera(wwd.globe(), camera)
                wwd.requestRedraw()
            }
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    override fun handleRotate(recognizer: GestureRecognizer) {
        val wwd = this.world ?: return
        val state: Int = recognizer.state
        val rotation: Float = (recognizer as RotationRecognizer).rotation()
        if (state == WorldWind.BEGAN) {
            gestureDidBegin()
            lastRotation = 0f
        } else if (state == WorldWind.CHANGED) { // Apply the change in rotation to the navigator, relative to the navigator's current values.
            val headingDegrees = lastRotation - rotation.toDouble()
            camera.heading = WWMath.normalizeAngle360(camera.heading + headingDegrees)
            lastRotation = rotation
            wwd.navigator().setAsCamera(wwd.globe(), camera)
            wwd.requestRedraw()
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    override fun handleTilt(recognizer: GestureRecognizer) {
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
            camera.heading = WWMath.normalizeAngle360(beginCamera.heading + headingDegrees)
            camera.tilt = beginCamera.tilt + tiltDegrees
            wwd.navigator().setAsCamera(wwd.globe(), camera)
            wwd.requestRedraw()
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            gestureDidEnd()
        }
    }

    override fun gestureDidBegin() {
        val wwd = this.world ?: return
        if (activeGestures++ == 0) {
            wwd.navigator().getAsCamera(wwd.globe(), beginCamera)
            camera.set(beginCamera)
        }
    }

    protected fun applyLimits(camera: Camera) {
        val wwd = this.world ?: return
        val distanceToExtents: Double = wwd.distanceToViewGlobeExtents()
        val minRange = 100.0
        val maxRange = distanceToExtents * 2
        camera.altitude = WWMath.clamp(camera.altitude, minRange, maxRange)
    }
}