package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.render.CartesianProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableCartesian : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableCartesian>): DrawableCartesian {
            return pool.acquire()?.setPool(pool) ?: DrawableCartesian().setPool(pool)
        }
    }
    protected var pool: Pool<DrawableCartesian>? = null

    protected var program: CartesianProgram? = null

    protected var color = Color()

    var vertexPoints: BufferObject? = null

    var triStripElements: BufferObject? = null

    private fun setPool(pool: Pool<DrawableCartesian>): DrawableCartesian {
        this.pool = pool
        return this
    }
    operator fun set(program: CartesianProgram, color: Color?): DrawableCartesian {
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
        if(vertexPoints == null || !vertexPoints!!.bindBuffer(dc)){
            return
        }
        if(triStripElements == null || !triStripElements!!.bindBuffer(dc)){
            return
        }
        GLES20.glLineWidth(5f)
        program.loadModelviewProjection(dc.modelviewProjection)
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 4, GLES20.GL_FLOAT, false, 0, 0)
        //GLES20.glDepthMask(false)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDrawElements(GLES20.GL_LINE_STRIP, triStripElements!!.bufferLength, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        //GLES20.glDepthMask(false)
        GLES20.glLineWidth(1f)
    }

    override fun recycle() {
        program = null
        pool?.release(this) // return this instance to the pool
        pool = null
    }
}
