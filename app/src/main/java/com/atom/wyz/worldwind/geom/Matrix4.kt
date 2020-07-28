package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger
import java.util.*


class Matrix4 {

    companion object {

        val identity = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0)
        /**
         * 具有LU分解矩阵的线性系统的实用方法
         */
        fun lubksb(A: Array<DoubleArray>, index: IntArray, b: DoubleArray) {
            var ii = -1
            run {
                var i = 0
                while (i < 4) {
                    val ip = index[i]
                    var sum = b[ip]
                    b[ip] = b[i]
                    if (ii != -1) {
                        var j = ii
                        while (j <= i - 1) {
                            sum -= A[i][j] * b[j]
                            j += 1
                        }
                    } else if (sum != 0.0) {
                        ii = i
                    }
                    b[i] = sum
                    i += 1
                }
            }
            var i = 3
            while (i >= 0) {
                var sum = b[i]
                var j = i + 1
                while (j < 4) {
                    sum -= A[i][j] * b[j]
                    j += 1
                }
                b[i] = sum / A[i][i]
                i -= 1
            }
        }

        /**
         *  在线性代数中，LU分解(LU Decomposition)是矩阵分解的一种，
         *  可以将一个矩阵分解为一个单位下三角矩阵和一个上三角矩阵的乘积（有时是它们和一个置换矩阵的乘积）。
         *  LU分解主要应用在数值分析中，用来解线性方程、求反矩阵或计算行列式。
         */
        fun ludcmp(A: Array<DoubleArray>, index: IntArray): Double {
            val TINY = 1.0e-20
            val vv = DoubleArray(4)
            var d = 1.0
            var temp: Double
            var sum: Double
            var i = 0
            while (i < 4) {
                var big = 0.0
                var j = 0
                while (j < 4) {
                    if (Math.abs(A[i][j]).also { temp = it } > big) {
                        big = temp
                    }
                    j += 1
                }
                if (big == 0.0) {
                    return 0.0 // Matrix is singular if the entire row contains zero.
                } else {
                    vv[i] = 1 / big
                }
                i += 1
            }
            var j = 0
            while (j < 4) {
                run {
                    i = 0
                    while (i < j) {
                        sum = A[i][j]
                        var k = 0
                        while (k < i) {
                            sum -= A[i][k] * A[k][j]
                            k += 1
                        }
                        A[i][j] = sum
                        i += 1
                    }
                }
                var big = 0.0
                var dum: Double
                var imax = -1
                i = j
                while (i < 4) {
                    sum = A[i][j]
                    for (k in 0 until j) {
                        sum -= A[i][k] * A[k][j]
                    }
                    A[i][j] = sum
                    if (vv[i] * Math.abs(sum).also { dum = it } >= big) {
                        big = dum
                        imax = i
                    }
                    i += 1
                }
                if (j != imax) {
                    var k = 0
                    while (k < 4) {
                        dum = A[imax][k]
                        A[imax][k] = A[j][k]
                        A[j][k] = dum
                        k += 1
                    }
                    d = -d
                    vv[imax] = vv[j]
                }
                index[j] = imax
                if (A[j][j] == 0.0) A[j][j] = TINY
                if (j != 3) {
                    dum = 1.0 / A[j][j]
                    i = j + 1
                    while (i < 4) {
                        A[i][j] *= dum
                        i += 1
                    }
                }
                j += 1
            }
            return d
        }

