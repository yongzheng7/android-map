package com.atom.wyz.worldwind.util

import android.graphics.Rect
import com.atom.wyz.worldwind.geom.Matrix4

class WWMath {
    companion object {
        /**
         * 将值限制在[min，max]度范围内
         */
        fun clamp(value: Double, min: Double, max: Double): Double {
            return if (value > max) max else if (value < min) min else value
        }

        /**
         * 规格话角度 判断在 0 ---- 360 最小为0 最大为360
         */
        fun normalizeAngle360(degrees: Double): Double {
            val angle = degrees % 360
            return if (angle >= 0) angle else if (angle < 0) 360 + angle else 360 - angle
        }

        /**
         * 将角度限制在[-180，+180]度范围内，包角不在该范围内。经度
         */
        fun normalizeAngle180(degrees: Double): Double {
            val angle = degrees % 360
            return if (angle > 180) angle - 360 else if (angle < -180) 360 + angle else angle
        }

        /**
         * 将角度限制在 -180 --- 180 之间  最大是 180  最小-180
         */
        fun clampAngle180(degrees: Double): Double {
            return if (degrees > 180) 180.0 else if (degrees < -180) -180.0 else degrees
        }

        /**
         * 将角度限制在 0 -- 360 最大是360 最小是0
         */
        fun clampAngle360(degrees: Double): Double {
            return if (degrees > 360) 360.0 else if (degrees < 0) 0.0 else degrees
        }

        /**
         * 是否是偶数
         */
        fun isPowerOfTwo(value: Int): Boolean {
            return value != 0 && value and value - 1 == 0
        }

        /**
         * 计算线性差值 (1 - 比例) * 数字1 + 比例 * 数字2
         */
        fun interpolate(amount: Double, value1: Double, value2: Double): Double {
            return (1 - amount) * value1 + amount * value2
        }

        /**
         * 计算线性角度 限制在 180 --  -180 中
         */
        fun interpolateAngle180(amount: Double, degrees1: Double, degrees2: Double): Double { // Normalize the two angles to the range [-180, +180].
            var angle1: Double = normalizeAngle180(degrees1)
            var angle2: Double = normalizeAngle180(degrees2)

            if (angle1 - angle2 > 180) {
                angle2 += 360.0
            } else if (angle1 - angle2 < -180) {
                angle1 += 360.0
            }

            val angle = (1 - amount) * angle1 + amount * angle2
            return normalizeAngle180(angle)
        }

        /**
         * 线性角度 限制在 0 --- 360 中
         */
        fun interpolateAngle360(amount: Double, degrees1: Double, degrees2: Double): Double { // Normalize the two angles to the range [-180, +180].
            var angle1: Double = normalizeAngle180(degrees1)
            var angle2: Double = normalizeAngle180(degrees2)
            if (angle1 - angle2 > 180) {
                angle2 += 360.0
            } else if (angle1 - angle2 < -180) {
                angle1 += 360.0
            }
            val angle = (1 - amount) * angle1 + amount * angle2
            return normalizeAngle360(angle)
        }

        /**
         * 将转换矩阵应用于正方形的四个角后，计算该正方形的边界矩形。
         */
        fun boundingRectForUnitSquare(unitSquareTransform: Matrix4?): Rect {
            if (unitSquareTransform == null) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "WWMath", "boundingRectForUnitSquare", "missingMatrix"))
            }
            val result = Rect();
            val m: DoubleArray = unitSquareTransform.m
            // transform of (0, 0)
            // 0  1  2  3
            // 4  5  6  7
            // 8  9  10 11
            // 12 13 14 15
            val x1 = m[3]
            val y1 = m[7]
            // transform of (1, 0)
            val x2 = m[0] + m[3]
            val y2 = m[4] + m[7]
            // transform of (0, 1)
            val x3 = m[1] + m[3]
            val y3 = m[5] + m[7]
            // transform of (1, 1)
            val x4 = m[0] + m[1] + m[3]
            val y4 = m[4] + m[5] + m[7]
            val minX = Math.min(Math.min(x1, x2), Math.min(x3, x4)).toInt()
            val maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4)).toInt()
            val minY = Math.min(Math.min(y1, y2), Math.min(y3, y4)).toInt()
            val maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4)).toInt()
            result.set(minX, minY, maxX - minX, maxY - minY)
            return result
        }

    }
}