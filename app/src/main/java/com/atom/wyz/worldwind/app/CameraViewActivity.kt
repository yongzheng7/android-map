package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.globe.Globe

class CameraViewActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val aircraft: Position = Position(34.2, -119.2, 3000.0) // Above Oxnard CA, altitude in meters

        val airport: Position = Position(34.1192744, -119.1195850, 4.0) // KNTD airport, Point Mugu CA, altitude MSL

        val globe: Globe = getWorldWindow().globe

        val heading: Double = aircraft.greatCircleAzimuth(airport)
        val distanceRadians: Double = aircraft.greatCircleDistance(airport)
        val distance: Double = distanceRadians * globe.getRadiusAt(aircraft.latitude, aircraft.longitude)
        val tilt = Math.toDegrees(Math.atan(distance / aircraft.altitude))

        val camera: Camera = Camera()

        camera.set(aircraft.latitude, aircraft.longitude, aircraft.altitude, WorldWind.ABSOLUTE, heading, tilt, 0.0) // No roll

        getWorldWindow().navigator.setAsCamera(globe, camera)
    }
}