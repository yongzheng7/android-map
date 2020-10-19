package com.atom.map.globe

import com.atom.map.geom.*
import com.atom.map.util.Logger

/**
 * GPS
 * 经纬度转笛卡尔坐标系的投影
 */
class ProjectionWgs84 : GeographicProjection {

    val scratchPos: Position = Position()

    override fun getDisplayName(): String {
        return "WGS84"
    }

    override fun geographicToCartesian(globe: Globe, latitude: Double, longitude: Double, altitude: Double, result: Vec3): Vec3 {
        // 经纬度转弧度
        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        // 获取con sin
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val ec2: Double = globe.getEccentricitySquared()
        val rpm: Double = globe.getEquatorialRadius() / Math.sqrt(1.0 - ec2 * sinLat * sinLat)

        result.x = (altitude + rpm) * cosLat * sinLon
        result.y = (altitude + rpm * (1.0 - ec2)) * sinLat
        result.z = (altitude + rpm) * cosLat * cosLon

        return result
    }

    /**
     * 经纬度 转法向量
     */
    override fun geographicToCartesianNormal(globe: Globe, latitude: Double, longitude: Double, result: Vec3): Vec3 {

        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)
        val eqr2: Double = globe.getEquatorialRadius() * globe.getEquatorialRadius()
        val pol2: Double = globe.getPolarRadius() * globe.getPolarRadius()

        result.x = cosLat * sinLon / eqr2
        result.y = (1 - globe.getEccentricitySquared()) * sinLat / pol2
        result.z = cosLat * cosLon / eqr2

