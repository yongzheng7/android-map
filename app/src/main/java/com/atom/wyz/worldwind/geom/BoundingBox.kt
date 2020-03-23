package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.util.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * 界限框
 */
class BoundingBox() {
    
    companion object{
        // Internal. Intentionally not documented.
         fun adjustExtremes(r: Vec3, rExtremes: DoubleArray, s: Vec3, sExtremes: DoubleArray, t: Vec3, tExtremes: DoubleArray, p: Vec3) {
            val pdr: Double = p.dot(r)
            if (rExtremes[0] > pdr) {
                rExtremes[0] = pdr
            }
            if (rExtremes[1] < pdr) {
                rExtremes[1] = pdr
            }
            val pds: Double = p.dot(s)
            if (sExtremes[0] > pds) {
                sExtremes[0] = pds
            }
            if (sExtremes[1] < pds) {
                sExtremes[1] = pds
            }
            val pdt: Double = p.dot(t)
            if (tExtremes[0] > pdt) {
                tExtremes[0] = pdt
            }
            if (tExtremes[1] < pdt) {
                tExtremes[1] = pdt
            }
        }

        /**
         * 交换
         */
        fun swapAxes(a: Vec3, aExtremes: DoubleArray, b: Vec3, bExtremes: DoubleArray) {
            a.swap(b)
            var tmp = aExtremes[0]
            aExtremes[0] = bExtremes[0]
            bExtremes[0] = tmp
            tmp = aExtremes[1]
            aExtremes[1] = bExtremes[1]
            bExtremes[1] = tmp
        }

        // Internal. Intentionally not documented.
        private fun intersectsAt(plane: Plane, effRadius: Double, endPoint1: Vec3, endPoint2: Vec3): Double { // Test the distance from the first end-point.
            val dq1: Double = plane.dot(endPoint1)
            val bq1 = dq1 <= -effRadius
            // Test the distance from the second end-point.
            val dq2: Double = plane.dot(endPoint2)
            val bq2 = dq2 <= -effRadius
            if (bq1 && bq2) { // endpoints more distant from plane than effective radius; box is on neg. side of plane
                return (-1).toDouble()
            }
            if (bq1 == bq2) { // endpoints less distant from plane than effective radius; can't draw any conclusions
                return 0.0
            }
            // Compute and return the endpoints of the box on the positive side of the plane
            val tmpPoint: Vec3 = Vec3(endPoint1)
            tmpPoint.subtract(endPoint2)
            val t: Double = (effRadius + dq1) / plane.normal.dot(tmpPoint)
            tmpPoint.set(endPoint2)
            tmpPoint.subtract(endPoint1)
            tmpPoint.multiply(t)
            tmpPoint.add(endPoint1)
            // Truncate the line to only that in the positive halfspace, e.g., inside the frustum.
            if (bq1) {
                endPoint1.set(tmpPoint)
            } else {
                endPoint2.set(tmpPoint)
            }
            return t
        }
    }

    var center: Vec3 = Vec3(0.0, 0.0, 0.0)

    var bottomCenter: Vec3 = Vec3(-0.5, 0.0, 0.0)

    var topCenter: Vec3 = Vec3(0.5, 0.0, 0.0)

    var r: Vec3 = Vec3(1.0, 0.0, 0.0)

    var s: Vec3 = Vec3(0.0, 1.0, 0.0)

    var t: Vec3 = Vec3(0.0, 0.0, 1.0)

    var radius = Math.sqrt(3.0)


