package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.util.Logger
import java.util.*

/**
 * 界限框
 */
class BoundingBox() {

    companion object {
        // Internal. Intentionally not documented.
        fun adjustExtremes(
            r: Vec3,
            rExtremes: DoubleArray,
            s: Vec3,
            sExtremes: DoubleArray,
            t: Vec3,
            tExtremes: DoubleArray,
            p: Vec3
        ) {
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
    }

    protected var center: Vec3 = Vec3(0.0, 0.0, 0.0)

    protected var bottomCenter: Vec3 = Vec3(-0.5, 0.0, 0.0)

    protected var topCenter: Vec3 = Vec3(0.5, 0.0, 0.0)

    protected var r: Vec3 = Vec3(1.0, 0.0, 0.0)

    protected var s: Vec3 = Vec3(0.0, 1.0, 0.0)

    protected var t: Vec3 = Vec3(0.0, 0.0, 1.0)

    protected var radius = Math.sqrt(3.0)

    private val endPoint1 = Vec3()

    private val endPoint2 = Vec3()


    fun setToSector(sector: Sector?, globe: Globe?, minElevation: Double, maxElevation: Double): BoundingBox? {
        if (sector == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingSector")
            )
        }
        if (globe == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingGlobe")
            )
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

        val points = FloatArray(count * stride)
        // points中是经过转换的点
        globe.geographicToCartesianGrid(sector, numLat, numLon, elevations, null, points, stride , 0)

        val centroidLat: Double = sector.centroidLatitude()
        val centroidLon: Double = sector.centroidLongitude()

        //
        val matrix: Matrix4 =
            globe.geographicToCartesianTransform(centroidLat, centroidLon, 0.0, Matrix4()) ?: return null;
        val m: DoubleArray = matrix.m

        this.r.set(m[0], m[4], m[8])
        this.s.set(m[1], m[5], m[9])
        this.t.set(m[2], m[6], m[10])

        // Find the extremes along each axis.
        val rExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        val sExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        val tExtremes = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)

        val u = Vec3()

        var idx = 0
        val len = points.size
        while (idx < len) {
            u.set(points[idx].toDouble(), points[idx + 1].toDouble() ,points[idx + 2].toDouble())
            adjustExtremes(r, rExtremes, s, sExtremes, t, tExtremes, u)
            idx += stride
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
    fun intersectsFrustum(frustum: Frustum?): Boolean {
        if (frustum == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "intersectsFrustum", "missingFrustum")
            )
        }
        endPoint1.set(bottomCenter)
        endPoint2.set(topCenter)

