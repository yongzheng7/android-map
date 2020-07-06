package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.util.Logger

/**
 * 观察者点位 角度 位置 倾斜度 等 导航器
 * 并初始化换算程模型变换矩阵
 */
class Navigator  {

     var scratchCamera: Camera = Camera()

     var latitude = 0.0

     var longitude = 0.0

     var altitude = 0.0

     var heading = 0.0

     var tilt = 0.0

     var roll = 0.0

     fun getAsCamera(globe: Globe?, result: Camera?): Camera {
        if (globe == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingGlobe"))
        }

        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingResult"))
        }

        result.latitude = this.latitude
        result.longitude = this.longitude
        result.altitude = this.altitude
        result.altitudeMode = WorldWind.ABSOLUTE
        result.heading = this.heading
        result.tilt = this.tilt
        result.roll = this.roll


        return result
    }

     fun setAsCamera(globe: Globe?, camera: Camera?): Navigator {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingGlobe"))
        }

        if (camera == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingCamera"))
        }

        this.latitude = camera.latitude
        this.longitude = camera.longitude
        this.altitude = camera.altitude // TODO interpret altitude modes other than absolute

        this.heading = camera.heading
        this.tilt = camera.tilt
        this.roll = camera.roll


        return this
    }

    
     fun getAsLookAt(globe: Globe?, result: LookAt?): LookAt {
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

    
     fun setAsLookAt(globe: Globe?, lookAt: LookAt?): Navigator {
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