    fun setToSector(sector: Sector?, globe: Globe?, minElevation: Double, maxElevation: Double): BoundingBox? {
        if (sector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingSector"))
        }
        if (globe == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingGlobe"))
        }
        val numLat = 3
        val numLon = 3
        val count = numLat * numLon
        val stride = 3

        val elevations = DoubleArray(count)

        Arrays.fill(elevations, maxElevation)
        /**
         * 0 1 2
         * 3 4 5
         * 6 7 8
         *
         * 8 6 2 0 == minElevation
         * 1 3 5 7 4 == maxElevation
         */
        elevations[8] = minElevation
        elevations[6] = elevations[8]
        elevations[2] = elevations[6]
        elevations[0] = elevations[2]

        val points = ByteBuffer.allocateDirect(count * stride * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        globe.geographicToCartesianGrid(sector, numLat, numLon, elevations, null, points, stride).rewind() // points中是经过转换的点

        val centroidLat: Double = sector.centroidLatitude()
        val centroidLon: Double = sector.centroidLongitude()

        //
        val matrix: Matrix4 = globe.geographicToCartesianTransform(centroidLat, centroidLon, 0.0, Matrix4()) ?:return null;
        val m: DoubleArray = matrix.m

        this.r.set(m[0],m[4],m[8])
        this.s.set(m[1],m[5],m[9])
        this.t.set(m[2],m[6],m[10])

        // Find the extremes along each axis.
        val rExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        val sExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        val tExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)

        val u = Vec3()
        val coords = FloatArray(stride)

        for (i in 0 until count) {
            points.get(coords, 0, stride)

            u.set(coords[0].toDouble(), coords[1].toDouble(), coords[2].toDouble())

           adjustExtremes(r, rExtremes, s, sExtremes, t, tExtremes, u)
        }
        // Sort the axes from most prominent to least prominent. The frustum intersection methods in WWBoundingBox assume
        // that the axes are defined in this way.
        if (rExtremes[1] - rExtremes[0] < sExtremes[1] - sExtremes[0]) {
            swapAxes(r, rExtremes, s, sExtremes)
        }
        if (sExtremes[1] - sExtremes[0] < tExtremes[1] - tExtremes[0]) {
            swapAxes(s, sExtremes, t, tExtremes)
        }
        if (rExtremes[1] - rExtremes[0] < sExtremes[1] - sExtremes[0]) {
            swapAxes(r, rExtremes, s, sExtremes)
        }
        // Compute the box properties from its unit axes and the extremes along each axis.
        val rLen = rExtremes[1] - rExtremes[0]
        val sLen = sExtremes[1] - sExtremes[0]
        val tLen = tExtremes[1] - tExtremes[0]
        val rSum = rExtremes[1] + rExtremes[0]
        val sSum = sExtremes[1] + sExtremes[0]
        val tSum = tExtremes[1] + tExtremes[0]

        val cx = 0.5 * (r.x * rSum + s.x * sSum + t.x * tSum)
        val cy = 0.5 * (r.y * rSum + s.y * sSum + t.y * tSum)
        val cz = 0.5 * (r.z * rSum + s.z * sSum + t.z * tSum)
        val rx_2 = 0.5 * r.x * rLen
        val ry_2 = 0.5 * r.y * rLen
        val rz_2 = 0.5 * r.z * rLen

        center.set(cx, cy, cz)
        topCenter.set(cx + rx_2, cy + ry_2, cz + rz_2)
        bottomCenter.set(cx - rx_2, cy - ry_2, cz - rz_2)

        r.multiply(rLen)
        s.multiply(sLen)
        t.multiply(tLen)

        radius = 0.5 * Math.sqrt(rLen * rLen + sLen * sLen + tLen * tLen)
        return this
    }

    /**
     * 指示此边界框是否与指定的视锥相交。
     */
    fun intersectsFrustum(frustum: Frustum?) : Boolean{
        if (frustum == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BoundingBox", "intersectsFrustum", "missingFrustum"))
        }

        val tmp1 = Vec3(bottomCenter)
        val tmp2 = Vec3(topCenter)

        if (this.intersectionPoint(frustum.near, tmp1, tmp2) < 0) {
            return false
        }
        if (this.intersectionPoint(frustum.far, tmp1, tmp2) < 0) {
            return false
        }
        if (this.intersectionPoint(frustum.left, tmp1, tmp2) < 0) {
            return false
        }
        if (this.intersectionPoint(frustum.right, tmp1, tmp2) < 0) {
            return false
        }
        if (this.intersectionPoint(frustum.top, tmp1, tmp2) < 0) {
            return false
        }
        return if (this.intersectionPoint(frustum.bottom, tmp1, tmp2) < 0) {
            return false
        } else true
    }

    // Internal. Intentionally not documented.
    private fun intersectionPoint(plane:Plane, endPoint1:Vec3, endPoint2:Vec3): Double {
        val n:Vec3 = plane.normal
        val effectiveRadius = 0.5 * (Math.abs(s.dot(n)) + Math.abs(t.dot(n)))
        return intersectsAt(plane, effectiveRadius, endPoint1, endPoint2)
    }

    /**
     * 点距离该边框的中心的距离
     */
    fun distanceTo(point: Vec3): Double {
        return center.distanceTo(point) // TODO shortest distance to center and corner points
    }

    override fun toString(): String {
        return "center=[$center], bottomCenter=[$bottomCenter], topCenter=[$topCenter], r=[$r], s=[$s], t=[$t], radius=$radius"
    }


}