        return result.normalize()
    }

    fun geographicToCartesianNorth(globe: Globe, latitude: Double, longitude: Double, result: Vec3): Vec3 {

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
    override fun geographicToCartesianTransform(globe: Globe, latitude: Double, longitude: Double, altitude: Double, result: Matrix4): Matrix4 {

        val radLat = Math.toRadians(latitude)
        val radLon = Math.toRadians(longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val ec2: Double = globe.getEccentricitySquared()
        val pol2: Double = globe.getPolarRadius() * globe.getPolarRadius()

        val rpm: Double = globe.getEquatorialRadius() / Math.sqrt(1.0 - ec2 * sinLat * sinLat)
        val eqr2: Double = globe.getEquatorialRadius() * globe.getEquatorialRadius()

        val px = (rpm + altitude) * cosLat * sinLon
        val py = (rpm * (1.0 - ec2) + altitude) * sinLat
        val pz = (rpm + altitude) * cosLat * cosLon

        var ux = cosLat * sinLon / eqr2
        var uy: Double = (1 - globe.getEccentricitySquared()) * sinLat / pol2
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


    override fun geographicToCartesianGrid(globe: Globe, sector: Sector, numLat: Int, numLon: Int, height: FloatArray?, verticalExaggeration :Float ,origin: Vec3?, result: FloatArray, offset: Int, val_rowStride : Int): FloatArray {
        var rowStride = val_rowStride
        if (numLat < 1 || numLon < 1) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84",
                    "geographicToCartesianGrid",
                    "瓦块的横纵点数小于1"))
        }

        val numPoints = numLat * numLon
        if (height != null && height.size < numPoints) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR,
                    "ProjectionWgs84",
                    "geographicToCartesianGrid",
                    "瓦片的高低点小于总点数"))
        }

        val minLat = Math.toRadians(sector.minLatitude)
        val maxLat = Math.toRadians(sector.maxLatitude)
        val minLon = Math.toRadians(sector.minLongitude)
        val maxLon = Math.toRadians(sector.maxLongitude)
        val deltaLat = (maxLat - minLat) / (if (numLat > 1) numLat - 1 else 1) //每一份的纬度
        val deltaLon = (maxLon - minLon) / (if (numLon > 1) numLon - 1 else 1) //每一份的经度

        val eqr: Double = globe.getEquatorialRadius()
        val ec2: Double = globe.getEccentricitySquared()
        var cosLat: Double
        var sinLat: Double
        var rpm: Double
        val cosLon = DoubleArray(numLon)
        val sinLon = DoubleArray(numLon)

        var latIndex :Int
        var lonIndex :Int
        var elevIndex  = 0

        var lat: Double
        var lon: Double
        var hgt: Float

        val xOffset: Double = if (origin != null) -origin.x else 0.0
        val yOffset: Double = if (origin != null) -origin.y else 0.0
        val zOffset: Double = if (origin != null) -origin.z else 0.0

        // Compute and save values that are a function of each unique longitude value in the specified sector. This
        // eliminates the need to re-compute these values for each column of constant longitude.
        lonIndex = 0
        lon = minLon
        while (lonIndex < numLon) {
            if (lonIndex == numLon - 1) {
                lon = maxLon // explicitly set the last lon to the max longitude to ensure alignment
            }
            cosLon[lonIndex] = Math.cos(lon) //每份的cos 经度
            sinLon[lonIndex] = Math.sin(lon) //每份的sin 经度
            lonIndex++
            lon += deltaLon
        }

        var rowIndex = offset // 行的索引起点偏移
        if (rowStride == 0) {
            rowStride = numLon * 3 //行的步
        }
        // 开始的起点偏移量 加上 该瓦片中 一行(经度点) * 3 就是 中间空余出一行的点数的float的值
        lat = minLat
        latIndex = 0
        while (latIndex < numLat) {
            if (latIndex == numLat - 1) {
                lat = maxLat // explicitly set the last lat to the max latitude to ensure alignment
            }
            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            cosLat = Math.cos(lat)
            sinLat = Math.sin(lat)
            rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat)
            var colIndex = rowIndex
            lonIndex = 0
            while (lonIndex < numLon) {
                hgt = height?.get(elevIndex++) ?:0f * verticalExaggeration
                result[colIndex++] = ((hgt + rpm) * cosLat * sinLon[lonIndex] + xOffset).toFloat()
                result[colIndex++] = ((hgt + rpm * (1.0 - ec2)) * sinLat + yOffset).toFloat()
                result[colIndex++] = ((hgt + rpm) * cosLat * cosLon[lonIndex] + zOffset).toFloat()
                lonIndex++
            }
            rowIndex += rowStride

            latIndex++
            lat += deltaLat
        }
        return result
    }

    override fun geographicToCartesianBorder(
        globe: Globe, sector: Sector,
        numLat: Int, numLon: Int, height: Float,
        origin: Vec3?, result: FloatArray
    ): FloatArray {
        require(!(numLat < 1 || numLon < 1)) {
            Logger.logMessage(
                Logger.ERROR, "ProjectionWgs84", "geographicToCartesianBorder",
                "Number of latitude or longitude locations is less than one"
            )
        }
        val minLat = Math.toRadians(sector.minLatitude)
        val maxLat = Math.toRadians(sector.maxLatitude)
        val minLon = Math.toRadians(sector.minLongitude)
        val maxLon = Math.toRadians(sector.maxLongitude)
        val deltaLat = (maxLat - minLat) / if (numLat > 1) numLat - 3 else 1 // 16 - 3 == 13
        val deltaLon = (maxLon - minLon) / if (numLon > 1) numLon - 3 else 1
        var lat = minLat // 最小的纬度
        var lon = minLon // 最小的经度

        val eqr = globe.getEquatorialRadius()
        val ec2 = globe.getEccentricitySquared()

        val xOffset = if (origin != null) -origin.x else 0.toDouble()
        val yOffset = if (origin != null) -origin.y else 0.toDouble()
        val zOffset = if (origin != null) -origin.z else 0.toDouble()
        var resultIndex = 0
        // Iterate over the edges of the specified sector, computing the Cartesian point at designated latitude and
        // longitude around the border.
        for (latIndex in 0 until numLat) {
            if (latIndex < 2) {
                lat = minLat // explicitly set the first lat to the min latitude to ensure alignment
            } else if (latIndex < numLat - 2) {
                lat += deltaLat
            } else {
                lat = maxLat // explicitly set the last lat to the max latitude to ensure alignment
            }
            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            val cosLat = Math.cos(lat)
            val sinLat = Math.sin(lat)
            val rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat)
            var lonIndex = 0
            while (lonIndex < numLon) {
                if (lonIndex < 2) {
                    lon = minLon // explicitly set the first lon to the min longitude to ensure alignment
                } else if (lonIndex < numLon - 2) {
                    lon += deltaLon
                } else {
                    lon = maxLon // explicitly set the last lon to the max longitude to ensure alignment
                }
                val cosLon = Math.cos(lon)
                val sinLon = Math.sin(lon)
                result[resultIndex++] = ((height + rpm) * cosLat * sinLon + xOffset).toFloat()
                result[resultIndex++] = ((height + rpm * (1.0 - ec2)) * sinLat + yOffset).toFloat()
                result[resultIndex++] = ((height + rpm) * cosLat * cosLon + zOffset).toFloat()
                if (lonIndex == 0 && latIndex != 0 && latIndex != numLat - 1) {
                    val skip = numLon - 2
                    lonIndex += skip
                    resultIndex += skip * 3
                }
                lonIndex++
            }
        }
        return result
    }

    /**
     * 笛卡尔地心坐标系转大地经纬度坐标系
     */
    override fun cartesianToGeographic(globe: Globe, x: Double, y: Double, z: Double, result: Position): Position {
        val XXpYY = z * z + x * x
        val sqrtXXpYY = Math.sqrt(XXpYY)

        val a: Double = globe.getEquatorialRadius()
        val ra2 = 1 / (a * a)
        val e2: Double = globe.getEccentricitySquared()
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
    override fun cartesianToLocalTransform(globe: Globe, x: Double, y: Double, z: Double, result: Matrix4): Matrix4 {
        val pos: Position = cartesianToGeographic(globe, x, y, z, scratchPos)
        val radLat = Math.toRadians(pos.latitude)
        val radLon = Math.toRadians(pos.longitude)
        val cosLat = Math.cos(radLat)
        val sinLat = Math.sin(radLat)
        val cosLon = Math.cos(radLon)
        val sinLon = Math.sin(radLon)

        val eqr2: Double = globe.getEquatorialRadius() * globe.getEquatorialRadius()
        val pol2: Double = globe.getPolarRadius() * globe.getPolarRadius()

        var ux = cosLat * sinLon / eqr2
        var uy: Double = (1 - globe.getEccentricitySquared()) * sinLat / pol2
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
    override fun intersect(globe: Globe, line: Line, result: Vec3): Boolean {

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition", Section 5.2.3.
        // Note that the parameter n from in equations 5.70 and 5.71 is omitted here. For an ellipsoidal globe this
        // parameter is always 1, so its square and its product with any other value simplifies to the identity.

        val vx = line.direction.x
        val vy = line.direction.y
        val vz = line.direction.z

        val sx = line.origin.x
        val sy = line.origin.y
        val sz = line.origin.z

        val eqr: Double = globe.getEquatorialRadius()
        val eqr2 = eqr * eqr // nominal radius squared
        //赤道半径比极半径
        val m: Double = eqr / globe.getPolarRadius() // ratio of the x semi-axis length to the y semi-axis length
        val m2 = m * m

        val a = vx * vx + m2 * vy * vy + vz * vz
        val b = 2 * (sx * vx + m2 * sy * vy + sz * vz)
        val c = sx * sx + m2 * sy * sy + sz * sz - eqr2
        val d = b * b - 4 * a * c // discriminant


//        return if (d < 0) {
//            false
//        } else {
//            val t = (-b - Math.sqrt(d)) / (2 * a)
//            result.x = sx + vx * t
//            result.y = sy + vy * t
//            result.z = sz + vz * t
//            true
//        }

        if (d < 0) {
            return false
        }

        var t = (-b - Math.sqrt(d)) / (2 * a)
        // check if the nearest intersection point is in front of the origin of the ray
        if (t > 0) {
            result.x = sx + vx * t
            result.y = sy + vy * t
            result.z = sz + vz * t
            return true
        }

        t = (-b + Math.sqrt(d)) / (2 * a)
        // check if the second intersection point is in front of the origin of the ray
        if (t > 0) {
            result.x = sx + vx * t
            result.y = sy + vy * t
            result.z = sz + vz * t
            return true
        }

        // the intersection points were behind the origin of the provided line
        return false
    }


}