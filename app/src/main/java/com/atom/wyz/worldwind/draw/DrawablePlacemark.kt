package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.render.GpuTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class DrawablePlacemark : Drawable {
    companion object {
        private var unitQuadBuffer2: FloatBuffer? = null
        private var unitQuadBuffer3: FloatBuffer? = null
        private var leaderBuffer: FloatBuffer? = null
        private var leaderPoints: FloatArray? = null

        fun getUnitQuadBuffer2D(): FloatBuffer? {
            if (unitQuadBuffer2 == null) {
                val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f) //右下 右上 左下 左上
                val size = points.size * 4
                unitQuadBuffer2 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                unitQuadBuffer2?.put(points)?.rewind()
            }
            return unitQuadBuffer2
        }

        fun getUnitQuadBuffer3D(): FloatBuffer? {
            if (unitQuadBuffer3 == null) {
                val points = floatArrayOf(0f, 1f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 1f, 0f, 0f) // lower right corner
                val size = points.size * 4
                unitQuadBuffer3 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                unitQuadBuffer3?.put(points)?.rewind()
            }
            return unitQuadBuffer3
        }

        fun getLeaderBuffer(groundPoint: Vec3, placePoint: Vec3): FloatBuffer? {
            if (leaderBuffer == null) {
                leaderPoints = FloatArray(6)
                val size = leaderPoints!!.size * 4
                leaderBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            }
            // TODO: consider whether these assignments should be inlined.
            leaderPoints!![0] = groundPoint.x.toFloat()
            leaderPoints!![1] = groundPoint.y.toFloat()
            leaderPoints!![2] = groundPoint.z.toFloat()
            leaderPoints!![3] = placePoint.x.toFloat()
            leaderPoints!![4] = placePoint.y.toFloat()
            leaderPoints!![5] = placePoint.z.toFloat()
            leaderBuffer!!.put(leaderPoints).rewind()
            return leaderBuffer
        }
    }

    var imageColor: Color = Color(Color.WHITE)
    var enableImageDepthTest = true
    var imageTransform: Matrix4 = Matrix4()


    var drawLeader = false
    /**
     * 绘制引导线 颜色
     * @drawLeader if true this will create
     */
    var leaderColor: Color? = null
    var enableLeaderDepthTest = true
    var leaderWidth = 1f
    var enableLeaderPicking = false


    var drawLabel = false
    /**
     * 绘制引导线 颜色
     * @drawLabel if true this will create
     */
    var labelColor: Color? = null // 绘制字体label颜色
    var label: String? = null
    var enableLabelDepthTest = true
    var labelTransform: Matrix4? = null

    var screenPlacePoint: Vec3 = Vec3(0.0, 0.0, 0.0)
    var screenGroundPoint: Vec3? = null

    var actualRotation = 0.0
    var actualTilt = 0.0

    var activeTexture: GpuTexture? = null // 图像纹理
    var labelTexture: GpuTexture? = null // 便签纹理

    /**
     * 纹理的变换矩阵
     */
    var texCoordMatrix: Matrix3 = Matrix3()

    /**
     * 顶点的变换矩阵
     */
    var mvpMatrix: Matrix4 = Matrix4()

    override fun draw(dc: DrawContext) {

        val program = dc.getProgram(BasicProgram.KEY)?.let { it as BasicProgram } ?: let {
            dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
        }
        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        var depthTesting = true // default
        var textureBound = false

        ///////////////////////////////////
        // Draw the optional leader-line
        ///////////////////////////////////
        // Draw the leader line first so that the image and label have visual priority.
        if (drawLeader) {
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(screenGroundPoint!!, screenPlacePoint))

            program.loadModelviewProjection(mvpMatrix.set(dc.screenProjection))

            program.loadColor( /*dc.pickingMode ? this.pickColor : */leaderColor!!) // TODO: pickColor

            if (enableLeaderDepthTest != depthTesting) {
                depthTesting = !depthTesting
                if (depthTesting) {
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                } else {
                    GLES20.glDisable(GLES20.GL_DEPTH_TEST)
                }
            }

            GLES20.glLineWidth(leaderWidth)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }

        ///////////////////////////////////
        // Draw the image
        ///////////////////////////////////
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getUnitQuadBuffer3D())

        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, getUnitQuadBuffer2D())

        // Compute and specify the MVP matrix...
        mvpMatrix.set(dc.screenProjection)
        mvpMatrix.multiplyByMatrix(imageTransform)

        // ... perform image rotation
        mvpMatrix.multiplyByTranslation(0.5, 0.5, 0.0)
        mvpMatrix.multiplyByRotation(0.0, 0.0, 1.0, actualRotation)
        mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0.0)

        // ... and perform the tilt so that the image tilts back from its base into the view volume.
        mvpMatrix.multiplyByRotation(-1.0, 0.0, 0.0, actualTilt)

        program.loadModelviewProjection(mvpMatrix)




        activeTexture?.let {
            dc.activeTextureUnit(GLES20.GL_TEXTURE0)
            textureBound = it.bindTexture(dc)
            if (textureBound) {
                program.enableTexture(true)
                activeTexture?.applyTexCoordTransform(texCoordMatrix.setToIdentity())
                program.loadTexCoordMatrix(texCoordMatrix)
            }
        }

        program.loadColor(imageColor)

        // Turn off depth testing for the placemark image if requested. The placemark label and leader line have
        // their own depth-test controls.
        if (enableImageDepthTest != depthTesting) {
            depthTesting = !depthTesting
            if (depthTesting) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            } else {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            }
        }

        // Draw the placemark's image quad.
        GLES20.glEnableVertexAttribArray(1) // vertexTexCoord
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        ///////////////////////////////////
        // Draw the label
        ///////////////////////////////////
        if (drawLabel) { // TODO: drawLabel
//            program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity * this.currentVisibility);
//
//            Placemark.matrix.copy(dc.screenProjection);
//            Placemark.matrix.multiplyMatrix(this.labelTransform);
//            program.loadModelviewProjection(gl, Placemark.matrix);
//
//            if (!dc.pickingMode && this.labelTexture) {
//                this.texCoordMatrix.setToIdentity();
//                this.texCoordMatrix.multiplyByTextureTransform(this.labelTexture);
//
//                program.loadTextureMatrix(gl, this.texCoordMatrix);
//                program.loadColor(gl, this.attributes.labelAttributes.color);
//
//                textureBound = this.labelTexture.bind(dc);
//                program.loadTextureEnabled(gl, textureBound);
//            } else {
//                program.loadTextureEnabled(gl, false);
//                program.loadColor(gl, this.pickColor);
//            }
//
//            if (this.attributes.labelAttributes.depthTest && depthTest) {
//                    depthTest = true;
//                    gl.enable(gl.DEPTH_TEST);
//            } else {
//                depthTest = false;
//                gl.disable(gl.DEPTH_TEST);
//            }
//
//            gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        }

        // Restore the default World Wind OpenGL state.
        // Restore the default World Wind OpenGL state.
        if (!depthTesting) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        if (textureBound) {
            program.enableTexture(false)
        }
        GLES20.glDisableVertexAttribArray(1)

    }
}
