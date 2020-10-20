package com.atom.map.geom.observer

import com.atom.map.WorldWind

class Camera {

    var latitude = 0.0

    var longitude = 0.0

    var altitude = 0.0

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE
    /**
     * 与y轴的角度
     */
    var heading = 0.0
    /**
     * 倾斜度
     * 正面
     * 平行与手机摄像头方向默认为Y
     * 垂直手机Y的方向和手机短边平行默认为X
     * 垂直手机的屏幕为Z
     * 目标是X
     */
    var tilt = 0.0
    /**
     * 滚动针对Z粥自转
     */
    var roll = 0.0

    constructor()

    constructor(latitude: Double, longitude: Double, altitude: Double, @WorldWind.AltitudeMode altitudeMode: Int,
               heading: Double, tilt: Double, roll: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.altitudeMode = altitudeMode
        this.heading = heading
        this.tilt = tilt
        this.roll = roll
    }

    constructor(camera: Camera) {
        latitude = camera.latitude
        longitude = camera.longitude
        altitude = camera.altitude
        altitudeMode = camera.altitudeMode
        heading = camera.heading
        tilt = camera.tilt
        roll = camera.roll
    }

    operator fun set(latitude: Double, longitude: Double, altitude: Double, @WorldWind.AltitudeMode altitudeMode: Int,
                     heading: Double, tilt: Double, roll: Double): Camera {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.altitudeMode = altitudeMode
        this.heading = heading
        this.tilt = tilt
        this.roll = roll
        return this
    }

    fun set(camera: Camera): Camera {
        latitude = camera.latitude
        longitude = camera.longitude
        altitude = camera.altitude
        altitudeMode = camera.altitudeMode
        heading = camera.heading
        tilt = camera.tilt
        roll = camera.roll
        return this
    }

    override fun toString(): String {
        return "Camera{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", altitudeMode=" + altitudeMode +
                ", heading=" + heading +
                ", tilt=" + tilt +
                ", roll=" + roll +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val camera: Camera = other as Camera
        if (camera.latitude.compareTo(latitude) != 0) return false
        if (camera.longitude.compareTo(longitude) != 0) return false
        if (camera.altitude.compareTo(altitude) != 0) return false
        if (altitudeMode != camera.altitudeMode) return false
        if (camera.heading.compareTo(heading) != 0) return false
        return if (camera.tilt.compareTo(tilt) != 0) false else camera.roll.compareTo(roll) == 0
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
        temp = java.lang.Double.doubleToLongBits(heading)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(tilt)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(roll)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
}