        if (this.intersectsAt(frustum.near) < 0) {
            return false
        }
        if (this.intersectsAt(frustum.far) < 0) {
            return false
        }
        if (this.intersectsAt(frustum.left) < 0) {
            return false
        }
        if (this.intersectsAt(frustum.right) < 0) {
            return false
        }
        if (this.intersectsAt(frustum.top) < 0) {
            return false
        }
        return if (this.intersectsAt(frustum.bottom) < 0) {
            return false
        } else true
    }

    // Internal. Intentionally not documented.
    private fun intersectsAt(plane: Plane): Double {
        val n: Vec3 = plane.normal
        val effectiveRadius = 0.5 * (Math.abs(s.dot(n)) + Math.abs(t.dot(n)))

        val dq1: Double = plane.dot(endPoint1)
        val bq1 = dq1 <= -effectiveRadius
        // Test the distance from the second end-point.
        val dq2: Double = plane.dot(endPoint2)
        val bq2 = dq2 <= -effectiveRadius
        if (bq1 && bq2) { // endpoints more distant from plane than effective radius; box is on neg. side of plane
            return (-1).toDouble()
        }
        if (bq1 == bq2) { // endpoints less distant from plane than effective radius; can't draw any conclusions
            return 0.0
        }
        // Compute and return the endpoints of the box on the positive side of the plane
        // Compute and return the endpoints of the box on the positive side of the plane
        val dot =
            n.x * (endPoint1.x - endPoint2.x) + n.y * (endPoint1.y - endPoint2.y) + n.z * (endPoint1.z - endPoint2.z)
        val t: Double = (effectiveRadius + dq1) / dot

        // Truncate the line to only that in the positive halfspace, e.g., inside the frustum.
        val x = (endPoint2.x - endPoint1.x) * t + endPoint1.x
        val y = (endPoint2.y - endPoint1.y) * t + endPoint1.y
        val z = (endPoint2.z - endPoint1.z) * t + endPoint1.z
        if (bq1) {
            endPoint1.set(x, y, z)
        } else {
            endPoint2.set(x, y, z)
        }
        return t
    }

    /**
     * 点距离该边框的中心的距离
     */
    fun distanceTo(point: Vec3): Double {

        var minDist2 = Double.POSITIVE_INFINITY

        var dist2 = center.distanceToSquared(point)
        if (minDist2 > dist2) {
            minDist2 = dist2
        }

        dist2 = bottomCenter.distanceToSquared(point)
        if (minDist2 > dist2) {
            minDist2 = dist2
        }
        dist2 = topCenter.distanceToSquared(point)
        if (minDist2 > dist2) {
            minDist2 = dist2
        }

        endPoint1.x = center.x - 0.5 * r.x
        endPoint1.y = center.y - 0.5 * r.y
        endPoint1.z = center.z - 0.5 * r.z
        dist2 = endPoint1.distanceToSquared(point)
        if (minDist2 > dist2) {
            minDist2 = dist2
        }

        endPoint1.x = center.x + 0.5 * r.x
        endPoint1.y = center.y + 0.5 * r.y
        endPoint1.z = center.z + 0.5 * r.z
        dist2 = endPoint1.distanceToSquared(point)
        if (minDist2 > dist2) {
            minDist2 = dist2
        }

        return Math.sqrt(minDist2)
    }

    fun isUnitBox(): Boolean {
        return center.x == 0.0 && center.y == 0.0 && center.z == 0.0 && radius == Math.sqrt(3.0)
    }

    fun translate(x: Double, y: Double, z: Double): BoundingBox {
        center.x += x
        center.y += y
        center.z += z
        bottomCenter.x += x
        bottomCenter.y += y
        bottomCenter.z += z
        topCenter.x += x
        topCenter.y += y
        topCenter.z += z
        return this
    }

    /**
     * Sets this bounding box to a unit box centered at the Cartesian origin (0, 0, 0).
     *
     * @return This bounding box set to a unit box
     */
    fun setToUnitBox(): BoundingBox {
        center.set(0.0, 0.0, 0.0)
        bottomCenter.set(-0.5, 0.0, 0.0)
        topCenter.set(0.5, 0.0, 0.0)
        r.set(1.0, 0.0, 0.0)
        s.set(0.0, 1.0, 0.0)
        t.set(0.0, 0.0, 1.0)
        radius = Math.sqrt(3.0)
        return this
    }

    /**
     * 设置此边界框，以使其最少包围指定的点数组。
     */
    fun setToPoints(array: FloatArray, count: Int, stride: Int): BoundingBox {
         // Compute this box's axes by performing a principal component analysis on the array of points.
        val matrix = Matrix4()
        matrix.setToCovarianceOfPoints(array, count, stride)
        matrix.extractEigenvectors(r, s, t)
        r.normalize()
        s.normalize()
        t.normalize()
        // Find the extremes along each axis.
        var rMin = Double.POSITIVE_INFINITY
        var rMax = Double.NEGATIVE_INFINITY
        var sMin = Double.POSITIVE_INFINITY
        var sMax = Double.NEGATIVE_INFINITY
        var tMin = Double.POSITIVE_INFINITY
        var tMax = Double.NEGATIVE_INFINITY
        val p = Vec3()
        var idx = 0
        while (idx < count) {
            p.set(array[idx].toDouble(), array[idx + 1].toDouble(), array[idx + 2].toDouble())
            val pdr = p.dot(r)
            if (rMin > pdr) {
                rMin = pdr
            }
            if (rMax < pdr) {
                rMax = pdr
            }
            val pds = p.dot(s)
            if (sMin > pds) {
                sMin = pds
            }
            if (sMax < pds) {
                sMax = pds
            }
            val pdt = p.dot(t)
            if (tMin > pdt) {
                tMin = pdt
            }
            if (tMax < pdt) {
                tMax = pdt
            }
            idx += stride
        }
        // Ensure that the extremes along each axis have nonzero separation.
        if (rMax == rMin) rMax = rMin + 1
        if (sMax == sMin) sMax = sMin + 1
        if (tMax == tMin) tMax = tMin + 1
        // Compute the box properties from its unit axes and the extremes along each axis.
        val rLen = rMax - rMin
        val sLen = sMax - sMin
        val tLen = tMax - tMin
        val rSum = rMax + rMin
        val sSum = sMax + sMin
        val tSum = tMax + tMin
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

    override fun toString(): String {
        return "center=[$center], bottomCenter=[$bottomCenter], topCenter=[$topCenter], r=[$r], s=[$s], t=[$t], radius=$radius"
    }




}