        /**
         * 反转4 x 4矩阵，将结果存储在目标参数中。
         * 源参数和目标参数表示一个4 x 4矩阵，具有按行优先顺序排列的一维数组。
         * 源和目标可以引用相同的数组。
         */
        fun invert(
            src: DoubleArray,
            dst: DoubleArray
        ): Boolean { // Copy the specified matrix into a mutable two-dimensional array.
            val A = Array(4) { DoubleArray(4) }
            A[0][0] = src[0]
            A[0][1] = src[1]
            A[0][2] = src[2]
            A[0][3] = src[3]
            A[1][0] = src[4]
            A[1][1] = src[5]
            A[1][2] = src[6]
            A[1][3] = src[7]
            A[2][0] = src[8]
            A[2][1] = src[9]
            A[2][2] = src[10]
            A[2][3] = src[11]
            A[3][0] = src[12]
            A[3][1] = src[13]
            A[3][2] = src[14]
            A[3][3] = src[15]
            val index = IntArray(4)
            var d: Double = ludcmp(A, index)
            // Compute the matrix's determinant.
            var i = 0
            while (i < 4) {
                d *= A[i][i]
                i += 1
            }
            // The matrix is singular if its determinant is zero or very close to zero.
            val NEAR_ZERO_THRESHOLD = 1.0e-8
            if (Math.abs(d) < NEAR_ZERO_THRESHOLD) {
                return false
            }
            val Y = Array(4) { DoubleArray(4) }
            val col = DoubleArray(4)
            var j = 0
            while (j < 4) {
                run {
                    i = 0
                    while (i < 4) {
                        col[i] = 0.0
                        i += 1
                    }
                }
                col[j] = 1.0
                lubksb(A, index, col)
                i = 0
                while (i < 4) {
                    Y[i][j] = col[i]
                    i += 1
                }
                j += 1
            }
            dst[0] = Y[0][0]
            dst[1] = Y[0][1]
            dst[2] = Y[0][2]
            dst[3] = Y[0][3]
            dst[4] = Y[1][0]
            dst[5] = Y[1][1]
            dst[6] = Y[1][2]
            dst[7] = Y[1][3]
            dst[8] = Y[2][0]
            dst[9] = Y[2][1]
            dst[10] = Y[2][2]
            dst[11] = Y[2][3]
            dst[12] = Y[3][0]
            dst[13] = Y[3][1]
            dst[14] = Y[3][2]
            dst[15] = Y[3][3]
            return true
        }
    }

    val m = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0)

    constructor() {
    }

    constructor(
        m11: Double, m12: Double, m13: Double, m14: Double,
        m21: Double, m22: Double, m23: Double, m24: Double,
        m31: Double, m32: Double, m33: Double, m34: Double,
        m41: Double, m42: Double, m43: Double, m44: Double
    ) {
        m[0] = m11
        m[1] = m12
        m[2] = m13
        m[3] = m14
        m[4] = m21
        m[5] = m22
        m[6] = m23
        m[7] = m24
        m[8] = m31
        m[9] = m32
        m[10] = m33
        m[11] = m34
        m[12] = m41
        m[13] = m42
        m[14] = m43
        m[15] = m44
    }

    constructor(matrix: Matrix4?) {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "constructor", "missingMatrix")
            )
        }
        System.arraycopy(matrix.m, 0, m, 0, 16)
    }

    operator fun set(
        m11: Double, m12: Double, m13: Double, m14: Double,
        m21: Double, m22: Double, m23: Double, m24: Double,
        m31: Double, m32: Double, m33: Double, m34: Double,
        m41: Double, m42: Double, m43: Double, m44: Double
    ): Matrix4 {
        m[0] = m11
        m[1] = m12
        m[2] = m13
        m[3] = m14
        m[4] = m21
        m[5] = m22
        m[6] = m23
        m[7] = m24
        m[8] = m31
        m[9] = m32
        m[10] = m33
        m[11] = m34
        m[12] = m41
        m[13] = m42
        m[14] = m43
        m[15] = m44
        return this
    }

    /**
     * 矩阵转置
     */
    fun transpose(): Matrix4 {
        val m = m
        var tmp = m[1]
        m[1] = m[4]
        m[4] = tmp
        tmp = m[2]
        m[2] = m[8]
        m[8] = tmp
        tmp = m[3]
        m[3] = m[12]
        m[12] = tmp
        tmp = m[6]
        m[6] = m[9]
        m[9] = tmp
        tmp = m[7]
        m[7] = m[13]
        m[13] = tmp
        tmp = m[11]
        m[11] = m[14]
        m[14] = tmp
        return this
    }

    /**
     * 矩阵转置
     */
    fun invertMatrix(matrix: Matrix4?): Matrix4 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "missingMatrix")
            )
        }
        val success: Boolean = invert(matrix.m, this.m) // store inverse of matrix in this matrix
        if (!success) { // the matrix is singular
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "singularMatrix")
            )
        }
        return this
    }

    /**
     * 转置后归一化
     *  0  1  2  3
     *  4  5  6  7
     *  8  9 10 11
     * 12 13 14 15
     */
    fun invertOrthonormal(): Matrix4 {
        // This is assumed to contain matrix 3D transformation matrix. The upper 3x3 is inverted, the translation
        // components are multiplied by the inverted-upper-3x3 and negated.
        val m = m
        var tmp = m[1]
        m[1] = m[4]
        m[4] = tmp
        tmp = m[2]
        m[2] = m[8]
        m[8] = tmp
        tmp = m[6]
        m[6] = m[9]
        m[9] = tmp
        val x = m[3]
        val y = m[7]
        val z = m[11]
        m[3] = -(m[0] * x) - m[1] * y - m[2] * z
        m[7] = -(m[4] * x) - m[5] * y - m[6] * z
        m[11] = -(m[8] * x) - m[9] * y - m[10] * z
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    fun invert(): Matrix4 {
        val success: Boolean = invert(this.m, this.m) // passing the same array as src and dst is supported
        if (!success) { // the matrix is singular
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "singularMatrix")
            )
        }
        return this
    }

    fun setTranslation(x: Double, y: Double, z: Double): Matrix4 {
        m[3] = x
        m[7] = y
        m[11] = z
        return this
    }

    fun setRotation(x: Double, y: Double, z: Double, angleDegrees: Double): Matrix4 {
        val toRadians = Math.toRadians(angleDegrees);
        val c = Math.cos(toRadians)
        val s = Math.sin(toRadians)
        m[0] = c + (1 - c) * x * x
        m[1] = (1 - c) * x * y - s * z
        m[2] = (1 - c) * x * z + s * y
        m[4] = (1 - c) * x * y + s * z
        m[5] = c + (1 - c) * y * y
        m[6] = (1 - c) * y * z - s * x
        m[8] = (1 - c) * x * z - s * y
        m[9] = (1 - c) * y * z + s * x
        m[10] = c + (1 - c) * z * z
        return this
    }

    fun setScale(xScale: Double, yScale: Double, zScale: Double): Matrix4 {
        m[0] = xScale
        m[5] = yScale
        m[10] = zScale
        return this
    }

    /**
     * 设置单位矩阵
     * 1 0 0 0
     * 0 1 0 0
     * 0 0 1 0
     * 0 0 0 1
     */
    fun setToIdentity(): Matrix4 {
        m[0] = 1.0
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = 0.0
        m[5] = 1.0
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 0.0
        m[9] = 0.0
        m[10] = 1.0
        m[11] = 0.0
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     * 设置单位位移矩阵
     * 1 0 0 x
     * 0 1 0 y
     * 0 0 1 z
     * 0 0 0 1
     */
    fun setToTranslation(x: Double, y: Double, z: Double): Matrix4 {
        m[0] = 1.0
        m[1] = 0.0
        m[2] = 0.0
        m[3] = x
        m[4] = 0.0
        m[5] = 1.0
        m[6] = 0.0
        m[7] = y
        m[8] = 0.0
        m[9] = 0.0
        m[10] = 1.0
        m[11] = z
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     * 单位旋转矩阵
     * x x x 0
     * x x x 0
     * x x x 0
     * 0 0 0 1
     */
    fun setToRotation(x: Double, y: Double, z: Double, angleDegrees: Double): Matrix4 {
        val toRadians = Math.toRadians(angleDegrees);
        val c = Math.cos(toRadians)
        val s = Math.sin(toRadians)
        m[0] = c + (1 - c) * x * x
        m[1] = (1 - c) * x * y - s * z
        m[2] = (1 - c) * x * z + s * y
        m[3] = 0.0
        m[4] = (1 - c) * x * y + s * z
        m[5] = c + (1 - c) * y * y
        m[6] = (1 - c) * y * z - s * x
        m[7] = 0.0
        m[8] = (1 - c) * x * z - s * y
        m[9] = (1 - c) * y * z + s * x
        m[10] = c + (1 - c) * z * z
        m[11] = 0.0
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     * 设置缩放矩阵
     * x 0 0 0
     * 0 y 0 0
     * 0 0 z 0
     * 0 0 0 1
     */
    fun setToScale(xScale: Double, yScale: Double, zScale: Double): Matrix4 {
        m[0] = xScale
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = 0.0
        m[5] = yScale
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 0.0
        m[9] = 0.0
        m[10] = zScale
        m[11] = 0.0
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     * 矩阵的乘法 一行x一列
     */
    fun setToMultiply(a: Matrix4, b: Matrix4): Matrix4 {
        val ma: DoubleArray = a.m
        val mb: DoubleArray = b.m
        m[0] = ma[0] * mb[0] + ma[1] * mb[4] + ma[2] * mb[8] + ma[3] * mb[12]
        m[1] = ma[0] * mb[1] + ma[1] * mb[5] + ma[2] * mb[9] + ma[3] * mb[13]
        m[2] = ma[0] * mb[2] + ma[1] * mb[6] + ma[2] * mb[10] + ma[3] * mb[14]
        m[3] = ma[0] * mb[3] + ma[1] * mb[7] + ma[2] * mb[11] + ma[3] * mb[15]
        m[4] = ma[4] * mb[0] + ma[5] * mb[4] + ma[6] * mb[8] + ma[7] * mb[12]
        m[5] = ma[4] * mb[1] + ma[5] * mb[5] + ma[6] * mb[9] + ma[7] * mb[13]
        m[6] = ma[4] * mb[2] + ma[5] * mb[6] + ma[6] * mb[10] + ma[7] * mb[14]
        m[7] = ma[4] * mb[3] + ma[5] * mb[7] + ma[6] * mb[11] + ma[7] * mb[15]
        m[8] = ma[8] * mb[0] + ma[9] * mb[4] + ma[10] * mb[8] + ma[11] * mb[12]
        m[9] = ma[8] * mb[1] + ma[9] * mb[5] + ma[10] * mb[9] + ma[11] * mb[13]
        m[10] = ma[8] * mb[2] + ma[9] * mb[6] + ma[10] * mb[10] + ma[11] * mb[14]
        m[11] = ma[8] * mb[3] + ma[9] * mb[7] + ma[10] * mb[11] + ma[11] * mb[15]
        m[12] = ma[12] * mb[0] + ma[13] * mb[4] + ma[14] * mb[8] + ma[15] * mb[12]
        m[13] = ma[12] * mb[1] + ma[13] * mb[5] + ma[14] * mb[9] + ma[15] * mb[13]
        m[14] = ma[12] * mb[2] + ma[13] * mb[6] + ma[14] * mb[10] + ma[15] * mb[14]
        m[15] = ma[12] * mb[3] + ma[13] * mb[7] + ma[14] * mb[11] + ma[15] * mb[15]
        return this
    }

    /**
     * 设置该矩阵为视口透视矩阵
     * x 0 0 0
     * x 0 0 0
     * 0 0 x x
     * 0 0 x 0
     */
    fun setToPerspectiveProjection(
        viewportWidth: Double, viewportHeight: Double,
        fovyDegrees: Double,
        nearDistance: Double, farDistance: Double
    ): Matrix4 {
        if (viewportWidth <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                    "invalidWidth"
                )
            )
        }
        if (viewportHeight <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                    "invalidHeight"
                )
            )
        }
        if (fovyDegrees <= 0 || fovyDegrees > 180) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                    "Invalid field of view"
                )
            )
        }
        if (nearDistance == farDistance) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                    "Near distance or far distance is invalid"
                )
            )
        }
        if (nearDistance <= 0 || farDistance <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                    "Near distance or far distance is invalid"
                )
            )
        }
        // Compute the dimensions of the near rectangle given the specified parameters.
        val aspect = viewportWidth / viewportHeight
        val tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5))
        val nearHeight = 2 * nearDistance * tanfovy_2
        val nearWidth = nearHeight * aspect

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition, equation 4.52.
        m[0] = (2 * nearDistance) / nearWidth
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = 0.0
        m[5] = (2 * nearDistance) / nearHeight
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 0.0
        m[9] = 0.0
        m[10] = -(farDistance + nearDistance) / (farDistance - nearDistance)
        m[11] = -(2 * nearDistance * farDistance) / (farDistance - nearDistance)
        m[12] = 0.0
        m[13] = 0.0
        m[14] = (-1).toDouble()
        m[15] = 0.0
        return this
    }


    fun setToScreenProjection(viewportWidth: Double, viewportHeight: Double): Matrix4? {
        if (viewportWidth <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToScreenProjection",
                    "invalidWidth"
                )
            )
        }
        if (viewportHeight <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "Matrix4", "setToScreenProjection",
                    "invalidHeight"
                )
            )
        }
        /**
         * m[0]   0       0   -1
         * 0      m[5]    0   -1
         * 0      0       2   -1
         * 0      0       0   -1
         */
        m[0] = 2 / viewportWidth
        m[1] = 0.0
        m[2] = 0.0
        m[3] = -1.0
        m[4] = 0.0
        m[5] = 2 / viewportHeight
        m[6] = 0.0
        m[7] = -1.0
        m[8] = 0.0
        m[9] = 0.0
        m[10] = 2.0
        m[11] = -1.0
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     *  0  1  2  3
     *  4  5  6  7
     *  8  9  10 11
     *  12 13 14 15
     */
    fun setToCovarianceOfPoints(array: FloatArray, count: Int, stride: Int): Matrix4 {
        var dx: Double
        var dy: Double
        var dz: Double
        var mx = 0.0
        var my = 0.0
        var mz = 0.0
        var c11 = 0.0
        var c22 = 0.0
        var c33 = 0.0
        var c12 = 0.0
        var c13 = 0.0
        var c23 = 0.0
        var numPoints = 0.0
        var idx = 0
        while (idx < count) {
            mx += array[idx]
            my += array[idx + 1]
            mz += array[idx + 2]
            numPoints++
            idx += stride
        }
        mx /= numPoints
        my /= numPoints
        mz /= numPoints

        idx = 0
        while (idx < count) {
            dx = array[idx    ] - mx
            dy = array[idx + 1] - my
            dz = array[idx + 2] - mz
            c11 += dx * dx
            c22 += dy * dy
            c33 += dz * dz
            c12 += dx * dy // c12 = c21
            c13 += dx * dz // c13 = c31
            c23 += dy * dz // c23 = c32
            idx += stride
        }

        m[0] = c11 / numPoints
        m[1] = c12 / numPoints
        m[2] = c13 / numPoints
        m[3] = 0.0

        m[4] = c12 / numPoints
        m[5] = c22 / numPoints
        m[6] = c23 / numPoints
        m[7] = 0.0

        m[8] = c13 / numPoints
        m[9] = c23 / numPoints
        m[10] = c33 / numPoints
        m[11] = 0.0

        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 0.0
        return this
    }

    /**
     * 矩阵位移
     */
    fun multiplyByTranslation(x: Double, y: Double, z: Double): Matrix4 {

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     1, 0, 0, x,
        //     0, 1, 0, y,
        //     0, 0, 1, z,
        //     0, 0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the translation matrix's components,
        // reducing the total number of primitive operations from 144 to 24.

        val m = m

        m[3] += m[0] * x + m[1] * y + m[2] * z
        m[7] += m[4] * x + m[5] * y + m[6] * z
        m[11] += m[8] * x + m[9] * y + m[10] * z
        m[15] += m[12] * x + m[13] * y + m[14] * z

        return this
    }

    /**
     * 矩阵旋转
     * x y z 两个值 1 0
     * 之后是角度
     */
    fun multiplyByRotation(x: Double, y: Double, z: Double, angleDegrees: Double): Matrix4 {
        val toRadians = Math.toRadians(angleDegrees)
        val c = Math.cos(toRadians)
        val s = Math.sin(toRadians)
        this.multiplyByMatrix(
            c + (1 - c) * x * x, (1 - c) * x * y - s * z, (1 - c) * x * z + s * y, 0.0,
            (1 - c) * x * y + s * z, c + (1 - c) * y * y, (1 - c) * y * z - s * x, 0.0,
            (1 - c) * x * z - s * y, (1 - c) * y * z + s * x, c + (1 - c) * z * z, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        return this
    }

    fun multiplyByScale(xScale: Double, yScale: Double, zScale: Double): Matrix4 {

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // this.multiplyByMatrix(
        //     xScale, 0, 0, 0,
        //     0, yScale, 0, 0,
        //     0, 0, zScale, 0,
        //     0, 0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the scale matrix's components,
        // reducing the total number of primitive operations from 144 to 12.
        val m = m

        m[0] *= xScale
        m[4] *= xScale
        m[8] *= xScale
        m[12] *= xScale

        m[1] *= yScale
        m[5] *= yScale
        m[9] *= yScale
        m[13] *= yScale

        m[2] *= zScale
        m[6] *= zScale
        m[10] *= zScale
        m[14] *= zScale

        return this
    }

    fun multiplyByMatrix(matrix: Matrix4?): Matrix4 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "multiplyByMatrix", "missingMatrix")
            )
        }
        val ma = m
        val mb: DoubleArray = matrix.m
        var ma0: Double
        var ma1: Double
        var ma2: Double
        var ma3: Double
        ma0 = ma[0]
        ma1 = ma[1]
        ma2 = ma[2]
        ma3 = ma[3]
        ma[0] = ma0 * mb[0] + ma1 * mb[4] + ma2 * mb[8] + ma3 * mb[12]
        ma[1] = ma0 * mb[1] + ma1 * mb[5] + ma2 * mb[9] + ma3 * mb[13]
        ma[2] = ma0 * mb[2] + ma1 * mb[6] + ma2 * mb[10] + ma3 * mb[14]
        ma[3] = ma0 * mb[3] + ma1 * mb[7] + ma2 * mb[11] + ma3 * mb[15]
        ma0 = ma[4]
        ma1 = ma[5]
        ma2 = ma[6]
        ma3 = ma[7]
        ma[4] = ma0 * mb[0] + ma1 * mb[4] + ma2 * mb[8] + ma3 * mb[12]
        ma[5] = ma0 * mb[1] + ma1 * mb[5] + ma2 * mb[9] + ma3 * mb[13]
        ma[6] = ma0 * mb[2] + ma1 * mb[6] + ma2 * mb[10] + ma3 * mb[14]
        ma[7] = ma0 * mb[3] + ma1 * mb[7] + ma2 * mb[11] + ma3 * mb[15]
        ma0 = ma[8]
        ma1 = ma[9]
        ma2 = ma[10]
        ma3 = ma[11]
        ma[8] = ma0 * mb[0] + ma1 * mb[4] + ma2 * mb[8] + ma3 * mb[12]
        ma[9] = ma0 * mb[1] + ma1 * mb[5] + ma2 * mb[9] + ma3 * mb[13]
        ma[10] = ma0 * mb[2] + ma1 * mb[6] + ma2 * mb[10] + ma3 * mb[14]
        ma[11] = ma0 * mb[3] + ma1 * mb[7] + ma2 * mb[11] + ma3 * mb[15]
        ma0 = ma[12]
        ma1 = ma[13]
        ma2 = ma[14]
        ma3 = ma[15]
        ma[12] = ma0 * mb[0] + ma1 * mb[4] + ma2 * mb[8] + ma3 * mb[12]
        ma[13] = ma0 * mb[1] + ma1 * mb[5] + ma2 * mb[9] + ma3 * mb[13]
        ma[14] = ma0 * mb[2] + ma1 * mb[6] + ma2 * mb[10] + ma3 * mb[14]
        ma[15] = ma0 * mb[3] + ma1 * mb[7] + ma2 * mb[11] + ma3 * mb[15]
        return this
    }

    fun multiplyByMatrix(
        m11: Double, m12: Double, m13: Double, m14: Double,
        m21: Double, m22: Double, m23: Double, m24: Double,
        m31: Double, m32: Double, m33: Double, m34: Double,
        m41: Double, m42: Double, m43: Double, m44: Double
    ): Matrix4 {
        val m = m
        var mr1: Double
        var mr2: Double
        var mr3: Double
        var mr4: Double
        mr1 = m[0]
        mr2 = m[1]
        mr3 = m[2]
        mr4 = m[3]
        m[0] = mr1 * m11 + mr2 * m21 + mr3 * m31 + mr4 * m41
        m[1] = mr1 * m12 + mr2 * m22 + mr3 * m32 + mr4 * m42
        m[2] = mr1 * m13 + mr2 * m23 + mr3 * m33 + mr4 * m43
        m[3] = mr1 * m14 + mr2 * m24 + mr3 * m34 + mr4 * m44
        mr1 = m[4]
        mr2 = m[5]
        mr3 = m[6]
        mr4 = m[7]
        m[4] = mr1 * m11 + mr2 * m21 + mr3 * m31 + mr4 * m41
        m[5] = mr1 * m12 + mr2 * m22 + mr3 * m32 + mr4 * m42
        m[6] = mr1 * m13 + mr2 * m23 + mr3 * m33 + mr4 * m43
        m[7] = mr1 * m14 + mr2 * m24 + mr3 * m34 + mr4 * m44
        mr1 = m[8]
        mr2 = m[9]
        mr3 = m[10]
        mr4 = m[11]
        m[8] = mr1 * m11 + mr2 * m21 + mr3 * m31 + mr4 * m41
        m[9] = mr1 * m12 + mr2 * m22 + mr3 * m32 + mr4 * m42
        m[10] = mr1 * m13 + mr2 * m23 + mr3 * m33 + mr4 * m43
        m[11] = mr1 * m14 + mr2 * m24 + mr3 * m34 + mr4 * m44
        mr1 = m[12]
        mr2 = m[13]
        mr3 = m[14]
        mr4 = m[15]
        m[12] = mr1 * m11 + mr2 * m21 + mr3 * m31 + mr4 * m41
        m[13] = mr1 * m12 + mr2 * m22 + mr3 * m32 + mr4 * m42
        m[14] = mr1 * m13 + mr2 * m23 + mr3 * m33 + mr4 * m43
        m[15] = mr1 * m14 + mr2 * m24 + mr3 * m34 + mr4 * m44
        return this
    }

    /**
     * 颠倒矩阵
     */
    fun transposeMatrix(matrix: Matrix4?): Matrix4 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "transposeMatrix", "missingMatrix")
            )
        }
        m[0] = matrix.m[0]
        m[1] = matrix.m[4]
        m[2] = matrix.m[8]
        m[3] = matrix.m[12]

        m[4] = matrix.m[1]
        m[5] = matrix.m[5]
        m[6] = matrix.m[9]
        m[7] = matrix.m[13]

        m[8] = matrix.m[2]
        m[9] = matrix.m[6]
        m[10] = matrix.m[10]
        m[11] = matrix.m[14]

        m[12] = matrix.m[3]
        m[13] = matrix.m[7]
        m[14] = matrix.m[11]
        m[15] = matrix.m[15]
        return this
    }

    /**
     * 倒置正交矩阵
     *  0  1  2  3
     *  4  5  6  7
     *  8  9 10 11
     * 12 13 14 15
     */
    fun invertOrthonormalMatrix(matrix: Matrix4?): Matrix4 {
        if (matrix == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertOrthonormalMatrix", "missingMatrix")
            )
        }
        // 'matrix' is assumed to contain matrix 3D transformation matrix.
        // Upper-3x3 is inverted, translation is transformed by inverted-upper-3x3 and negated.
        m[0] = matrix.m.get(0)
        m[1] = matrix.m.get(4)
        m[2] = matrix.m.get(8)
        m[3] =
            0.0 - matrix.m.get(0) * matrix.m.get(3) - matrix.m.get(4) * matrix.m.get(7) - matrix.m.get(8) * matrix.m.get(
                11
            )
        m[4] = matrix.m.get(1)
        m[5] = matrix.m.get(5)
        m[6] = matrix.m.get(9)
        m[7] =
            0.0 - matrix.m.get(1) * matrix.m.get(3) - matrix.m.get(5) * matrix.m.get(7) - matrix.m.get(9) * matrix.m.get(
                11
            )
        m[8] = matrix.m.get(2)
        m[9] = matrix.m.get(6)
        m[10] = matrix.m.get(10)
        m[11] =
            0.0 - matrix.m.get(2) * matrix.m.get(3) - matrix.m.get(6) * matrix.m.get(7) - matrix.m.get(10) * matrix.m.get(
                11
            )
        m[12] = 0.0
        m[13] = 0.0
        m[14] = 0.0
        m[15] = 1.0
        return this
    }

    /**
     * 设置深度
     *  0  1  2  3
     *  4  5  6  7
     *  8  9 10 11
     * 12 13 14 15
     */
    fun offsetProjectionDepth(depthOffset: Double): Matrix4 {
        m[10] *= 1 + depthOffset
        return this
    }

    /**
     * 提取视点位置
     */
    fun extractEyePoint(result: Vec3): Vec3 {
        // The eye point of a modelview matrix is computed by transforming the origin (0, 0, 0, 1) by the matrix's inverse.
        // This is equivalent to transforming the inverse of this matrix's translation components in the rightmost column by
        // the transpose of its upper 3x3 components.
        result.x = -(m[0] * m[3]) - m[4] * m[7] - m[8] * m[11]
        result.y = -(m[1] * m[3]) - m[5] * m[7] - m[9] * m[11]
        result.z = -(m[2] * m[3]) - m[6] * m[7] - m[10] * m[11]
        return result
    }

    fun extractForwardVector(result: Vec3): Vec3 {
        // The forward vector of a modelview matrix is computed by transforming the negative Z axis (0, 0, -1, 0) by the
        // matrix's inverse. We have pre-computed the result inline here to simplify this computation.
        result.x = -m[8]
        result.y = -m[9]
        result.z = -m[10]
        return result
    }

    fun extractEigenvectors(result1: Vec3, result2: Vec3, result3: Vec3): Boolean {
        if (m[1] != m[4] || m[2] != m[8] || m[6] != m[9]) {
            return false // matrix is not symmetric
        }

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition,
        // listing 14.6.
        val EPSILON = 1.0e-10
        val MAX_SWEEPS = 32
        // Since the matrix is symmetric m12=m21, m13=m31 and m23=m32, therefore we can ignore the values m21,
        // m32 and m32.
        var m11 = m[0]
        var m12 = m[1]
        var m13 = m[2]
        var m22 = m[5]
        var m23 = m[6]
        var m33 = m[10]
        val r = Array(3) { DoubleArray(3) }
        r[2][2] = 1.0
        r[1][1] = r[2][2]
        r[0][0] = r[1][1]
        for (a in 0 until MAX_SWEEPS) { // Exit if off-diagonal entries small enough
            if (Math.abs(m12) < EPSILON && Math.abs(m13) < EPSILON && Math.abs(m23) < EPSILON) {
                break
            }
            // Annihilate (1,2) entry.
            if (m12 != 0.0) {
                val u = (m22 - m11) * 0.5 / m12
                val u2 = u * u
                val u2p1 = u2 + 1
                val t = if (u2p1 != u2) (if (u < 0) -1 else 1) * (Math.sqrt(u2p1) - Math.abs(u)) else 0.5 / u
                val c = 1 / Math.sqrt(t * t + 1)
                val s = c * t
                m11 -= t * m12
                m22 += t * m12
                m12 = 0.0
                var temp = c * m13 - s * m23
                m23 = s * m13 + c * m23
                m13 = temp
                for (i in 0..2) {
                    temp = c * r[i][0] - s * r[i][1]
                    r[i][1] = s * r[i][0] + c * r[i][1]
                    r[i][0] = temp
                }
            }
            // Annihilate (1,3) entry.
            if (m13 != 0.0) {
                val u = (m33 - m11) * 0.5 / m13
                val u2 = u * u
                val u2p1 = u2 + 1
                val t = if (u2p1 != u2) (if (u < 0) -1 else 1) * (Math.sqrt(u2p1) - Math.abs(u)) else 0.5 / u
                val c = 1 / Math.sqrt(t * t + 1)
                val s = c * t
                m11 -= t * m13
                m33 += t * m13
                m13 = 0.0
                var temp = c * m12 - s * m23
                m23 = s * m12 + c * m23
                m12 = temp
                for (i in 0..2) {
                    temp = c * r[i][0] - s * r[i][2]
                    r[i][2] = s * r[i][0] + c * r[i][2]
                    r[i][0] = temp
                }
            }
            // Annihilate (2,3) entry.
            if (m23 != 0.0) {
                val u = (m33 - m22) * 0.5 / m23
                val u2 = u * u
                val u2p1 = u2 + 1
                val t = if (u2p1 != u2) (if (u < 0) -1 else 1) * (Math.sqrt(u2p1) - Math.abs(u)) else 0.5 / u
                val c = 1 / Math.sqrt(t * t + 1)
                val s = c * t
                m22 -= t * m23
                m33 += t * m23
                m23 = 0.0
                var temp = c * m12 - s * m13
                m13 = s * m12 + c * m13
                m12 = temp
                for (i in 0..2) {
                    temp = c * r[i][1] - s * r[i][2]
                    r[i][2] = s * r[i][1] + c * r[i][2]
                    r[i][1] = temp
                }
            }
        }
        // Sort the eigenvectors by descending magnitude.
        var i1 = 0
        var i2 = 1
        var i3 = 2
        var itemp: Int
        var temp: Double
        if (m11 < m22) {
            temp = m11
            m11 = m22
            m22 = temp
            itemp = i1
            i1 = i2
            i2 = itemp
        }
        if (m22 < m33) {
            temp = m22
            m22 = m33
            m33 = temp
            itemp = i2
            i2 = i3
            i3 = itemp
        }
        if (m11 < m22) {
            temp = m11
            m11 = m22
            m22 = temp
            itemp = i1
            i1 = i2
            i2 = itemp
        }
        result1.set(r[0][i1], r[1][i1], r[2][i1])
        result2.set(r[0][i2], r[1][i2], r[2][i2])
        result3.set(r[0][i3], r[1][i3], r[2][i3])

        result1.normalize()
        result2.normalize()
        result3.normalize()

        result1.multiply(m11)
        result2.multiply(m22)
        result3.multiply(m33)

        return true
    }

    fun set(matrix: Matrix4): Matrix4 {
        System.arraycopy(matrix.m, 0, this.m, 0, 16)
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Matrix4 = other as Matrix4
        return m[0] == that.m.get(0) && m[1] == that.m.get(1) && m[2] == that.m.get(2) && m[3] == that.m.get(3) && m[4] == that.m.get(
            4
        ) && m[5] == that.m.get(5) && m[6] == that.m.get(6) && m[7] == that.m.get(7) && m[8] == that.m.get(8) && m[9] == that.m.get(
            9
        ) && m[10] == that.m.get(10) && m[11] == that.m.get(11) && m[12] == that.m.get(12) && m[13] == that.m.get(13) && m[14] == that.m.get(
            14
        ) && m[15] == that.m.get(15)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(m)
    }

    /**
     * 返回此观察矩阵的方位角（以度为单位）。
     */
    fun extractHeading(roll: Double): Double {
        val rad = Math.toRadians(roll)
        val cr = Math.cos(rad)
        val sr = Math.sin(rad)
        val ch = cr * m[0] - sr * m[4]
        val sh = sr * m[5] - cr * m[1]
        return Math.toDegrees(Math.atan2(sh, ch))
    }

    /**
     * 返回此观察矩阵的倾斜角度（以度为单位）。
     */
    fun extractTilt(): Double {
        val ct = m[10]
        val st = Math.sqrt(m[2] * m[2] + m[6] * m[6])
        return Math.toDegrees(Math.atan2(st, ct))
    }
    /**
     * 转置此矩阵，将结果存储在指定的单精度数组中。
     * 结果与GLSL统一矩阵兼容，并且可以传递给函数glUniformMatrix4fv。
     */
    fun transposeToArray(result: FloatArray?, offset: Int): FloatArray {
        var offset_temp = offset
        if (result == null || result.size - offset_temp < 16) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "transposeToArray", "missingResult")
            )
        }
        result[offset_temp++] = m[0].toFloat()
        result[offset_temp++] = m[4].toFloat()
        result[offset_temp++] = m[8].toFloat()
        result[offset_temp++] = m[12].toFloat()
        result[offset_temp++] = m[1].toFloat()
        result[offset_temp++] = m[5].toFloat()
        result[offset_temp++] = m[9].toFloat()
        result[offset_temp++] = m[13].toFloat()
        result[offset_temp++] = m[2].toFloat()
        result[offset_temp++] = m[6].toFloat()
        result[offset_temp++] = m[10].toFloat()
        result[offset_temp++] = m[14].toFloat()
        result[offset_temp++] = m[3].toFloat()
        result[offset_temp++] = m[7].toFloat()
        result[offset_temp++] = m[11].toFloat()
        result[offset_temp] = m[15].toFloat()
        return result
    }

    fun project(
        x: Double,
        y: Double,
        z: Double,
        viewport: Viewport?,
        result: Vec3?
    ): Boolean {
        requireNotNull(viewport) { Logger.logMessage(Logger.ERROR, "Matrix4", "project", "missingViewport") }
        requireNotNull(result) { Logger.logMessage(Logger.ERROR, "Matrix4", "project", "missingResult") }
        // Transform the model point from model coordinates to eye coordinates then to clip coordinates. This inverts
        // the Z axis and stores the negative of the eye coordinate Z value in the W coordinate.
        val m = m
        var sx = m[0] * x + m[1] * y + m[2] * z + m[3]
        var sy = m[4] * x + m[5] * y + m[6] * z + m[7]
        var sz = m[8] * x + m[9] * y + m[10] * z + m[11]
        val sw = m[12] * x + m[13] * y + m[14] * z + m[15]
        if (sw == 0.0) {
            return false
        }
        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
        sx /= sw
        sy /= sw
        sz /= sw
        // Clip the point against the near and far clip planes.
        if (sz < -1 || sz > 1) {
            return false
        }
        // Convert the point from clip coordinate to the range [0,1]. This enables the X and Y coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range[0,1].
        sx = sx * 0.5 + 0.5
        sy = sy * 0.5 + 0.5
        sz = sz * 0.5 + 0.5
        // Convert the X and Y coordinates from the range [0,1] to screen coordinates.
        sx = sx * viewport.width + viewport.x
        sy = sy * viewport.height + viewport.y
        result.x = sx
        result.y = sy
        result.z = sz
        return true
    }

    fun unProject(
        x: Double,
        y: Double,
        viewport: Viewport,
        nearResult: Vec3,
        farResult: Vec3
    ): Boolean {
        var sx = (x - viewport.x) / viewport.width
        var sy = (y - viewport.y) / viewport.height
        sx = sx * 2 - 1
        sy = sy * 2 - 1
        val m = m
        val mx = m[0] * sx + m[1] * sy + m[3]
        val my = m[4] * sx + m[5] * sy + m[7]
        val mz = m[8] * sx + m[9] * sy + m[11]
        val mw = m[12] * sx + m[13] * sy + m[15]
        // Transform the screen point at the near clip plane (z = -1) to model coordinates.
        val nx = mx - m[2]
        val ny = my - m[6]
        val nz = mz - m[10]
        val nw = mw - m[14]
        // Transform the screen point at the far clip plane (z = +1) to model coordinates.
        val fx = mx + m[2]
        val fy = my + m[6]
        val fz = mz + m[10]
        val fw = mw + m[14]
        if (nw == 0.0 || fw == 0.0) {
            return false
        }
        // Complete the conversion from near clip coordinates to model coordinates by dividing by the W component.
        nearResult.x = nx / nw
        nearResult.y = ny / nw
        nearResult.z = nz / nw
        // Complete the conversion from far clip coordinates to model coordinates by dividing by the W component.
        farResult.x = fx / fw
        farResult.y = fy / fw
        farResult.z = fz / fw
        return true
    }

    /**
     * Sets this matrix to an infinite perspective projection matrix for the specified viewport dimensions, vertical
     * field of view and near clip distance.
     * 无限透视投影矩阵
     */
    fun setToInfiniteProjection(
        viewportWidth: Double,
        viewportHeight: Double,
        fovyDegrees: Double,
        nearDistance: Double
    ): Matrix4? {
        require(viewportWidth > 0) {
            Logger.logMessage(
                Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidWidth"
            )
        }
        require(viewportHeight > 0) {
            Logger.logMessage(
                Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidHeight"
            )
        }
        require(!(fovyDegrees <= 0 || fovyDegrees >= 180)) {
            Logger.logMessage(
                Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidFieldOfView"
            )
        }
        require(nearDistance > 0) {
            Logger.logMessage(
                Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidClipDistance"
            )
        }
        // Compute the dimensions of the near rectangle given the specified parameters.
        val aspect = viewportWidth / viewportHeight
        val tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5))
        val nearHeight = 2 * nearDistance * tanfovy_2
        val nearWidth = nearHeight * aspect
        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition, equation 4.52.
        m[0] = 2 * nearDistance / nearWidth
        m[1] = 0.0
        m[2] = 0.0
        m[3] = 0.0
        m[4] = 0.0
        m[5] = 2 * nearDistance / nearHeight
        m[6] = 0.0
        m[7] = 0.0
        m[8] = 0.0
        m[9] = 0.0
        m[10] = -1.0
        m[11] = -2 * nearDistance
        m[12] = 0.0
        m[13] = 0.0
        m[14] = -1.0
        m[15] = 0.0
        return this
    }

    override fun toString(): String {
        return " \n" + "[" + m[0] + ",      " + m[1] + ",       " + m[2] + ",       " + m[3] + "], \n" +
                '[' + m[4] + ",      " + m[5] + ",      " + m[6] + ",       " + m[7] + "], \n" +
                '[' + m[8] + ",         " + m[9] + ",       " + m[10] + ",      " + m[11] + "], \n" +
                '[' + m[12] + ",        " + m[13] + ",      " + m[14] + ",      " + m[15] + ']'
    }
}