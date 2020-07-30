package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableLines : Drawable {

    companion object {

        fun obtain(pool: Pool<DrawableLines>): DrawableLines =
            pool.acquire()?.setPool(pool) ?: DrawableLines().setPool(pool)
    }

    var program: BasicProgram? = null

    var vertexPoints = FloatArray(6)

    var mvpMatrix: Matrix4 = Matrix4()

    var color: SimpleColor =
        SimpleColor()

    var lineWidth = 1f

    var enableDepthTest = true

    var pool: Pool<DrawableLines>? = null

    private fun setPool(pool: Pool<DrawableLines>): DrawableLines {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return
        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        program.enableTexture(false)
        program.loadColor(color)
        program.loadModelviewProjection(this.mvpMatrix)
        // Disable depth testing if requested.
        if (!enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        GLES20.glLineWidth(lineWidth)
        // Use the leader line as the vertex point attribute.
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        val buffer = dc.scratchBuffer(vertexPoints.size * 4).asFloatBuffer()
        buffer.clear()
        buffer.put(vertexPoints).flip()
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, buffer)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0 /*first*/, buffer.remaining() / 3 /*count*/)
        // Restore the default World Wind OpenGL state.
        if (!enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        GLES20.glLineWidth(1f)
    }

    override fun recycle() {
        program = null
        pool?.release(this)
        pool = null
    }
}