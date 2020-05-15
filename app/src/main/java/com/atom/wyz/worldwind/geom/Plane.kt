package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

/**
 * 平面
 */
class Plane {

    companion object{
        protected const val NEAR_ZERO_THRESHOLD = 1e-10
    }
    /**
     * The normal vector to the plane.
     */
    val normal: Vec3 = Vec3()

    /**
     * The plane's distance from the origin.
     */
    var distance = 0.0

    constructor(x: Double, y: Double, z: Double, distance: Double) {
        normal.set(x, y, z)
        this.distance = distance
        this.normalizeIfNeeded()
    }

    constructor() {
        normal.z = 1.0
    }
    constructor(normal: Vec3, distance: Double) {
        this.normal.set(normal)
        this.distance = distance
        this.normalizeIfNeeded()
    }

    constructor(plane : Plane? ) {
        if (plane == null) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "constructor", "missingPlane"))
        }
        normal.set(plane.normal)
        this.distance =plane.distance
    }

    fun set(x: Double, y: Double, z: Double, distance: Double): Plane {
        normal.set(x ,y , z)
        this.distance = distance
        this.normalizeIfNeeded()
        return this
    }
    fun set(normal: Vec3, distance: Double): Plane{
        this.normal.set(normal)
        this.distance = distance
        this.normalizeIfNeeded()
        return this
    }


    fun set(plane : Plane ): Plane {
        normal.set(plane.normal)
        this.distance =plane.distance
        return this
    }

    /**
     * 通过三个点确定一个面
     */
    fun fromPoints(pa: Vec3?, pb: Vec3?, pc: Vec3?): Plane {
        if (pa == null || pb == null || pc == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Plane", "fromPoints", "missingVector"))
        }
        val vab: Vec3 = Vec3(pb.x, pb.y, pb.z)
        vab.subtract(pa)
        val vac: Vec3 = Vec3(pc.x, pc.y, pc.z)
        vac.subtract(pa)
        vab.cross(vac) //差积
        vab.normalize() //规格化
        val d: Double = -vab.dot(pa) //点积
        return Plane(vab.x, vab.y, vab.z, d)
    }

    /**
     * Computes the dot product of this plane's normal vector with a specified vector. Since the plane was defined with
     * a unit normal vector, this function returns the distance of the vector from the plane.
     *
     * @param vector The vector to dot with this plane's normal vector.
     *
     * @throws IllegalArgumentException If the specified vector is null or undefined.
     * @return The computed dot product.
     */
    fun dot(vector: Vec3?): Double {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Plane", "dot", "missingVector"))
        }
        return normal.dot(vector) + distance //TODO
    }

    /**
     * 目标点距离该面的距离
     * 如果是正数则在面的正面
     * 反之负面
     */
    fun distanceToPoint(point: Vec3?): Double {
        return dot(point)
    }

    /**
     * 变换矩阵
     */
    fun transformByMatrix(matrix: Matrix4?): Plane {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Plane", "transformByMatrix", "missingMatrix"))
        }

        val m: DoubleArray = matrix.m
        val x = m[0] * normal.x + m[1] * normal.y + m[2] * normal.z + m[3] * distance
        val y = m[4] * normal.x + m[5] * normal.y + m[6] * normal.z + m[7] * distance
        val z = m[8] * normal.x + m[9] * normal.y + m[10] * normal.z + m[11] * distance
        val distance = m[12] * normal.x + m[13] * normal.y + m[14] * normal.z + m[15] * distance
        normal.x = x
        normal.y = y
        normal.z = z
        this.distance = distance
        this.normalizeIfNeeded()
        return this
    }

    /**
     * Normalizes the components of this plane.
     *  待定
     * @return This plane with its components normalized.
     */
    fun normalizeIfNeeded() {
        // Compute the plane normal's magnitude in order to determine whether or not the plane needs normalization.
        val magnitude = normal.magnitude()

        // Don't normalize a zero vector; the result is NaN when it should be 0.0.
        if (magnitude == 0.0) {
            return
        }

        // Don't normalize a unit vector, this indicates that the caller has already normalized the vector, but floating
        // point roundoff results in a length not exactly 1.0. Since we're normalizing on the caller's behalf, we want
        // to avoid unnecessary any normalization that modifies the specified values.
        if (magnitude >= 1 - NEAR_ZERO_THRESHOLD && magnitude <= 1 + NEAR_ZERO_THRESHOLD) {
            return
        }

        // Normalize the caller-specified plane coordinates.
        normal.x /= magnitude
        normal.y /= magnitude
        normal.z /= magnitude
        distance /= magnitude
    }

    /**
     * 确定指定的线段是否与此平面相交。
     */
    fun intersectsSegment(endPoint1: Vec3, endPoint2: Vec3): Boolean {
        val distance1 = dot(endPoint1)
        val distance2 = dot(endPoint2)
        return distance1 * distance2 <= 0
    }

    /**
     * 计算此平面与指定线段的相交点。
     */
    fun intersectsSegmentAt(endPoint1: Vec3, endPoint2: Vec3, result: Vec3): Boolean {
        // Compute the distance from the end-points.
        val distance1: Double = this.dot(endPoint1)
        val distance2: Double = this.dot(endPoint2)
        // If both points points lie on the plane, ...
        if (distance1 == 0.0 && distance2 == 0.0) { // Choose an arbitrary endpoint as the intersection.
            result.x = endPoint1.x
            result.y = endPoint1.y
            result.z = endPoint1.z
            return true
        } else if (distance1 == distance2) { // The intersection is undefined.
            return false
        }
        val weight1 = -distance1 / (distance2 - distance1)
        val weight2 = 1 - weight1
        result.x = weight1 * endPoint1.x + weight2 * endPoint2.x
        result.y = weight1 * endPoint1.y + weight2 * endPoint2.y
        result.z = weight1 * endPoint1.z + weight2 * endPoint2.z
        return distance1 * distance2 <= 0
    }

    /**
     * Determines whether two points are on the same side of this plane.
     * 是否在同侧
     * -1如果两个点都在该平面的负侧，
     * +1如果两个点都在该平面的正侧
       0 如果点在该平面的相对侧
     * @param pointA the first point.
     * @param pointB the second point.
     *
     * @return -1 If both points are on the negative side of this plane, +1 if both points are on the positive side of
     * this plane, 0 if the points are on opposite sides of this plane.
     *
     * @throws IllegalArgumentException If either point is null or undefined.
     */
    fun onSameSide(pointA: Vec3?, pointB: Vec3?): Int {
        if (pointA == null || pointB == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Plane", "onSameSide", "missingPoint"))
        }
        val da = distanceToPoint(pointA)
        val db = distanceToPoint(pointB)
        if (da < 0 && db < 0) return -1
        return if (da > 0 && db > 0) 1 else 0
    }

    /**
     * 将线段裁剪到此平面。
     */
    fun clip(pointA: Vec3?, pointB: Vec3?): Array<Vec3>? {
        if (pointA == null || pointB == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Plane", "clip", "missingPoint"))
        }
        if (pointA == pointB) {
            return null
        }
        // Get the projection of the segment onto the plane.
        val line: Line = Line().setToSegment(pointA, pointB)
        val lDotV = normal.dot(line.direction) // 先用线的方向和面的法向量进行点积
        // Are the line and plane parallel?
        if (lDotV == 0.0) { // 线和平面平行，和法向量垂直
            val lDotS = dot(line.origin) //面的法向量和线的原点进行点积 并减去面和原点的距离
            return if (lDotS == 0.0) {
                arrayOf<Vec3>(pointA, pointB) // line is coincident with the plane
            } else {
                null // line is not coincident with the plane.
            }
        }
        // Not parallel so the line intersects. But does the segment intersect?
        val t = -dot(line.origin) / lDotV // lDotS / lDotV
        if (t < 0 || t > 1) { // segment does not intersect
            return null
        }
        val p: Vec3 = line.pointAt(t, Vec3())
        return if (lDotV > 0) {
            arrayOf<Vec3>(p, pointB)
        } else {
            arrayOf<Vec3>(pointA, p)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }

        val that: Plane = other as Plane
        return normal.equals(that.normal) && distance == that.distance

    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = normal.hashCode()
        temp = java.lang.Double.doubleToLongBits(distance)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String{
        return "normal=[$normal], distance=$distance"
    }
}

