package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger

class LookAt {

    var latitude = 0.0

    var longitude = 0.0

    var altitude = 0.0

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE
    /**
     *
     */
    var range = 0.0

    var heading = 0.0

    var tilt = 0.0

    var roll = 0.0

    constructor()

    constructor(latitude: Double, longitude: Double, altitude: Double, @WorldWind.AltitudeMode altitudeMode: Int, range: Double, heading: Double, tilt: Double,roll : Double ) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.altitudeMode = altitudeMode
        this.range = range
        this.heading = heading
        this.tilt = tilt
        this.roll = roll
    }

    fun LookAt(lookAt: LookAt?) {
        if (lookAt == null) {
            throw java.lang.IllegalArgumentException(
                   Logger.logMessage(Logger.ERROR, "LookAt", "constructor", "missingLookAt"))
        }
        latitude = lookAt.latitude
        longitude = lookAt.longitude
        altitude = lookAt.altitude
        altitudeMode = lookAt.altitudeMode
        range = lookAt.range
        heading = lookAt.heading
        tilt = lookAt.tilt
        roll = lookAt.roll
    }

    operator fun set(latitude: Double, longitude: Double, altitude: Double, @WorldWind.AltitudeMode altitudeMode: Int, range: Double, heading: Double, tilt: Double, roll: Double ): LookAt {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.altitudeMode = altitudeMode
        this.range = range
        this.heading = heading
        this.tilt = tilt
        this.roll = roll
        return this
    }

    fun set(lookAt: LookAt?): LookAt {
        if (lookAt == null) {
            throw IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LookAt", "set", "missingLookAt"))
        }
        latitude = lookAt.latitude
        longitude = lookAt.longitude
        altitude = lookAt.altitude
        altitudeMode = lookAt.altitudeMode
        range = lookAt.range
        heading = lookAt.heading
        tilt = lookAt.tilt
        roll = lookAt.roll
        return this
    }

    override fun toString(): String {
        return "LookAt{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", altitudeMode=" + altitudeMode +
                ", range=" + range +
                ", heading=" + heading +
                ", tilt=" + tilt +
                ", roll=" + roll +
                '}'
    }
}