package com.atom.map.layer.draw

import android.opengl.GLES20
import com.atom.map.geom.SimpleColor
import com.atom.map.core.shader.BasicProgram
import com.atom.map.core.shader.BufferObject
import com.atom.map.util.pool.Pool

class DrawableFrustum : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableFrustum>): DrawableFrustum =
            pool.acquire()?.setPool(pool) ?: DrawableFrustum()
                .setPool(pool)
    }

    var pool: Pool<DrawableFrustum>? = null

    var program: BasicProgram? = null

    var color = SimpleColor()

    var vertexPoints: BufferObject? = null

    var triStripElements: BufferObject? = null

    private fun setPool(pool: Pool<DrawableFrustum>): DrawableFrustum {
        this.pool = pool
        return this
    }

    operator fun set(program: BasicProgram, color: SimpleColor?): DrawableFrustum {
        this.program = program
        if (color != null) {
            this.color.set(color)
        } else {
            this.color.set(1f, 1f, 1f, 1f)
        }
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return
        }
        if (vertexPoints == null || !vertexPoints!!.bindBuffer(dc)) {
            return
        }
        if (triStripElements == null || !triStripElements!!.bindBuffer(dc)) {
            return
        }
        program.enableTexture(false)
        program.loadColor(color)
        GLES20.glDepthMask(false)
        program.loadModelviewProjection(dc.modelviewProjection)

        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 4, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glDepthMask(false)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP,
            triStripElements!!.bufferLength,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        GLES20.glDepthMask(true)
    }

    override fun recycle() {
        program = null
        pool?.release(this) // return this instance to the pool
        pool = null
    }
}
