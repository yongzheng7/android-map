package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.util.Logger

/**
 * 观察者点位 角度 位置 倾斜度 等 导航器
 * 并初始化换算程模型变换矩阵
 */
class BasicNavigator() : Navigator {

    protected var scratchCamera: Camera = Camera()

    protected var _fieldOfView = 45.0

    protected var _latitude = 0.0

    protected var _longitude = 0.0

    protected var _altitude = 0.0

    protected var _heading = 0.0

    protected var _tilt = 0.0

    protected var _roll = 0.0

    @Synchronized
    override fun getLatitude(): Double {
        return _latitude
    }

    @Synchronized
    override fun setLatitude(latitude: Double): Navigator {
        _latitude = latitude
        return this
    }

    @Synchronized
    override fun getLongitude(): Double {
        return _longitude
    }

    @Synchronized
    override fun setLongitude(longitude: Double): Navigator {
        _longitude = longitude
        return this
    }

    @Synchronized
    override fun getAltitude(): Double {
        return _altitude
    }

    @Synchronized
    override fun setAltitude(altitude: Double): Navigator {
        _altitude = altitude
        return this
    }

    @Synchronized
    override fun getHeading(): Double {
        return _heading
    }

    @Synchronized
    override fun setHeading(headingDegrees: Double): Navigator {
        _heading = headingDegrees
        return this
    }

    @Synchronized
    override fun getTilt(): Double {
        return _tilt
    }

    @Synchronized
    override fun setTilt(tiltDegrees: Double): Navigator {
        _tilt = tiltDegrees
        return this
    }

    @Synchronized
    override fun getRoll(): Double {
        return _roll
    }

    @Synchronized
    override fun setRoll(rollDegrees: Double): Navigator {
        _roll = rollDegrees
        return this
    }

    @Synchronized
    override fun getFieldOfView(): Double {
        return _fieldOfView
    }

    @Synchronized
    override fun setFieldOfView(fovyDegrees: Double): Navigator {
        _fieldOfView = fovyDegrees
        return this
    }

    @Synchronized
    override fun getAsCamera(globe: Globe?, result: Camera?): Camera {
        if (globe == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingGlobe"))
        }

        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingResult"))
        }

        result.latitude = this._latitude
        result.longitude = this._longitude
        result.altitude = this._altitude
        result.altitudeMode = WorldWind.ABSOLUTE
        result.heading = this._heading
        result.tilt = this._tilt
        result.roll = this._roll

        return result
    }

    @Synchronized
    override fun setAsCamera(globe: Globe?, camera: Camera?): Navigator {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingGlobe"))
        }

        if (camera == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingCamera"))
        }

        this._latitude = camera.latitude
        this._longitude = camera.longitude
        this._altitude = camera.altitude // TODO interpret altitude modes other than absolute

        this._heading = camera.heading
        this._tilt = camera.tilt
        this._roll = camera.roll


        return this
    }

    @Synchronized
    override fun getAsLookAt(globe: Globe?, result: LookAt?): LookAt {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsLookAt", "missingGlobe"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsLookAt", "missingResult"))
        }
        getAsCamera(globe, this.scratchCamera)
        globe.cameraToLookAt(this.scratchCamera, result)

        return result
    }

    @Synchronized
    override fun setAsLookAt(globe: Globe?, lookAt: LookAt?): Navigator {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingGlobe"))
        }

        if (lookAt == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingLookAt"))
        }
        globe.lookAtToCamera(lookAt, scratchCamera)
        setAsCamera(globe, scratchCamera) // TODO convert altitudeMode to absolute if necessary

        return this
    }
}