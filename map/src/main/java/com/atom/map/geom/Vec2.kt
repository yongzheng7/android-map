package com.atom.map.geom

import com.atom.map.util.Logger

class Vec2(var x: Double, var y: Double) {

    constructor() : this(0.0, 0.0)
    constructor(vector: Vec2) : this(vector.x, vector.y)

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Vec2 = other as Vec2
        return x == that.x && y == that.y
    }

    override fun toString(): String {
        return x.toString() + ", " + y
    }

    /**
     * 距离原点的长度 标量
     */
    fun magnitude(): Double {
        return Math.sqrt(this.magnitudeSquared())
    }

    fun magnitudeSquared(): Double {
        return x * x + y * y
    }

    /**
     * 距离目标点的长度 标量
     */
    fun distanceTo(vector: Vec2): Double {
        return Math.sqrt(this.distanceToSquared(vector))
    }

    fun distanceToSquared(vector: Vec2): Double {
        val dx: Double = x - vector.x
        val dy: Double = y - vector.y
        return dx * dx + dy * dy
    }

    fun set(x: Double, y: Double): Vec2 {
        this.x = x
        this.y = y
        return this
    }

    fun set(vector: Vec2): Vec2 {
        return set(vector.x , vector.y)
    }

    /**
     * 对象交换并将数据赋给自己
     */
    fun swap(vector: Vec2): Vec2 {
        var tmp = x
        x = vector.x
        vector.x = tmp
        tmp = y
        y = vector.y
        vector.y = tmp
        return this
    }

    /**
     * add 加
     */
     fun add(vector: Vec2?): Vec2 {
        if (vector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec2", "add", "missingVector"))
        }
        x += vector.x
        y += vector.y
        return this
    }

    /**
     * 减 操作
     * TODO 可以优化为 -
     */
    fun subtract(vector: Vec2?): Vec2 {
        if (vector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec2", "subtract", "missingVector"))
        }
        x -= vector.x
        y -= vector.y
        return this
    }

    /**
     * 乘法 操作
     * TODO 可以优化为 *
     */
    fun multiply(scalar: Double): Vec2 {
        x *= scalar
        y *= scalar
        return this
    }

    fun multiplyByMatrix(matrix: Matrix3): Vec2 {
        val m: DoubleArray = matrix.m
        val x = m[0] * x + m[1] * y + m[2]
        val y = m[3] * this.x + m[4] * y + m[5]
        val z = m[6] * this.x + m[7] * this.y + m[8]
        this.x = x / z
        this.y = y / z
        return this
    }

    /**
     * 除法操作
     */
    fun divide(divisor: Double): Vec2 {
        x /= divisor
        y /= divisor
        return this
    }

    /**
     * 取反操作
     */
    fun negate(): Vec2 {
        x = -x
        y = -y
        return this
    }

    /**
     * 归一化操作 长度为 1
     */
    fun normalize(): Vec2 {
        val magnitude = magnitude()
        if (magnitude != 0.0) {
            x /= magnitude
            y /= magnitude
        }
        return this
    }

    /**
     * 计算点积操作
     */
    fun dot(vector: Vec2): Double {
        return x * vector.x + y * vector.y
    }

    /**
     * 插值根据权重操作
     */
    fun mix(vector: Vec2, weight: Double): Vec2 {
        val w0 = 1 - weight
        x = x * w0 + vector.x * weight
        y = y * w0 + vector.y * weight
        return this
    }


    /**
     * 将此向量的分量复制到指定的单精度数组。 结果与GLSL统一向量兼容，并且可以传递给函数glUniform2fv。
     *
     * @param result a pre-allocated array of length 2 in which to return the components
     *
     * @return the result argument set to this vector's components
     */
    fun toArray(result: FloatArray, offset: Int): FloatArray {
        var offset_temp = offset
        if (result.size - offset < 2) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec2", "toArray", "missingResult"))
        }
        result[offset_temp++] = x.toFloat()
        result[offset_temp] = y.toFloat()
        return result
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
}