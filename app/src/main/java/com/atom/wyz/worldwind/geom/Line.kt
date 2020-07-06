package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Line {

    val origin: Vec3 = Vec3()

    val direction: Vec3 = Vec3()

    constructor()

    constructor(origin: Vec3?, direction: Vec3?) {
        if (origin == null) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Line", "constructor",
                    "Origin is null or undefined."
                )
            )
        }
        if (direction == null) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Line", "constructor",
                    "Direction is null or undefined."
                )
            )
        }
        this.origin.set(origin)
        this.direction.set(direction)
    }

    operator fun set(origin: Vec3?, direction: Vec3?): Line? {
        if (origin == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "set", "The origin is null")
            )
        }
        if (direction == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "set", "The direction is null")
            )
        }
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    /**
     * Computes a Cartesian point a specified distance along this line.
     * 该线✖️一个系数 的新的点
     *
     * @param distance The distance from this line's origin at which to compute the point.
     * @param result   A pre-allocated [Vec3] instance in which to return the computed point.
     *
     * @return The specified result argument containing the computed point.
     *
     * @throws IllegalArgumentException If the specified result argument is null or undefined.
     */
    fun pointAt(distance: Double, result: Vec3): Vec3 {
        result.x = origin.x + direction.x * distance
        result.y = origin.y + direction.y * distance
        result.z = origin.z + direction.z * distance
        return result
    }

    /**
     * 将此行设置为指定的段。 这条线的起点在第一个端点，方向从第一个端点延伸到第二个端点。
     */
    fun setToSegment(pointA: Vec3?, pointB: Vec3?): Line {
        if (pointA == null || pointB == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "setToSegment", "missingVector")
            )
        }
        this.origin.set(pointA)
        this.direction.set(pointB.x - pointA.x, pointB.y - pointA.y, pointB.z - pointA.z)
        return this
    }


    fun Line(line: Line?) {
        if (line == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "constructor", "missingLine")
            )
        }
        origin.set(line.origin)
        direction.set(line.direction)
    }

    fun triStripIntersection(
        points: FloatArray?,
        stride: Int,
        elements: ShortArray?,
        count : Int ,
        result: Vec3?
    ): Boolean {
        require(!(points == null || points.size < stride)) {
            Logger.logMessage(
                Logger.ERROR,
                "Line",
                "triStripIntersections",
                "missingBuffer"
            )
        }
        require(stride >= 3) { Logger.logMessage(Logger.ERROR, "Line", "triStripIntersections", "invalidStride") }
        require(!(elements == null || elements.size == 0)) {
            Logger.logMessage(
                Logger.ERROR,
                "Line",
                "triStripIntersections",
                "missingBuffer"
            )
        }
        requireNotNull(result) { Logger.logMessage(Logger.ERROR, "Line", "triStripIntersections", "missingResult") }

        val vx = direction.x
        val vy = direction.y
        val vz = direction.z
        val sx = origin.x
        val sy = origin.y
        val sz = origin.z
        var tMin = Double.POSITIVE_INFINITY
        val EPSILON = 0.00001
        // 获取三角形的第一个定点
        // Get the triangle strip's first vertex.
        var vertex = elements[0] * stride
        var vert1x = points[vertex++].toDouble()
        var vert1y = points[vertex++].toDouble()
        var vert1z = points[vertex].toDouble()
        // 获取三角形的第2个定点
        // Get the triangle strip's second vertex.
        vertex = elements[1] * stride
        var vert2x = points[vertex++].toDouble()
        var vert2y = points[vertex++].toDouble()
        var vert2z = points[vertex].toDouble()
        // 计算每个三角形与指定射线的交点。
        var idx = 2
        while (idx < count) {

            val vert0x = vert1x
            val vert0y = vert1y
            val vert0z = vert1z
            vert1x = vert2x
            vert1y = vert2y
            vert1z = vert2z
            // Get the triangle strip's next vertex.
            vertex = elements[idx] * stride
            vert2x = points[vertex++].toDouble()
            vert2y = points[vertex++].toDouble()
            vert2z = points[vertex].toDouble()
            // find vectors for two edges sharing point a: vert1 - vert0 and vert2 - vert0
            val edge1x = vert1x - vert0x
            val edge1y = vert1y - vert0y
            val edge1z = vert1z - vert0z
            val edge2x = vert2x - vert0x
            val edge2y = vert2y - vert0y
            val edge2z = vert2z - vert0z
            // Compute cross product of line direction and edge2
            val px = vy * edge2z - vz * edge2y
            val py = vz * edge2x - vx * edge2z
            val pz = vx * edge2y - vy * edge2x
            // Get determinant
            val det = edge1x * px + edge1y * py + edge1z * pz // edge1 dot p
            if (det > -EPSILON && det < EPSILON) { // if det is near zero then ray lies in plane of triangle
                idx++
                continue
            }
            val inv_det = 1.0 / det
            // Compute distance for vertex A to ray origin: origin - vert0
            val tx = sx - vert0x
            val ty = sy - vert0y
            val tz = sz - vert0z
            // Calculate u parameter and test bounds: 1/det * t dot p
            val u = inv_det * (tx * px + ty * py + tz * pz)
            if (u < -EPSILON || u > 1 + EPSILON) {
                idx++
                continue
            }
            // Prepare to test v parameter: tvec cross edge1
            val qx = ty * edge1z - tz * edge1y
            val qy = tz * edge1x - tx * edge1z
            val qz = tx * edge1y - ty * edge1x
            // Calculate v parameter and test bounds: 1/det * dir dot q
            val v = inv_det * (vx * qx + vy * qy + vz * qz)
            if (v < -EPSILON || u + v > 1 + EPSILON) {
                idx++
                continue
            }
            // Calculate the point of intersection on the line: t = 1/det * edge2 dot q
            val t = inv_det * (edge2x * qx + edge2y * qy + edge2z * qz)
            if (t >= 0 && t < tMin) {
                tMin = t
            }
            idx++
        }
        if (tMin != Double.POSITIVE_INFINITY) {
            result.set(sx + vx * tMin, sy + vy * tMin , sz + vz * tMin)
        }
        return tMin != Double.POSITIVE_INFINITY
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val line: Line = other as Line
        return if (origin != line.origin) false else direction == line.direction
    }

    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 31 * result + direction.hashCode()
        return result
    }

    override fun toString(): String {
        return "origin=[$origin], direction=[$direction]"
    }
}