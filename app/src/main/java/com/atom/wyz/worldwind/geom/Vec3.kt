package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger
import java.nio.FloatBuffer

class Vec3(var x: Double, var y: Double, var z: Double) {

    companion object {
        /**
         * 将缓存中的点 xyz 变换成Vec3 且为所有点的平均值
         */
        fun averageOfBuffer(points: FloatBuffer?, stride: Int, result: Vec3?): Vec3 {
            if (points == null || points.remaining() < stride) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "Vec3", "averageOfBuffer", "missingBuffer"))
            }
            if (stride < 3) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "Vec3", "averageOfBuffer", "invalidStride"))
            }
            if (result == null) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "Vec3", "averageOfBuffer", "nullResult"))
            }
            result.x = 0.0
            result.y = 0.0
            result.z = 0.0
            val count = points.remaining() / stride
            val coords = FloatArray(stride)
            points.mark()
            for (i in 0 until count) {
                points[coords, 0, stride] // get the entire coordinate to advance the buffer position
                result.x += coords[0]
                result.y += coords[1]
                result.z += coords[2]
            }
            points.reset()
            result.x /= count.toDouble()
            result.y /= count.toDouble()
            result.z /= count.toDouble()
            return result
        }

        fun principalAxesOfBuffer(points: FloatBuffer?, stride: Int, result: Array<Vec3?>?): Array<Vec3?> {
            if (points == null || points.remaining() < stride) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "Vec3", "principalAxesOfBuffer", "missingBuffer"))
            }
            if (stride < 3) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "Vec3", "principalAxesOfBuffer", "invalidStride"))
            }
            if (result == null || result.size < 3 || result[0] == null || result[1] == null || result[2] == null) {
                throw IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Vec3", "principalAxesOfBuffer",
                        "nullResult"))
            }
            // Compute the covariance matrix.
            val covariance: Matrix4 = Matrix4().setToCovarianceOfBuffer(points, stride)
            // Compute the eigenvectors from the covariance matrix. The covariance matrix is symmetric by definition.
            covariance.eigensystemFromSymmetricMatrix(result)
            // Normalize the eigenvectors, which are already sorted in order of descending magnitude.
            result[0]?.normalize()
            result[1]?.normalize()
            result[2]?.normalize()
            return result
        }

    }

    constructor() : this(0.0, 0.0, 0.0)
    constructor(vector: Vec3) : this(vector.x, vector.y, vector.z)

    fun set(x: Double, y: Double, z: Double): Vec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(vector: Vec3?): Vec3? {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "set", "missingVector"))
        }
        x = vector.x
        y = vector.y
        z = vector.z
        return this
    }

    fun swap(vector: Vec3?): Vec3 {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "swap", "missingVector"))
        }
        var tmp = x
        x = vector.x
        vector.x = tmp
        tmp = y
        y = vector.y
        vector.y = tmp
        tmp = z
        z = vector.z
        vector.z = tmp
        return this
    }

    fun add(vector: Vec3?): Vec3 {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "add", "missingVector"))
        }
        x += vector.x
        y += vector.y
        z += vector.z
        return this
    }

    /**
     * 减去
     */
    fun subtract(vector: Vec3?): Vec3 {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "subtract", "missingVector"))
        }
        x -= vector.x
        y -= vector.y
        z -= vector.z
        return this
    }

    fun multiply(scalar: Double): Vec3 {
        x *= scalar
        y *= scalar
        z *= scalar
        return this
    }

    fun multiplyByMatrix(matrix: Matrix4?): Vec3 {
        if (matrix == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "multiplyByMatrix", "missingMatrix"))
        }
        val m: DoubleArray = matrix.m
        val x = m[0] * x + m[1] * y + m[2] * z + m[3]
        val y = m[4] * this.x + m[5] * y + m[6] * z + m[7]
        val z = m[8] * this.x + m[9] * this.y + m[10] * z + m[11]
        val w = m[12] * this.x + m[13] * this.y + m[14] * this.z + m[15]
        this.x = x / w
        this.y = y / w
        this.z = z / w
        return this
    }

    /**
     * 该点➗
     */
    fun divide(divisor: Double): Vec3 {
        x /= divisor
        y /= divisor
        z /= divisor
        return this
    }

    fun negate(): Vec3 {
        x = -x
        y = -y
        z = -z
        return this
    }

    fun normalize(): Vec3 {
        val magnitude = magnitude()
        val magnitudeInverse = 1 / magnitude
        x *= magnitudeInverse
        y *= magnitudeInverse
        z *= magnitudeInverse
        return this
    }

    /**
     * 点积
     */
    fun dot(vector: Vec3?): Double {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "dot", "missingVector"))
        }
        return x * vector.x + y * vector.y + z * vector.z
    }

    /**
     * 叉积
     */
    fun cross(vector: Vec3?): Vec3 {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "cross", "missingVector"))
        }
        val x: Double = y * vector.z - z * vector.y
        val y: Double = z * vector.x - this.x * vector.z
        val z: Double = this.x * vector.y - this.y * vector.x
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * 根据权重取两个点的数据组成一个新的点
     */
    fun mix(vector: Vec3?, weight: Double): Vec3 {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "mix", "missingVector"))
        }
        val w0 = 1 - weight
        x = x * w0 + vector.x * weight
        y = y * w0 + vector.y * weight
        z = z * w0 + vector.z * weight
        return this
    }

    /**
     * 获取平均点
     */
    fun averageOfList(vectors: List<Vec3>?): Vec3? {
        if (vectors == null || vectors.size == 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "averageOfList", "missingList"))
        }
        x = 0.0
        y = 0.0
        z = 0.0
        for (vec in vectors) {
            x += vec.x
            y += vec.y
            z += vec.z
        }
        val count = vectors.size
        x /= count.toDouble()
        y /= count.toDouble()
        z /= count.toDouble()
        return this
    }

    /**
     * 计算三点组成的三角形的法向量并赋值给自己
     */
    fun triangleNormal(a: Vec3?, b: Vec3?, c: Vec3?): Vec3 {
        if (a == null || b == null || c == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "triangleNormal", "missingVector"))
        }
        val x: Double = (b.y - a.y) * (c.z - a.z) - (b.z - a.z) * (c.y - a.y)
        val y: Double = (b.z - a.z) * (c.x - a.x) - (b.x - a.x) * (c.z - a.z)
        val z: Double = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
        var length = x * x + y * y + z * z
        if (length == 0.0) {
            this.x = x
            this.y = y
            this.z = z
        } else {
            length = Math.sqrt(length)
            this.x = x / length
            this.y = y / length
            this.z = z / length
        }
        return this
    }

    /**
     * 该位置和原点的距离
     */
    fun magnitude(): Double {
        return Math.sqrt(this.magnitudeSquared())
    }

    fun magnitudeSquared(): Double {
        return x * x + y * y + z * z
    }

    fun distanceToSquared(vector: Vec3?): Double {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "distanceToSquared", "missingVector"))
        }
        val dx: Double = x - vector.x
        val dy: Double = y - vector.y
        val dz: Double = z - vector.z
        return dx * dx + dy * dy + dz * dz
    }

    fun distanceTo(vector: Vec3?): Double {
        if (vector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Vec3", "distanceTo", "missingVector"))
        }
        return Math.sqrt(this.distanceToSquared(vector))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Vec3 = other as Vec3
        return x == that.x && y == that.y && z == that.z
    }
    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(z)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return x.toString() + ", " + y + ", " + z
    }

    /**
     * 将此向量的分量复制到指定的单精度数组。
     * 结果与GLSL统一向量兼容，并且可以传递给函数glUniform3fv。
     *
     * @param result a pre-allocated array of length 3 in which to return the components
     *
     * @return the result argument set to this vector's components
     */
    fun toArray(result: FloatArray?, offset: Int): FloatArray? {
        var offset_temp = offset
        if (result == null || result.size - offset < 3) {
            throw java.lang.IllegalArgumentException(
                     Logger.logMessage( Logger.ERROR, "Vec3", "toArray", "missingResult"))
        }
        result[offset_temp++] = x.toFloat()
        result[offset_temp++] = y.toFloat()
        result[offset_temp] = z.toFloat()
        return result
    }
}