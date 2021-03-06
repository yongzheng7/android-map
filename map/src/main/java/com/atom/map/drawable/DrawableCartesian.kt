package com.atom.map.drawable

import android.opengl.GLES20
import com.atom.map.geom.SimpleColor
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.shader.CartesianProgram
import com.atom.map.util.pool.Pool

class DrawableCartesian : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableCartesian>): DrawableCartesian =
            pool.acquire()?.setPool(pool) ?: DrawableCartesian()
                .setPool(pool)
    }

    var pool: Pool<DrawableCartesian>? = null

    var program: CartesianProgram? = null

    var color = SimpleColor()

    var vertexPoints: BufferObject? = null

    var triStripElements: BufferObject? = null

    var isLine = true ;

    private fun setPool(pool: Pool<DrawableCartesian>): DrawableCartesian {
        this.pool = pool
        return this
    }

    operator fun set(program: CartesianProgram, color: SimpleColor?, line : Boolean ): DrawableCartesian {
        this.program = program
        if (color != null) {
            this.color.set(color)
        } else {
            this.color.set(1f, 1f, 1f, 1f)
        }
        isLine = line
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
        program.loadModelviewProjection(dc.modelviewProjection)
        GLES20.glLineWidth(5f)
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 4, GLES20.GL_FLOAT, false, 0, 0)
        //GLES20.glDepthMask(false)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        if(isLine){
            GLES20.glDrawElements(GLES20.GL_LINE_STRIP, triStripElements!!.bufferLength, GLES20.GL_UNSIGNED_SHORT, 0)
        }else{
            GLES20.glDisable(GLES20.GL_CULL_FACE)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, triStripElements!!.bufferLength, GLES20.GL_UNSIGNED_SHORT, 0)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }
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
