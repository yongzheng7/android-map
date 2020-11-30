package com.atom.map.navigator

import com.atom.map.WorldWind
import com.atom.map.geom.*
import com.atom.map.geom.Camera
import com.atom.map.geom.LookAt
import com.atom.map.globe.Globe

/**
 * 观察者点位 角度 位置 倾斜度 等 导航器
 * 并初始化换算程模型变换矩阵
 */
class Navigator {

    var scratchCamera: Camera =
        Camera()

    var latitude = 0.0

    var longitude = 0.0

    var altitude = 0.0

    var heading = 0.0

    var tilt = 0.0

    var roll = 0.0

    private val modelview: Matrix4 = Matrix4()

    private val origin: Matrix4 = Matrix4()

    private val originPoint: Vec3 = Vec3()

    private val originPos: Position = Position()

    private val forwardRay: Line = Line()

    fun getAsCamera(globe: Globe, result: Camera): Camera {
        result.latitude = this.latitude
        result.longitude = this.longitude
        result.altitude = this.altitude
        result.altitudeMode = WorldWind.ABSOLUTE
        result.heading = this.heading
        result.tilt = this.tilt
        result.roll = this.roll
        return result
    }

    fun setAsCamera(globe: Globe, camera: Camera): Navigator {
        this.latitude = camera.latitude
        this.longitude = camera.longitude
        this.altitude = camera.altitude // TODO interpret altitude modes other than absolute

        this.heading = camera.heading
        this.tilt = camera.tilt
        this.roll = camera.roll

        return this
    }


    fun getAsLookAt(globe: Globe, result: LookAt): LookAt {
        getAsCamera(globe, this.scratchCamera)
        this.cameraToLookAt(globe , this.scratchCamera, result)
        return result
    }


    fun setAsLookAt(globe: Globe, lookAt: LookAt): Navigator {
        this.lookAtToCamera(globe , lookAt, scratchCamera)
        setAsCamera(globe, scratchCamera) // TODO convert altitudeMode to absolute if necessary
        return this
    }


    fun getAsViewingMatrix(globe: Globe, result: Matrix4): Matrix4 {
        getAsCamera(globe, scratchCamera) // get this navigator's properties as a Camera
        this.cameraToViewingMatrix(globe, scratchCamera, result) // convert the Camera to a viewing matrix
        return result
    }

    protected fun cameraToLookAt(globe: Globe, camera: Camera, result: LookAt): LookAt {
        cameraToViewingMatrix(globe, camera, modelview)
        modelview.extractEyePoint(forwardRay.origin)
        modelview.extractForwardVector(forwardRay.direction)
        if (!globe.intersect(forwardRay, originPoint)) {
            val horizon = globe.horizonDistance(camera.altitude)
            forwardRay.pointAt(horizon, originPoint)
        }
        globe.cartesianToGeographic(originPoint.x, originPoint.y, originPoint.z, originPos)
        globe.cartesianToLocalTransform(originPoint.x, originPoint.y, originPoint.z, origin)
        modelview.multiplyByMatrix(origin)
        result.latitude = originPos.latitude
        result.longitude = originPos.longitude
        result.altitude = originPos.altitude
        result.range = -modelview.m[11]
        result.heading = modelview.extractHeading(camera.roll) // disambiguate heading and roll
        result.tilt = modelview.extractTilt()
        result.roll = camera.roll // roll passes straight through
        return result
    }

    protected fun cameraToViewingMatrix(
        globe: Globe,
        camera: Camera,
        result: Matrix4
    ): Matrix4 {
        globe.geographicToCartesianTransform(camera.latitude, camera.longitude, camera.altitude, result)
        result.multiplyByRotation(0.0, 0.0, 1.0, -camera.heading) // rotate clockwise about the Z axis
        result.multiplyByRotation(1.0, 0.0, 0.0, camera.tilt) // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0.0, 0.0, 1.0, camera.roll)
        result.invertOrthonormal()
        return result
    }

    protected fun lookAtToCamera(globe: Globe, lookAt: LookAt, result: Camera): Camera {
        lookAtToViewingTransform(globe, lookAt, modelview)
        modelview.extractEyePoint(originPoint)
        globe.cartesianToGeographic(originPoint.x, originPoint.y, originPoint.z, originPos)
        globe.cartesianToLocalTransform(originPoint.x, originPoint.y, originPoint.z, origin)
        modelview.multiplyByMatrix(origin)
        result.latitude = originPos.latitude
        result.longitude = originPos.longitude
        result.altitude = originPos.altitude
        result.heading = modelview.extractHeading(lookAt.roll) // disambiguate heading and roll
        result.tilt = modelview.extractTilt()
        result.roll = lookAt.roll // roll passes straight through
        return result
    }

    protected fun lookAtToViewingTransform(
        globe: Globe,
        lookAt: LookAt,
        result: Matrix4
    ): Matrix4 {
        globe.geographicToCartesianTransform(lookAt.latitude, lookAt.longitude, lookAt.altitude, result)
        // Transform by the heading and tilt.
        result.multiplyByRotation(0.0, 0.0, 1.0, -lookAt.heading) // rotate clockwise about the Z axis
        result.multiplyByRotation(1.0, 0.0, 0.0, lookAt.tilt) // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0.0, 0.0, 1.0, lookAt.roll) // rotate counter-clockwise about the Z axis (again)
        // Transform by the range.
        result.multiplyByTranslation(0.0, 0.0, lookAt.range)
        // Make the transform a viewing matrix.
        result.invertOrthonormal()
        return result
    }
}