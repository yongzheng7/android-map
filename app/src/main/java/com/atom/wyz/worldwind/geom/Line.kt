package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Line {

    /**
     * 线的原点
     */
    val origin: Vec3 = Vec3()

    /**
     * 从原点到结尾距离xyz
     */
    val direction: Vec3 = Vec3()

    /**
     * Constructs a line from a specified origin and direction.
     *
     * @param origin    The line's origin.
     * @param direction The line's direction.
     *
     * @throws IllegalArgumentException If either the origin or the direction are null or undefined.
     */
    constructor()

    constructor(origin: Vec3?, direction: Vec3?) {
        if (origin == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "constructor",
                            "Origin is null or undefined."))
        }
        if (direction == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "constructor",
                            "Direction is null or undefined."))
        }
        this.origin.set(origin)
        this.direction.set(direction)
    }

    operator fun set(origin: Vec3?, direction: Vec3?): Line? {
        if (origin == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "set", "The origin is null"))
        }
        if (direction == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "set", "The direction is null"))
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
    fun pointAt(distance: Double, result: Vec3?): Vec3 {
        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "pointAt", "missingResult."))
        }
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
                    Logger.logMessage(Logger.ERROR, "Line", "setToSegment", "missingVector"))
        }
        this.origin.set(pointA)
        this.direction.set(pointB.x - pointA.x, pointB.y - pointA.y, pointB.z - pointA.z)
        return this
    }


    fun Line(line: Line?) {
        if (line == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Line", "constructor", "missingLine"))
        }
        origin.set(line.origin)
        direction.set(line.direction)
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