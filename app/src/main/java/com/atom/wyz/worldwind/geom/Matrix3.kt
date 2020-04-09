package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger
import java.util.*

class Matrix3 {
    companion object {
        protected val identity = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
    }

    val m = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)

    constructor()
    constructor(m11: Double, m12: Double, m13: Double,
                m21: Double, m22: Double, m23: Double,
                m31: Double, m32: Double, m33: Double) {
        m[0] = m11
        m[1] = m12
        m[2] = m13

        m[3] = m21
        m[4] = m22
        m[5] = m23

        m[6] = m31
        m[7] = m32
        m[8] = m33
    }

    constructor(matrix: Matrix3?) {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "constructor", "missingMatrix"))
        }
        System.arraycopy(matrix.m, 0, m, 0, 9)
    }

    /**
     * 设置矩阵
     */
    fun set(m11: Double, m12: Double, m13: Double,
            m21: Double, m22: Double, m23: Double,
            m31: Double, m32: Double, m33: Double): Matrix3 {
        m[0] = m11
        m[1] = m12
        m[2] = m13
        m[3] = m21
        m[4] = m22
        m[5] = m23
        m[6] = m31
        m[7] = m32
        m[8] = m33
        return this
    }

    /**
     * 设置矩阵
     */
    fun set(matrix: Matrix3?): Matrix3 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "set", "missingMatrix"))
        }
        System.arraycopy(matrix.m, 0, m, 0, 9)
        return this
    }

    /**
     * 矩阵转置
     */
    fun transpose(): Matrix3 {
        val m = m
        var tmp = m[1]
        m[1] = m[3]
        m[3] = tmp
        tmp = m[2]
        m[2] = m[6]
        m[6] = tmp
        tmp = m[5]
        m[5] = m[7]
        m[7] = tmp
        return this
    }

    fun invert(): Matrix3? {
        throw UnsupportedOperationException("Matrix3.invert is not implemented") // TODO
    }

    /**
     * 矩阵平移 x y
     * 1 1 x
     * 1 1 y
     * 1 1 1
     */
    fun setTranslation(x: Double, y: Double): Matrix3 {
        m[2] = x
        m[5] = y
        return this
    }

    /**
     * 矩阵旋转 旋转角度
     * a b 1
     * c d 1
     * 1 1 1
     * a  cos(degress)
     * b -sin(degress)
     * c  sin(degress)
     * d  cos(degress)
     */
    fun setRotation(angleDegrees: Double): Matrix3 {
        val c = Math.cos(Math.toRadians(angleDegrees))
        val s = Math.sin(Math.toRadians(angleDegrees))
        m[0] = c
        m[1] = -s
        m[3] = s
        m[4] = c
        return this
    }

    /**
     * 设置缩放 x y
     * x 1 1
     * 1 y 1
     * 1 1 1
     */
    fun setScale(xScale: Double, yScale: Double): Matrix3 {
        m[0] = xScale
        m[4] = yScale
        return this
    }

    /**
     * 将此矩阵设置为3 x 3单位矩阵。
     */
    fun setToIdentity(): Matrix3 {
        m[0] = 1.0
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = 1.0
        m[5] = 0.0
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * 3x3 单位位移矩阵
     */
    fun setToTranslation(x: Double, y: Double): Matrix3 {
        m[0] = 1.0
        m[1] = 0.0
        m[2] = x
        m[3] = 0.0
        m[4] = 1.0
        m[5] = y
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * 3x3单位旋转矩阵
     */
    fun setToRotation(angleDegrees: Double): Matrix3 {
        val c = Math.cos(Math.toRadians(angleDegrees))
        val s = Math.sin(Math.toRadians(angleDegrees))
        m[0] = c
        m[1] = -s
        m[2] = 0.0
        m[3] = s
        m[4] = c
        m[5] = 0.0
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * 3x3 单位缩放矩阵
     */
    fun setToScale(xScale: Double, yScale: Double): Matrix3 {
        m[0] = xScale
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = yScale
        m[5] = 0.0
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * 设置单位垂直翻转矩阵
     */
    fun setToVerticalFlip(): Matrix3 {
        m[0] = 1.0
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = -1.0
        m[5] = 1.0
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * Sets this matrix to one that transforms normalized coordinates from a source sector to a destination sector.
     * Normalized coordinates within a sector range from 0 to 1, with (0, 0) indicating the lower left corner and (1, 1)
     * indicating the upper right. The resultant matrix maps a normalized source coordinate (X, Y) to its corresponding
     * normalized destination coordinate (X', Y').
     * <p/>
     * This matrix typically necessary to transform texture coordinates from one geographic region to another. For
     * example, the texture coordinates for a terrain tile spanning one region must be transformed to coordinates
     * appropriate for an image tile spanning a potentially different region.
     *
     * @param src the source sector
     * @param dst the destination sector
     *
     * @return this matrix set to values described above
     */
    fun setToTileTransform(src: Sector?, dst: Sector?): Matrix3 {
        if (src == null || dst == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Sector", "setToTileTransform", "missingSector"))
        }
        val srcDeltaLat: Double = src.deltaLatitude()
        val srcDeltaLon: Double = src.deltaLongitude()
        val dstDeltaLat: Double = dst.deltaLatitude()
        val dstDeltaLon: Double = dst.deltaLongitude()
        val xs = srcDeltaLon / dstDeltaLon
        val ys = srcDeltaLat / dstDeltaLat
        val xt: Double = (src.minLongitude - dst.minLongitude) / dstDeltaLon
        val yt: Double = (src.minLatitude - dst.minLatitude) / dstDeltaLat
        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     xs, 0, xt
        //     0, ys, yt,
        //     0, 0, 1);
        m[0] = xs
        m[1] = 0.0
        m[2] = xt

        m[3] = 0.0
        m[4] = ys
        m[5] = yt

        m[6] = 0.0
        m[7] = 0.0
        m[8] = 1.0
        return this
    }

    /**
     * 矩阵乘法 相当于 一行x一列
     */
    fun setToMultiply(a: Matrix3?, b: Matrix3?): Matrix3 {
        if (a == null || b == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "setToMultiply", "missingMatrix"))
        }
        val ma: DoubleArray = a.m
        val mb: DoubleArray = b.m
        m[0] = ma[0] * mb[0] + ma[1] * mb[3] + ma[2] * mb[6]
        m[1] = ma[0] * mb[1] + ma[1] * mb[4] + ma[2] * mb[7]
        m[2] = ma[0] * mb[2] + ma[1] * mb[5] + ma[2] * mb[8]
        m[3] = ma[3] * mb[0] + ma[4] * mb[3] + ma[5] * mb[6]
        m[4] = ma[3] * mb[1] + ma[4] * mb[4] + ma[5] * mb[7]
        m[5] = ma[3] * mb[2] + ma[4] * mb[5] + ma[5] * mb[8]
        m[6] = ma[6] * mb[0] + ma[7] * mb[3] + ma[8] * mb[6]
        m[7] = ma[6] * mb[1] + ma[7] * mb[4] + ma[8] * mb[7]
        m[8] = ma[6] * mb[2] + ma[7] * mb[5] + ma[8] * mb[8]
        return this
    }

    fun multiplyByRotation(angleDegrees: Double): Matrix3 {
        val c = Math.cos(Math.toRadians(angleDegrees))
        val s = Math.sin(Math.toRadians(angleDegrees))
        this.multiplyByMatrix(
                c, -s, 0.0,
                s, c, 0.0,
                0.0, 0.0, 1.0)
        return this
    }

    fun multiplyByTranslation(x: Double, y: Double): Matrix3 {
        this.multiplyByMatrix(1.0, 0.0, x, 0.0, 1.0, y, 0.0, 0.0, 1.0)
        return this
    }

    fun multiplyByScale(xScale: Double, yScale: Double): Matrix3 {
        this.multiplyByMatrix(
                xScale, 0.0, 0.0, 0.0, yScale, 0.0, 0.0, 0.0, 1.0)
        return this
    }

    fun multiplyByVerticalFlip(): Matrix3 {
        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //  1  0  0     1, 0, 0      1  0  0
        //  0  1  0     0, -1, 1,    0  -1  1
        //  0  0  1     0, 0, 1);    0  0   1
        //  1  0  0     1   0   0
        //  0  1  0     0  -1  1
        //  0  0  1     0   0   1

        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the matrix's components, reducing
        // the total number of primitive operations from 63 to 6.
        val m = m
        m[2] += m[1]
        m[5] += m[4]
        m[8] += m[7]

        m[1] = -m[1]
        m[4] = -m[4]
        m[7] = -m[7]

        return this
    }

    /**
     * Multiplies this matrix by a matrix that transforms normalized coordinates from a source sector to a destination
     * sector. Normalized coordinates within a sector range from 0 to 1, with (0, 0) indicating the lower left corner
     * and (1, 1) indicating the upper right. The resultant matrix maps a normalized source coordinate (X, Y) to its
     * corresponding normalized destination coordinate (X', Y').
     * <p/>
     *
     * This matrix typically necessary to transform texture coordinates from one geographic region to another. For
     * example, the texture coordinates for a terrain tile spanning one region must be transformed to coordinates
     * appropriate for an image tile spanning a potentially different region.
     *
     * 将此矩阵乘以一个矩阵，该矩阵将地理坐标中的标准化坐标从源矩形转换为目标矩形。
     * 扇区范围从0到1的归一化坐标，其中（0，0）指示左下角，（1，1）指示右上角。
     * 结果矩阵将归一化的源坐标（X，Y）映射到其对应的归一化的目标坐标（X'，Y'）。
     * 该矩阵通常是将纹理坐标从一个地理区域转换到另一个地理区域所必需的。
     * 例如，必须将跨越一个区域的地形图块的纹理坐标转换为适合于跨越潜在不同区域的图像图块的坐标。
     *
     * @param src the source sector
     * @param dst the destination sector
     *
     * @return this matrix multiplied by the transform matrix implied by values described above
     */
    fun multiplyByTileTransform(src: Sector?, dst: Sector?): Matrix3 {
        if (src == null || dst == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Sector", "multiplyByTileTransform", "missingSector"))
        }
        val srcDeltaLat: Double = src.deltaLatitude()
        val srcDeltaLon: Double = src.deltaLongitude()
        val dstDeltaLat: Double = dst.deltaLatitude()
        val dstDeltaLon: Double = dst.deltaLongitude()
        val xs = srcDeltaLon / dstDeltaLon
        val ys = srcDeltaLat / dstDeltaLat
        val xt: Double = (src.minLongitude - dst.minLongitude) / dstDeltaLon
        val yt: Double = (src.minLatitude - dst.minLatitude) / dstDeltaLat
        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     xs, 0, xt
        //     0, ys, yt,
        //     0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the matrix's components, reducing
        // the total number of primitive operations from 63 to 18.
        val m = m
        // Must be done before modifying m0, m1, etc. below.
        m[2] += m[0] * xt + m[1] * yt
        m[5] += m[3] * xt + m[4] * yt
        m[8] += m[6] * xt + m[6] * yt
        m[0] *= xs
        m[1] *= ys
        m[3] *= xs
        m[4] *= ys
        m[6] *= xs
        m[7] *= ys
        return this
    }

    fun multiplyByMatrix(matrix: Matrix3?): Matrix3 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "multiplyByMatrix", "missingMatrix"))
        }
        val ma = m
        val mb: DoubleArray = matrix.m
        /**
         * ma   ma0  ma1  ma2
         *      ma3  ma4  ma5
         *      ma6  ma7  ma8
         *
         * mb   mb0  mb1  mb2
         *      mb3  mb4  mb5
         *      mb6  mb7  mb8
         */
        var ma0: Double
        var ma1: Double
        var ma2: Double
        ma0 = ma[0]
        ma1 = ma[1]
        ma2 = ma[2]
        ma[0] = ma0 * mb[0] + ma1 * mb[3] + ma2 * mb[6]
        ma[1] = ma0 * mb[1] + ma1 * mb[4] + ma2 * mb[7]
        ma[2] = ma0 * mb[2] + ma1 * mb[5] + ma2 * mb[8]
        ma0 = ma[3]
        ma1 = ma[4]
        ma2 = ma[5]
        ma[3] = ma0 * mb[0] + ma1 * mb[3] + ma2 * mb[6]
        ma[4] = ma0 * mb[1] + ma1 * mb[4] + ma2 * mb[7]
        ma[5] = ma0 * mb[2] + ma1 * mb[5] + ma2 * mb[8]
        ma0 = ma[6]
        ma1 = ma[7]
        ma2 = ma[8]
        ma[6] = ma0 * mb[0] + ma1 * mb[3] + ma2 * mb[6]
        ma[7] = ma0 * mb[1] + ma1 * mb[4] + ma2 * mb[7]
        ma[8] = ma0 * mb[2] + ma1 * mb[5] + ma2 * mb[8]
        return this
    }

    /**
     * 给当前矩阵进行矩阵变换操作
     */
    fun multiplyByMatrix(m11: Double, m12: Double, m13: Double,
                         m21: Double, m22: Double, m23: Double,
                         m31: Double, m32: Double, m33: Double): Matrix3 {
        val m = m
        var mr1: Double
        var mr2: Double
        var mr3: Double
        mr1 = m[0]
        mr2 = m[1]
        mr3 = m[2]
        m[0] = mr1 * m11 + mr2 * m21 + mr3 * m31
        m[1] = mr1 * m12 + mr2 * m22 + mr3 * m32
        m[2] = mr1 * m13 + mr2 * m23 + mr3 * m33
        mr1 = m[3]
        mr2 = m[4]
        mr3 = m[5]
        m[3] = mr1 * m11 + mr2 * m21 + mr3 * m31
        m[4] = mr1 * m12 + mr2 * m22 + mr3 * m32
        m[5] = mr1 * m13 + mr2 * m23 + mr3 * m33
        mr1 = m[6]
        mr2 = m[7]
        mr3 = m[8]
        m[6] = mr1 * m11 + mr2 * m21 + mr3 * m31
        m[7] = mr1 * m12 + mr2 * m22 + mr3 * m32
        m[8] = mr1 * m13 + mr2 * m23 + mr3 * m33
        return this
    }

    /**
     * 颠倒矩阵
     * 0 1 2    0 3 6
     * 3 4 5 -> 1 4 7
     * 6 7 8    2 5 8
     */
    fun transposeMatrix(matrix: Matrix3?): Matrix3 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "transposeMatrix", "missingMatrix"))
        }
        m[0] = matrix.m.get(0)
        m[1] = matrix.m.get(3)
        m[2] = matrix.m.get(6)
        m[3] = matrix.m.get(1)
        m[4] = matrix.m.get(4)
        m[5] = matrix.m.get(7)
        m[6] = matrix.m.get(2)
        m[7] = matrix.m.get(5)
        m[8] = matrix.m.get(8)
        return this
    }

    fun invertMatrix(matrix: Matrix3?): Matrix3? {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix3", "invertMatrix", "missingMatrix"))
        }
        throw java.lang.UnsupportedOperationException("Matrix3.invertMatrix is not implemented") // TODO
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Matrix3 = other as Matrix3
        return m[0] == that.m.get(0) && m[1] == that.m.get(1) && m[2] == that.m.get(2) && m[3] == that.m.get(3) && m[4] == that.m.get(4) && m[5] == that.m.get(5) && m[6] == that.m.get(6) && m[7] == that.m.get(7) && m[8] == that.m.get(8)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(this.m)
    }

    override fun toString(): String{
        return  " \n"+"[" + m[0] + ", " + m[1] + ", " + m[2] + "] \n" +
                '[' + m[3] + ", " + m[4] + ", " + m[5] + "] \n" +
                '[' + m[6] + ", " + m[7] + ", " + m[8] + ']'
    }

    /**
     * 转置此矩阵，将结果存储在指定的单精度数组中。
     * 结果与GLSL统一矩阵兼容，并且可以传递给函数glUniformMatrix3fv。
     * 0 1 2
     * 3 4 5
     * 6 7 8
     * 转换
     *  0  1  2  3
     *  4  5  6  7
     *  8  9 10 11
     * 12 13 14 15
     */
    fun transposeToArray(result: FloatArray?, offset: Int): FloatArray {
        var offset_temp = offset
        if (result == null || result.size - offset < 9) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Matrix4", "transposeToArray", "missingResult"))
        }
        result[offset_temp++] = m[0].toFloat()
        result[offset_temp++] = m[3].toFloat()
        result[offset_temp++] = m[6].toFloat()
        result[offset_temp++] = m[1].toFloat()
        result[offset_temp++] = m[4].toFloat()
        result[offset_temp++] = m[7].toFloat()
        result[offset_temp++] = m[2].toFloat()
        result[offset_temp++] = m[5].toFloat()
        result[offset_temp] = m[8].toFloat()
        return result
    }
}