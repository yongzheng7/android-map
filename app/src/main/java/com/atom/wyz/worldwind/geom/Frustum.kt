package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Frustum {
    val left: Plane = Plane(1.0, 0.0, 0.0, 1.0)

    val right: Plane = Plane(-1.0, 0.0, 0.0, 1.0)

    val bottom: Plane = Plane(0.0, 1.0, 0.0, 1.0)

    val top: Plane = Plane(0.0, -1.0, 0.0, 1.0)

    val near: Plane = Plane(0.0, 0.0, -1.0, 1.0)

    val far: Plane = Plane(0.0, 0.0, 1.0, 1.0)

    private var planes = arrayOf(this.left, this.right, this.top, this.bottom, this.near, this.far)

    constructor()
    constructor(left: Plane?, right: Plane?, bottom: Plane?, top: Plane?, near: Plane?, far: Plane?) {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingPlane"))
        }
        // Internal. Intentionally not documented. See property accessors below for public interface.
        this.left.set(left)
        this.right.set(right)
        this.bottom.set(bottom)
        this.top.set(top)
        this.near.set(near)
        this.far.set(far)
    }

    /**
     * 将此视锥设置为单位视锥，其每个平面距中心1米
     */
    fun setToUnitFrustum(): Frustum {
        left.set(1.0, 0.0, 0.0, 1.0)
        right.set(-1.0, 0.0, 0.0, 1.0)
        bottom.set(0.0, 1.0, 0.0, 1.0)
        top.set(0.0, -1.0, 0.0, 1.0)
        near.set(0.0, 0.0, -1.0, 1.0)
        far.set(0.0, 0.0, 1.0, 1.0)
        return this
    }

    /**
     * 视口变换 通过矩阵
     */
    fun transformByMatrix(matrix: Matrix4?): Frustum {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "transformByMatrix", "missingMatrix"))
        }
        left.transformByMatrix(matrix)
        right.transformByMatrix(matrix)
        bottom.transformByMatrix(matrix)
        top.transformByMatrix(matrix)
        near.transformByMatrix(matrix)
        far.transformByMatrix(matrix)
        return this
    }

    /**
     * 视口进行规格化
     */
    fun normalize(): Frustum {
        left.normalize()
        right.normalize()
        bottom.normalize()
        top.normalize()
        near.normalize()
        far.normalize()
        return this
    }

    /**
     * 创建一个标准的视口边长为2的
     */
    fun unitFrustum(): Frustum {

        return Frustum(
                Plane(1.0, 0.0, 0.0, 1.0),  // left
                Plane(-1.0, 0.0, 0.0, 1.0),  // right
                Plane(0.0, 1.0, 0.0, 1.0),  // bottom
                Plane(0.0, -1.0, 0.0, 1.0),  // top
                Plane(0.0, 0.0, -1.0, 1.0),  // near
                Plane(0.0, 0.0, 1.0, 1.0) // far
        )
    }

    /**
     * 来自透视矩阵 变为视口
     */
    fun setToProjectionMatrix(matrix: Matrix4?): Frustum {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "setToProjectionMatrix", "missingMatrix"))
        }
        /**
         * 0  1  2  3
         * 4  5  6  7
         * 8  9  10 11
         * 12 13 14 15
         *
         * 1  0  0  0
         * 0  1  0  0
         * 0  0  1  0
         * 0  0  0  1
         */
        val m: DoubleArray = matrix.m
        var x: Double
        var y: Double
        var z: Double
        var w: Double
        var d: Double
        // Left Plane = row 4 + row 1:
        x = m[12] + m[0]
        y = m[13] + m[1]
        z = m[14] + m[2]
        w = m[15] + m[3]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        left.set(x / d, y / d, z / d, w / d)

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0]
        y = m[13] - m[1]
        z = m[14] - m[2]
        w = m[15] - m[3]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        right.set(x / d, y / d, z / d, w / d)

        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4]
        y = m[13] + m[5]
        z = m[14] + m[6]
        w = m[15] + m[7]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        bottom.set(x / d, y / d, z / d, w / d)

        // Top Plane = row 4 - row 2:
        x = m[12] - m[4]
        y = m[13] - m[5]
        z = m[14] - m[6]
        w = m[15] - m[7]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        top.set(x / d, y / d, z / d, w / d)

        // Near Plane = row 4 + row 3:
        x = m[12] + m[8]
        y = m[13] + m[9]
        z = m[14] + m[10]
        w = m[15] + m[11]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        near.set(x / d, y / d, z / d, w / d)

        // Far Plane = row 4 - row 3:
        x = m[12] - m[8]
        y = m[13] - m[9]
        z = m[14] - m[10]
        w = m[15] - m[11]
        d = Math.sqrt(x * x + y * y + z * z) // for normalizing the coordinates
        far.set(x / d, y / d, z / d, w / d)


        return this
    }

    /**
     * 判断视锥是否包含点 TODO 有问题
     */
    fun containsPoint(point: Vec3?): Boolean {
        if (point == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"))
        }
        // See if the point is entirely within the frustum. The dot product of the point with each plane's vector
        // provides a distance to each plane. If this distance is less than 0, the point is clipped by that plane and
        // neither intersects nor is contained by the space enclosed by this Frustum.

        // 查看该点是否完全在视锥范围内。 点与每个平面向量的点积提供了到每个平面的距离。
        // 如果此距离小于0，则该点将被该平面修剪，并且该截锥既不相交也不包含在该视锥体中。

        if (far.dot(point) <= 0) return false
        if (left.dot(point) <= 0) return false
        if (right.dot(point) <= 0) return false
        if (top.dot(point) <= 0) return false
        if (bottom.dot(point) <= 0) return false
        return if (near.dot(point) <= 0) false else true
    }

    /**
     * 确定线段是否与该视锥相交。
     */
    fun intersectsSegment(pointA: Vec3?, pointB: Vec3?): Boolean {
        if (pointA == null || pointB == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"))
        }
        // First do a trivial accept test.
        if (containsPoint(pointA) || containsPoint(pointB)) return true
        if (pointA == pointB) return false
        for (plane in planes) { // See if both points are behind the plane and therefore not in the frustum.
            //查看两个点是否都在平面后面，因此不在视锥中。
            if (plane.onSameSide(pointA, pointB) < 0) return false
            // See if the segment intersects the plane.
            // 查看线段是否与平面相交。
            if (plane.clip(pointA, pointB) != null) return true
        }
        return false // segment does not intersect frustum
    }

    override fun toString(): String {
        return "left={$left}, right={$right}, bottom={$bottom}, top={$top}, near={$near}, far={$far}"
    }
}