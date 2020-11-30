package com.atom.wyz.changelookat

import android.os.Bundle
import com.atom.map.WorldWind
import com.atom.map.geom.Position
import com.atom.map.geom.LookAt
import com.atom.map.globe.Globe
import com.atom.wyz.base.BasicWorldWindActivity

class LookAtViewActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val aircraft: Position = Position(34.0158333, -118.4513056, 2500.0) // Aircraft above Santa Monica airport, altitude in meters

        val airport: Position = Position(33.9424368, -118.4081222, 38.7) // LAX airport, Los Angeles CA, altitude MSL


        val globe: Globe = getWorldWindow().globe
        val heading: Double = aircraft.greatCircleAzimuth(airport)
        val distanceRadians: Double = aircraft.greatCircleDistance(airport)
        val distance: Double = distanceRadians * globe.getRadiusAt(aircraft.latitude, aircraft.longitude)

        val altitude: Double = aircraft.altitude - airport.altitude
        val range = Math.sqrt(altitude * altitude + distance * distance)
        val tilt = Math.toDegrees(Math.atan(distance / aircraft.altitude))


        val lookAt: LookAt =
            LookAt()
        lookAt.set(airport.latitude, airport.longitude, airport.altitude, WorldWind.ABSOLUTE, range, heading, tilt, 0.0)
        getWorldWindow().navigator.setAsLookAt(globe, lookAt)
    }
}