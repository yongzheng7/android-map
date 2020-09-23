package com.atom.wyz.worldwind.test.utils

import android.opengl.Matrix

enum class MatrixUtils {
    ;

    companion object {
        public val TYPE_FITXY = 0
        public val TYPE_CENTERCROP = 1
        public val TYPE_CENTERINSIDE = 2
        public val TYPE_FITSTART = 3
        public val TYPE_FITEND = 4


        /**
         * 获取一个新的原始纹理坐标，每次调用，都会重新创建
         * @return 坐标数组
         * -1,1         0,1[2]        1,1[4]
         *
         *
         * -------------0,0[1]--------1,0[3]
         *
         *
         * -1,-1        0,-1          1,-1
         */
        open fun getOriginalTextureCo(): FloatArray {
            return floatArrayOf(
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f
            )
        }

        /**
         * 获取一个新的原始顶点坐标，每次调用，都会重新创建
         * @return 坐标数组
         * -1,1[2]      0,1---        1,1[4]
         *
         *
         * -------------0,0-----------1,0
         *
         *
         * -1,-1[1]     0,-1          1,-1[3]
         */
        open fun getOriginalVertexCo(): FloatArray {
            return floatArrayOf(
                    -1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, -1.0f,
                    1.0f, 1.0f
            )
        }

        /**
         * 获取一个新的4*4单位矩阵
         * @return 矩阵数组
         * | 1 0 0 0 |
         * | 0 1 0 0 |
         * | 0 0 1 0 |
         * | 0 0 0 1 |
         */
        open fun getOriginalMatrix(): FloatArray {
            return floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        }

        /**
         * 根据预览的大小和图像的大小，计算合适的变换矩阵
         * @param matrix  接收变换矩阵的数组
         * @param type 变换的类型，参考
         * [.TYPE_CENTERCROP]、
         * [.TYPE_FITEND]、
         * [.TYPE_CENTERINSIDE]、
         * [.TYPE_FITSTART]、
         * [.TYPE_FITXY]，
         * 对应[android.widget.ImageView]的[android.widget.ImageView.setScaleType]
         * @param imgWidth 图像的宽度
         * @param imgHeight 图像的高度
         * @param viewWidth 视图的宽度
         * @param viewHeight 视图的高度
         */
        open fun getMatrix(matrix: FloatArray, type: Int,
                           imgWidth: Int, imgHeight: Int,
                           viewWidth: Int, viewHeight: Int) {

            if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
                val projection = FloatArray(16) // 透视矩阵
                val camera = FloatArray(16) //观察矩阵
                if (type == TYPE_FITXY) { //
                    Matrix.orthoM(projection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
                    Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                    Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
                    return
                }
                val sWhView = viewWidth.toFloat() / viewHeight
                val sWhImg = imgWidth.toFloat() / imgHeight
                if (sWhImg > sWhView) { // 如果
                    when (type) {
                        TYPE_CENTERCROP -> Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1f, 1f, 1f, 3f)
                        TYPE_CENTERINSIDE -> Matrix.orthoM(projection, 0, -1f, 1f, -sWhImg / sWhView, sWhImg / sWhView, 1f, 3f)
                        TYPE_FITSTART -> Matrix.orthoM(projection, 0, -1f, 1f, 1 - 2 * sWhImg / sWhView, 1f, 1f, 3f)
                        TYPE_FITEND -> Matrix.orthoM(projection, 0, -1f, 1f, -1f, 2 * sWhImg / sWhView - 1, 1f, 3f)
                        else -> {
                        }
                    }
                } else {
                    when (type) {
                        TYPE_CENTERCROP -> Matrix.orthoM(projection, 0, -1f, 1f, -sWhImg / sWhView, sWhImg / sWhView, 1f, 3f)
                        TYPE_CENTERINSIDE -> Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1f, 1f, 1f, 3f)
                        TYPE_FITSTART -> Matrix.orthoM(projection, 0, -1f, 2 * sWhView / sWhImg - 1, -1f, 1f, 1f, 3f)
                        TYPE_FITEND -> Matrix.orthoM(projection, 0, 1 - 2 * sWhView / sWhImg, 1f, -1f, 1f, 1f, 3f)
                        else -> {
                        }
                    }
                }
                Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
            }
        }

        /**
         * 翻转矩阵
         * @param m 需要被翻转的矩阵
         * @param x 是否x轴左右翻转
         * @param y 是否y轴左右翻转
         * @return 传入的矩阵
         */
        open fun flip(m: FloatArray, x: Boolean, y: Boolean): FloatArray {
            if (x || y) {
                Matrix.scaleM(m, 0, if (x) -1.0f else 1.0f, if (y) -1.0f else 1.0f, 1f)
            }
            return m
        }
    }
}