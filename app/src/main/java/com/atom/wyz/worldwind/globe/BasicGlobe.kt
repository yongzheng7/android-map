package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.util.Logger
import java.nio.FloatBuffer

open class BasicGlobe: Globe {


    final override var eccentricitySquared: Double
    final override var equatorialRadius: Double
    final override var polarRadius: Double
    final override var projection: GeographicProjection
    final override var tessellator: Tessellator

    val modelview: Matrix4 = Matrix4()

    val origin: Matrix4 = Matrix4()

    val originPoint: Vec3 = Vec3()

    val originPos: Position = Position()

    val forwardRay: Line = Line()

    /**
     * 赤道半径 == c
     * 两级半径 == j
     * f == ( c-j )/ c
     * inverseFlattening == c / ( c-j )
     * 偏心率平方  == 2 * f - f * f;
     */
    constructor(semiMajorAxis: Double, inverseFlattening: Double, projection: GeographicProjection?) {
        if (semiMajorAxis <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Semi-major axis is invalid"))
        }
        if (inverseFlattening <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Inverse flattening is invalid"))
        }
        if (projection == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "missingProjection"))
        }
        val f: Double = 1 / inverseFlattening
        this.equatorialRadius = semiMajorAxis
        this.polarRadius = semiMajorAxis * (1 - f)

        this.eccentricitySquared = 2 * f - f * f

        this.projection = projection
        this.tessellator = BasicTessellator()
    }

    override fun getRadiusAt(latitude: Double, longitude: Double): Double {
        val sinLat = Math.sin(Math.toRadians(latitude))
        val ec2 = eccentricitySquared
        val rpm = equatorialRadius / Math.sqrt(1 - ec2 * sinLat * sinLat)
        return rpm * Math.sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat)
    }
    override fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double, result: Vec3): Vec3 {
        return projection.geographicToCartesian(this, latitude, longitude, altitude, null, result)
    }
    override fun geographicToCartesianNormal(latitude: Double, longitude: Double, result: Vec3): Vec3 {
        return projection.geographicToCartesianNormal(this, latitude, longitude, result)
    }
    override fun geographicToCartesianTransform(latitude: Double, longitude: Double, altitude: Double, result: Matrix4): Matrix4 {
        return projection.geographicToCartesianTransform(this, latitude, longitude, altitude, null, result)
    }
    override fun geographicToCartesianGrid(sector: Sector, numLat: Int, numLon: Int, elevations: DoubleArray?, origin: Vec3?, result: FloatArray, stride: Int, pos : Int): FloatArray {
        if (numLat < 1 || numLon < 1) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                    "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"))
        }

        val numPoints = numLat * numLon
        if (elevations != null && elevations.size < numPoints) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                    "geographicToCartesianGrid", "missingArray"))
        }

        if (result.size < numPoints * stride + pos) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingResult"))
        }

        return projection.geographicToCartesianGrid(this, sector, numLat, numLon, elevations, origin, null, result, stride , pos)
    }
    override fun cartesianToGeographic(x: Double, y: Double, z: Double, result: Position): Position {
        return projection.cartesianToGeographic(this, x, y, z, null, result)
    }
    override fun cartesianToLocalTransform(x: Double, y: Double, z: Double, result: Matrix4): Matrix4 {
        return projection.cartesianToLocalTransform(this, x, y, z, null, result)
    }
    override fun cameraToCartesianTransform(camera: Camera, result: Matrix4): Matrix4 {
        geographicToCartesianTransform(camera.latitude, camera.longitude, camera.altitude, result)
        // Transform by the heading, tilt and roll.
        result.multiplyByRotation(0.0, 0.0, 1.0, -camera.heading) // rotate clockwise about the Z axis
        result.multiplyByRotation(1.0, 0.0, 0.0, camera.tilt) // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0.0, 0.0, 1.0, camera.roll) // rotate counter-clockwise about the Z axis (again)
        return result
    }
    override fun cameraToLookAt(camera: Camera, result: LookAt): LookAt {
        cameraToCartesianTransform(camera, modelview)!!.invertOrthonormal()
        modelview.extractEyePoint(forwardRay.origin)
        modelview.extractForwardVector(forwardRay.direction)
        if (!this.intersect(forwardRay, originPoint)) {
            val horizon = this.horizonDistance(camera.altitude)
            forwardRay.pointAt(horizon, originPoint)
        }
        cartesianToGeographic(originPoint.x, originPoint.y, originPoint.z, originPos)
        cartesianToLocalTransform(originPoint.x, originPoint.y, originPoint.z, origin)
        modelview.multiplyByMatrix(origin)
        result.latitude = originPos.latitude
        result.longitude = originPos.longitude
        result.altitude = originPos.altitude
        result.range = -modelview.m[11]
        result.heading = this.computeViewHeading(modelview, camera.roll)
        result.tilt = this.computeViewTilt(modelview)
        result.roll = camera.roll
        return result
    }
    override fun lookAtToCartesianTransform(lookAt: LookAt, result: Matrix4): Matrix4 {
        geographicToCartesianTransform(lookAt.latitude, lookAt.longitude, lookAt.altitude, result)

        result.multiplyByRotation(0.0, 0.0, 1.0, -lookAt.heading) // rotate clockwise about the Z axis

        result.multiplyByRotation(1.0, 0.0, 0.0, lookAt.tilt) // rotate counter-clockwise about the X axis

        result.multiplyByRotation(0.0, 0.0, 1.0, lookAt.roll) // rotate counter-clockwise about the Z axis (again)

        result.multiplyByTranslation(0.0, 0.0, lookAt.range)

        return result
    }
    override fun lookAtToCamera(lookAt: LookAt, result: Camera): Camera {
        lookAtToCartesianTransform(lookAt, modelview)!!.invertOrthonormal()
        modelview.extractEyePoint(originPoint)

        cartesianToGeographic(originPoint.x, originPoint.y, originPoint.z, originPos)
        cartesianToLocalTransform(originPoint.x, originPoint.y, originPoint.z, origin)
        modelview.multiplyByMatrix(origin)

        result.latitude = originPos.latitude
        result.longitude = originPos.longitude
        result.altitude = originPos.altitude
        result.heading = this.computeViewHeading(modelview, lookAt.roll) // disambiguate heading and roll

        result.tilt = this.computeViewTilt(modelview)
        result.roll = lookAt.roll // roll passes straight through
        return result
    }
    protected open fun computeViewHeading(matrix: Matrix4, roll: Double): Double {
        val rad = Math.toRadians(roll)
        val cr = Math.cos(rad)
        val sr = Math.sin(rad)
        val m: DoubleArray = matrix.m
        val ch = cr * m[0] - sr * m[4]
        val sh = sr * m[5] - cr * m[1]
        return Math.toDegrees(Math.atan2(sh, ch))
    }
    protected open fun computeViewTilt(matrix: Matrix4): Double {
        val m: DoubleArray = matrix.m
        val ct = m[10]
        val st = Math.sqrt(m[2] * m[2] + m[6] * m[6])
        return Math.toDegrees(Math.atan2(st, ct))
    }
    override fun horizonDistance(eyeAltitude: Double): Double {
        val eqr = equatorialRadius
        return Math.sqrt(eyeAltitude * (2 * eqr + eyeAltitude))
    }
    override fun horizonDistance(eyeAltitude: Double, objectAltitude: Double): Double {
        val eqr = equatorialRadius
        val eyeDistance = Math.sqrt(eyeAltitude * (2 * eqr + eyeAltitude)) // distance from eye altitude to globe MSL horizon
        val horDistance = Math.sqrt(objectAltitude * (2 * eqr + objectAltitude)) // distance from object altitude to globe MSL horizon
        return eyeDistance + horDistance // desired distance is the sum of the two horizon distances
    }
    override fun intersect(line: Line, result: Vec3): Boolean {
        return projection.intersect(this, line, null, result)
    }
}