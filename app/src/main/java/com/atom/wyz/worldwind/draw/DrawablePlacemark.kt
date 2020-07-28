package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.shader.GpuTexture
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class DrawablePlacemark : Drawable {
    companion object {
        private var leaderBuffer: FloatBuffer? = null

        fun getLeaderBuffer(points: FloatArray): FloatBuffer? {
            val size: Int = points.size
            if (leaderBuffer == null || leaderBuffer!!.capacity() < size) {
                leaderBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            }
            leaderBuffer!!.clear()
            leaderBuffer!!.put(points).flip()
            return leaderBuffer
        }

        fun obtain(pool: Pool<DrawablePlacemark>): DrawablePlacemark =
            pool.acquire()?.setPool(pool) ?: DrawablePlacemark().setPool(pool)
    }

    var iconColor: Color = Color(Color.WHITE)
    var enableIconDepthTest = true
    var iconTexture: GpuTexture? = null
    var iconMvpMatrix: Matrix4 = Matrix4()
    var iconTexCoordMatrix: Matrix3 = Matrix3()

    var drawLeader = false
    var leaderColor = Color()
    var enableLeaderDepthTest = true
    var leaderWidth = 1f
    var enableLeaderPicking = false
    var leaderMvpMatrix: Matrix4? = Matrix4()
    var leaderVertexPoint: FloatArray? = FloatArray(6)

    var program: BasicProgram? = null

    var enableDepthTest = true

    private var pool: Pool<DrawablePlacemark>? = null

    override fun draw(dc: DrawContext) {

        if (this.program == null) {
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
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        iconTexture?.let {
            if (it.bindTexture(dc)) {
                program!!.enableTexture(true)
                program!!.loadTexCoordMatrix(this.iconTexCoordMatrix)
            }
        } ?: let {
            program!!.enableTexture(false)
        }
        program!!.loadColor(iconColor)

        program!!.loadModelviewProjection(iconMvpMatrix)

        enableDepthTest(enableIconDepthTest)

        GLES20.glDepthMask(false)

        dc.unitSquareBuffer().bindBuffer(dc)

        GLES20.glEnableVertexAttribArray(1)
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glDepthMask(true)

        GLES20.glDisableVertexAttribArray(1)
    }

    protected fun drawLeader(dc: DrawContext) {
        program!!.enableTexture(false)

        program!!.loadColor(leaderColor)

        this.leaderMvpMatrix?.let {
            program!!.loadModelviewProjection(it)
        }

        this.enableDepthTest(enableLeaderDepthTest)

        GLES20.glLineWidth(leaderWidth)

        leaderVertexPoint?.let {
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(it))
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }

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

    private fun setPool(pool: Pool<DrawablePlacemark>): DrawablePlacemark {
        this.pool = pool
        return this
    }

    override fun recycle() {
        program = null
        iconTexture = null
        pool?.release(this)
        pool = null
    }
}
