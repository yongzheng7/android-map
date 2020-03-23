package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger

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

    fun Camera(camera: Camera?) {
        if (camera == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Camera", "constructor", "missingCamera"))
        }
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

    fun set(camera: Camera?):Camera  {
        if (camera == null) {
            throw IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Camera", "set", "missingCamera"))
        }
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
}