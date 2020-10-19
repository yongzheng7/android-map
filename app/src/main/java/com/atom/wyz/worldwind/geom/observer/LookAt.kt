package com.atom.wyz.worldwind.geom.observer

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val lookAt = other as LookAt
        if (java.lang.Double.compare(lookAt.latitude, latitude) != 0) return false
        if (java.lang.Double.compare(lookAt.longitude, longitude) != 0) return false
        if (java.lang.Double.compare(lookAt.altitude, altitude) != 0) return false
        if (altitudeMode != lookAt.altitudeMode) return false
        if (java.lang.Double.compare(lookAt.range, range) != 0) return false
        if (java.lang.Double.compare(lookAt.heading, heading) != 0) return false
        return if (java.lang.Double.compare(lookAt.tilt, tilt) != 0) false else java.lang.Double.compare(
            lookAt.roll,
            roll
        ) == 0
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(latitude)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(altitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + altitudeMode
        temp = java.lang.Double.doubleToLongBits(range)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(heading)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(tilt)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(roll)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
}