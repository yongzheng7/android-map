package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.shader.GpuTexture
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableScreenTexture : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableScreenTexture>): DrawableScreenTexture =
            pool.acquire()?.setPool(pool) ?: DrawableScreenTexture().setPool(pool)
    }

    var program: BasicProgram? = null

    var mvpMatrix: Matrix4 = Matrix4()

    var color: SimpleColor =
        SimpleColor()

    var texture: GpuTexture? = null

    var enableDepthTest = true

    var unitSquareTransform = Matrix4()

    var pool: Pool<DrawableScreenTexture>? = null

    private fun setPool(pool: Pool<DrawableScreenTexture>): DrawableScreenTexture {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return
        if (!program.useProgram(dc)) return

        if (!dc.unitSquareBuffer().bindBuffer(dc)) return

        program.enablePickMode(dc.pickMode)

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        GLES20.glDepthMask(false)

        GLES20.glEnableVertexAttribArray(1)
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, 2, GLES20.GL_FLOAT, false, 0, 0)

        // Draw this DrawableScreenTextures.
        this.doDraw(dc, this)

        var next: Drawable?
        while (dc.peekDrawable().also {
                next = it
            } != null && canBatchWith(next!!)) {
            val drawable = dc.pollDrawable() as DrawableScreenTexture // take it off the queue
            this.doDraw(dc, drawable)
        }

        GLES20.glDepthMask(true)
        GLES20.glDisableVertexAttribArray(1)
    }

    protected fun doDraw(dc: DrawContext, drawable: DrawableScreenTexture) {
        // Use the drawable's color.
        drawable.program!!.loadColor(drawable.color)

        // Attempt to bind the drawable's texture, configuring the shader program appropriately if there is no texture
        // or if the texture failed to bind.
        if (drawable.texture != null && drawable.texture!!.bindTexture(dc)) {
            drawable.program!!.enableTexture(true)
            drawable.program!!.loadTexCoordMatrix(drawable.texture!!.texCoordTransform)
        } else {
            drawable.program!!.enableTexture(false)
        }
        // Use a modelview-projection matrix that transforms the unit square to screen coordinates.
        drawable.mvpMatrix.setToMultiply(dc.screenProjection, drawable.unitSquareTransform)
        drawable.program!!.loadModelviewProjection(drawable.mvpMatrix)

        // Disable depth testing if requested.
        if (!drawable.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        // Draw the unit square as triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Restore the default World Wind OpenGL state.
        if (!drawable.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
    }

    override fun recycle() {
        program = null
        texture = null
        pool?.release(this)
        pool = null
    }

    protected fun canBatchWith(that: Drawable): Boolean {
        return this.javaClass == that.javaClass && this.program === (that as DrawableScreenTexture).program
    }
}