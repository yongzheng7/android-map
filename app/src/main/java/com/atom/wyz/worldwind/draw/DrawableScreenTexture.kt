package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableScreenTexture : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableScreenTexture>): DrawableScreenTexture {
            return pool.acquire()?.setPool(pool) ?: DrawableScreenTexture().setPool(pool) // get an instance from the pool
        }
    }

    var program: BasicProgram? = null

    var mvpMatrix: Matrix4 = Matrix4()

    var color: Color = Color()

    var texture: GpuTexture? = null

    var enableDepthTest = true

    var unitSquareTransform = Matrix4()

    private var pool: Pool<DrawableScreenTexture>? = null

    private fun setPool(pool: Pool<DrawableScreenTexture>): DrawableScreenTexture {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return
        if (!program.useProgram(dc)) return

        if (!dc.unitSquareBuffer().bindBuffer(dc)) return

        program.loadColor(color)

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        if (texture != null && texture!!.bindTexture(dc)) {
            program.enableTexture(true)
            program.loadTexCoordMatrix(texture!!.texCoordTransform)
        } else {
            program.enableTexture(false)
        }

        GLES20.glDepthMask(false)
        if (!enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }

        GLES20.glEnableVertexAttribArray(1)

        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, 2, GLES20.GL_FLOAT, false, 0, 0)
        // Use a modelview-projection matrix that transforms the unit square to screen coordinates.

        mvpMatrix.setToMultiply(dc.screenProjection, unitSquareTransform)
        program.loadModelviewProjection(mvpMatrix)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        var next: Drawable?
        while (dc.peekDrawable().also { next = it } != null && next?.let { canBatchWith(it) } ?: false) {
            val drawable = dc.pollDrawable() as DrawableScreenTexture? ?:continue
            mvpMatrix.setToMultiply(dc.screenProjection, drawable.unitSquareTransform)
            program.loadModelviewProjection(mvpMatrix)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        GLES20.glDepthMask(true)

        if (!enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        GLES20.glDisableVertexAttribArray(1)
    }

    override fun recycle() {
        program = null
        texture = null
        pool?.release(this)
        pool = null
    }

    protected fun canBatchWith(that: Drawable): Boolean {
        return (this.javaClass == that::javaClass && program === (that as DrawableScreenTexture).program && color.equals((that as DrawableScreenTexture).color)
                && texture === that.texture && enableDepthTest == that.enableDepthTest)
    }
}