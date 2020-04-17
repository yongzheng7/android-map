package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.util.Logger
import java.nio.FloatBuffer

/**
 * GPS
 * 经纬度转笛卡尔坐标系的投影
 */
class ProjectionWgs84() : GeographicProjection {

    val scratchPos: Position = Position()

    override fun getDisplayName(): String {
        return "WGS84"
    }

    override fun geographicToCartesian(globe: Globe?, latitude: Double, longitude: Double, altitude: Double, offset: Vec3?, result: Vec3?): Vec3 {
        if (globe == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesian", "missingGlobe"))
        }

        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesian", "missingResult"))
        }
        // 经纬度转弧度
        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        // 获取con sin
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val ec2: Double = globe.eccentricitySquared
        val rpm: Double = globe.equatorialRadius / Math.sqrt(1.0 - ec2 * sinLat * sinLat)

        result.x = (altitude + rpm) * cosLat * sinLon
        result.y = (altitude + rpm * (1.0 - ec2)) * sinLat
        result.z = (altitude + rpm) * cosLat * cosLon

        return result
    }

    /**
     * 经纬度 转法向量
     */
    override fun geographicToCartesianNormal(globe: Globe?, latitude: Double, longitude: Double, result: Vec3?): Vec3 {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingGlobe"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingResult"))
        }

        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)
        val eqr2: Double = globe.equatorialRadius * globe.equatorialRadius
        val pol2: Double = globe.polarRadius * globe.polarRadius

        result.x = cosLat * sinLon / eqr2
        result.y = (1 - globe.eccentricitySquared) * sinLat / pol2
        result.z = cosLat * cosLon / eqr2

        return result.normalize()
    }

    fun geographicToCartesianNorth(globe: Globe?, latitude: Double, longitude: Double, result: Vec3?): Vec3 {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingGlobe"))
        }
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingResult"))
        }
        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        result.x = -sinLat * sinLon
        result.y = cosLat
        result.z = -sinLat * cosLon
        return result.normalize()
    }

    /**
     * 地理>>转变>>笛卡尔
     */
    override fun geographicToCartesianTransform(globe: Globe?, latitude: Double, longitude: Double, altitude: Double, offset: Vec3?, result: Matrix4?): Matrix4 {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingGlobe"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingResult"))
        }

        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val ec2: Double = globe.eccentricitySquared
        val rpm: Double = globe.equatorialRadius / Math.sqrt(1.0 - ec2 * sinLat * sinLat)
        val eqr2: Double = globe.equatorialRadius * globe.equatorialRadius
        val pol2: Double = globe.polarRadius * globe.polarRadius

        val px = (rpm + altitude) * cosLat * sinLon
        val py = (rpm * (1.0 - ec2) + altitude) * sinLat
        val pz = (rpm + altitude) * cosLat * cosLon

        var ux = cosLat * sinLon / eqr2
        var uy: Double = (1 - globe.eccentricitySquared) * sinLat / pol2
        var uz = cosLat * cosLon / eqr2

        var len = Math.sqrt(ux * ux + uy * uy + uz * uz)
        ux /= len
        uy /= len
        uz /= len

        var nx = -sinLat * sinLon
        var ny = cosLat
        var nz = -sinLat * cosLon

        len = Math.sqrt(nx * nx + ny * ny + nz * nz)
        nx /= len
        ny /= len
        nz /= len

        val ex = ny * uz - nz * uy
        val ey = nz * ux - nx * uz
        val ez = nx * uy - ny * ux

        nx = uy * ez - uz * ey
        ny = uz * ex - ux * ez
        nz = ux * ey - uy * ex

        result.set(ex, nx, ux, px, ey, ny, uy, py, ez, nz, uz, pz, 0.0, 0.0, 0.0 , 1.0)

        return result
    }


    override fun geographicToCartesianGrid(globe: Globe?, sector: Sector?, numLat: Int, numLon: Int, elevations: DoubleArray?, origin: Vec3?, offset: Vec3?, result: FloatBuffer?, stride: Int): FloatBuffer {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingGlobe"))
        }

        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingSector"))
        }

        if (numLat < 1 || numLon < 1) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "ProjectionWgs84",
                    "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"))
        }

        val numPoints = numLat * numLon

        if (elevations != null && elevations.size < numPoints) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "ProjectionWgs84",
                    "geographicToCartesianGrid", "missingArray"))
        }

        if (result == null || result.remaining() < numPoints * stride) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingResult"))
        }

        val eqr: Double = globe.equatorialRadius
        val ec2: Double = globe.eccentricitySquared
        var cosLat: Double
        var sinLat: Double
        var rpm: Double
        val cosLon = DoubleArray(numLon)
        val sinLon = DoubleArray(numLon)

        val minLat = Math.toRadians(sector.minLatitude)
        val maxLat = Math.toRadians(sector.maxLatitude)
        val minLon = Math.toRadians(sector.minLongitude)
        val maxLon = Math.toRadians(sector.maxLongitude)
        val deltaLat = (maxLat - minLat) / (if (numLat > 1) numLat - 1 else 1)
        val deltaLon = (maxLon - minLon) / (if (numLon > 1) numLon - 1 else 1)

        var latIndex :Int
        var lonIndex :Int
        var elevIndex :Int = 0

        var pos :Int

        var lat: Double
        var lon: Double
        var elev: Double

        val xOffset: Double = if (origin != null) -origin.x else 0.0
        val yOffset: Double = if (origin != null) -origin.y else 0.0
        val zOffset: Double = if (origin != null) -origin.z else 0.0

        val xyz = FloatArray(3)

        // Compute and save values that are a function of each unique longitude value in the specified sector. This
        // eliminates the need to re-compute these values for each column of constant longitude.
        lonIndex = 0
        lon = minLon
        while (lonIndex < numLon) {
            if (lonIndex == numLon - 1) {
                lon = maxLon // explicitly set the last lon to the max longitude to ensure alignment
            }
            cosLon[lonIndex] = Math.cos(lon)
            sinLon[lonIndex] = Math.sin(lon)
            lonIndex++
            lon += deltaLon
        }

        latIndex = 0
        lat = minLat
        while (latIndex < numLat) {
            if (latIndex == numLat - 1) {
                lat = maxLat // explicitly set the last lat to the max latitude to ensure alignment
            }
            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            cosLat = Math.cos(lat)
            sinLat = Math.sin(lat)
            rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat)
            lonIndex = 0
            while (lonIndex < numLon) {
                pos = result.position()
                elev = elevations?.get(elevIndex++) ?: 0.0
                xyz[0] = ((elev + rpm) * cosLat * sinLon[lonIndex] + xOffset).toFloat()
                xyz[1] = ((elev + rpm * (1.0 - ec2)) * sinLat + yOffset).toFloat()
                xyz[2] = ((elev + rpm) * cosLat * cosLon[lonIndex] + zOffset).toFloat()
                result.put(xyz, 0, 3)
                if (result.limit() >= pos + stride) {
                    result.position(pos + stride)
                }
                lonIndex++
            }
            latIndex++
            lat += deltaLat
        }
        return result
    }

    /**
     * 笛卡尔地心坐标系转大地经纬度坐标系
     */
    override fun cartesianToGeographic(globe: Globe?, x: Double, y: Double, z: Double, offset: Vec3?, result: Position?): Position? {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingGlobe"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingResult"))
        }



        val XXpYY = z * z + x * x
        val sqrtXXpYY = Math.sqrt(XXpYY)

        val a: Double = globe.equatorialRadius
        val ra2 = 1 / (a * a)
        val e2: Double = globe.eccentricitySquared
        val e4 = e2 * e2
        // Step 1
        val p = XXpYY * ra2
        val q = y * y * (1 - e2) * ra2
        val r = (p + q - e4) / 6

        val h: Double
        val phi: Double

        val evoluteBorderTest = 8 * r * r * r + e4 * p * q

        if (evoluteBorderTest > 0 || q != 0.0) {
            val u: Double
            u = if (evoluteBorderTest > 0) {
                // Step 2: general case
                val rad1 = Math.sqrt(evoluteBorderTest)
                val rad2 = Math.sqrt(e4 * p * q)

                // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                if (evoluteBorderTest > 10 * e2) {
                    val rad3 = Math.cbrt((rad1 + rad2) * (rad1 + rad2))
                    r + 0.5 * rad3 + 2 * r * r / rad3
                } else {
                    r + 0.5 * Math.cbrt((rad1 + rad2) * (rad1 + rad2)) + 0.5 * Math.cbrt(
                            (rad1 - rad2) * (rad1 - rad2))
                }
            } else { // Step 3: near evolute
                val rad1 = Math.sqrt(-evoluteBorderTest)
                val rad2 = Math.sqrt(-8 * r * r * r)
                val rad3 = Math.sqrt(e4 * p * q)
                val atan = 2 * Math.atan2(rad3, rad1 + rad2) / 3
                -4 * r * Math.sin(atan) * Math.cos(Math.PI / 6 + atan)
            }
            val v = Math.sqrt(u * u + e4 * q)
            val w = e2 * (u + v - q) / (2 * v)
            val k = (u + v) / (Math.sqrt(w * w + u + v) + w)
            val D = k * sqrtXXpYY / (k + e2)
            val sqrtDDpZZ = Math.sqrt(D * D + y * y)
            h = (k + e2 - 1) * sqrtDDpZZ / k
            phi = 2 * Math.atan2(y, sqrtDDpZZ + D)
        } else { // Step 4: singular disk
            val rad1 = Math.sqrt(1 - e2)
            val rad2 = Math.sqrt(e2 - p)
            val e = Math.sqrt(e2)
            h = -a * rad1 * rad2 / e
            phi = rad2 / (e * rad2 + rad1 * Math.sqrt(p))
        }

        // Compute lambda
        // Compute lambda
        val lambda: Double
        val s2 = Math.sqrt(2.0)
        lambda = if ((s2 - 1) * x < sqrtXXpYY + z) { // case 1 - -135deg < lambda < 135deg
            2 * Math.atan2(x, sqrtXXpYY + z)
        } else if (sqrtXXpYY + x < (s2 + 1) * z) { // case 2 - -225deg < lambda < 45deg
            -Math.PI * 0.5 + 2 * Math.atan2(z, sqrtXXpYY - x)
        } else { // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
        // case 3: - -45deg < lambda < 225deg
            Math.PI * 0.5 - 2 * Math.atan2(z, sqrtXXpYY + x)
        }

        result.latitude = Math.toDegrees(phi)
        result.longitude = Math.toDegrees(lambda)
        result.altitude = h

        return result
    }

    /**
     * 笛卡尔坐标系 到 本地的位移
     */
    override fun cartesianToLocalTransform(globe: Globe?, x: Double, y: Double, z: Double, offset: Vec3?, result: Matrix4?): Matrix4? {

        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToLocalTransform", "missingGlobe"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToLocalTransform", "missingResult"))
        }

        val pos: Position = cartesianToGeographic(globe, x, y, z, offset, scratchPos) ?: return null
        val radLat = Math.toRadians(pos.latitude)
        val radLon = Math.toRadians(pos.longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val eqr2: Double = globe.equatorialRadius * globe.equatorialRadius
        val pol2: Double = globe.polarRadius * globe.polarRadius


        var ux = cosLat * sinLon / eqr2
        var uy: Double = (1 - globe.eccentricitySquared) * sinLat / pol2
        var uz = cosLat * cosLon / eqr2
        var len = Math.sqrt(ux * ux + uy * uy + uz * uz)
        ux /= len
        uy /= len
        uz /= len


        var nx = -sinLat * sinLon
        var ny = cosLat
        var nz = -sinLat * cosLon
        len = Math.sqrt(nx * nx + ny * ny + nz * nz)
        nx /= len
        ny /= len
        nz /= len


        val ex = ny * uz - nz * uy
        val ey = nz * ux - nx * uz
        val ez = nx * uy - ny * ux


        nx = uy * ez - uz * ey
        ny = uz * ex - ux * ez
        nz = ux * ey - uy * ex


        result.set(ex, nx, ux, x, ey, ny, uy, y, ez, nz, uz, z, 0.0, 0.0, 0.0,1.0)

        return result
    }

    /**
     * 计算视线和地球的交点
     */
    override fun intersect(globe: Globe?, line: Line?, offset: Vec3?, result: Vec3?): Boolean {
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingGlobe"))
        }

        if (line == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "intersect", "missingLine"))
        }

        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "intersect", "missingResult"))
        }

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition", Section 5.2.3.
        //
        // Note that the parameter n from in equations 5.70 and 5.71 is omitted here. For an ellipsoidal globe this
        // parameter is always 1, so its square and its product with any other value simplifies to the identity.

        val vx = line.direction.x
        val vy = line.direction.y
        val vz = line.direction.z

        val sx = line.origin.x
        val sy = line.origin.y
        val sz = line.origin.z

        val eqr: Double = globe.equatorialRadius
        val eqr2 = eqr * eqr // nominal radius squared
        //赤道半径比极半径
        val m: Double = eqr / globe.polarRadius // ratio of the x semi-axis length to the y semi-axis length

        val m2 = m * m

        val a = vx * vx + m2 * vy * vy + vz * vz
        val b = 2 * (sx * vx + m2 * sy * vy + sz * vz)
        val c = sx * sx + m2 * sy * sy + sz * sz - eqr2

        val d = b * b - 4 * a * c // discriminant


        return if (d < 0) {
            false
        } else {
            val t = (-b - Math.sqrt(d)) / (2 * a)
            result.x = sx + vx * t
            result.y = sy + vy * t
            result.z = sz + vz * t
            true
        }
    }


}