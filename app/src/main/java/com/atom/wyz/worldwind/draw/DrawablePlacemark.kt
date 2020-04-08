package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.render.GpuTexture
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class DrawablePlacemark : Drawable {
    companion object {
        private var unitQuadBuffer: FloatBuffer? = null
        private var leaderBuffer: FloatBuffer? = null
        private var leaderPoints: FloatArray? = null

        fun getUnitQuadBuffer(): FloatBuffer? {
            if (unitQuadBuffer == null) {
                val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f) //右下 右上 左下 左上
                val size = points.size * 4
                unitQuadBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                unitQuadBuffer?.put(points)?.rewind()
            }
            return unitQuadBuffer
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
    var leaderColor: Color? = null
    var enableLeaderDepthTest = true
    var leaderWidth = 1f
    var enableLeaderPicking = false

    var screenPlacePoint: Vec3 = Vec3(0.0, 0.0, 0.0)
    var screenGroundPoint: Vec3? = null

    var rotation = 0.0
    var tilt = 0.0

    var iconTexture: GpuTexture? = null // 图像纹理

    var program: BasicProgram? = null

    var texCoordMatrix: Matrix3 = Matrix3()

    var mvpMatrix: Matrix4 = Matrix4()

    var enableDepthTest = true

    override fun draw(dc: DrawContext) {

        if(this.program == null) {
            return
        }
        val program = this.program as BasicProgram

        if (!program.useProgram(dc)) {
            return
        }

        enableDepthTest = true

        if (drawLeader) {
            drawLeader(dc)
        }
        drawIcon(dc)

        if (!enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//            GLES20.glDepthMask(true )
        }


    }
    protected fun drawIcon(dc: DrawContext) { // Set up a 2D unit quad as the source of vertex points and texture coordinates.
        mvpMatrix.set(dc.screenProjection)
        mvpMatrix.multiplyByMatrix(imageTransform)
        mvpMatrix.multiplyByTranslation(0.5, 0.5, 0.0)
        mvpMatrix.multiplyByRotation(0.0, 0.0, 1.0, rotation)
        mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0.0)
        mvpMatrix.multiplyByRotation((-1).toDouble(), 0.0, 0.0, tilt)
        program!!.loadModelviewProjection(mvpMatrix)
        iconTexture?.let {
            dc.activeTextureUnit(GLES20.GL_TEXTURE0)
            if (it.bindTexture(dc)) {
                program!!.enableTexture(true)
                texCoordMatrix.setToIdentity()
                it.applyTexCoordTransform(texCoordMatrix)
                program!!.loadTexCoordMatrix(texCoordMatrix)
            }
        } ?:let{
            program!!.enableTexture(false)
        }
        program!!.loadColor(imageColor)
        enableDepthTest(enableImageDepthTest)
        val unitQuadBuffer: Buffer? = getUnitQuadBuffer()
        GLES20.glEnableVertexAttribArray(1) // enable vertex attrib 1; vertex attrib 0 is enabled by default
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, unitQuadBuffer)
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, unitQuadBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(1)
    }
    protected fun drawLeader(dc: DrawContext) {
        program!!.enableTexture(false)
        program!!.loadModelviewProjection(dc.screenProjection)
        program!!.loadColor(leaderColor!!)
        this.enableDepthTest(enableLeaderDepthTest)
        GLES20.glLineWidth(leaderWidth)
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(screenGroundPoint!!, screenPlacePoint))
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
    }
    protected fun enableDepthTest(enable: Boolean) {
        if (enableDepthTest != enable) {
            enableDepthTest = enable
            if (enable) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//                GLES20.glDepthMask(true )
            } else {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST)
//                GLES20.glDepthMask(false )
            }
        }
    }

    override fun recycle() {
        program = null
        iconTexture = null
    }
}
