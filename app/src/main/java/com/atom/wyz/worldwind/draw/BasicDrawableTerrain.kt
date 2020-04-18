package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class BasicDrawableTerrain : DrawableTerrain {

    companion object {
        fun obtain(pool: Pool<BasicDrawableTerrain>): BasicDrawableTerrain {
            val instance = pool.acquire() // get an instance from the pool
            return instance?.setPool(pool) ?: BasicDrawableTerrain().setPool(pool)
        }
    }

    override var sector = Sector()

    override var vertexOrigin = Vec3()

    var vertexPoints: FloatBuffer? = null

    var vertexTexCoords: FloatBuffer? = null

    var lineElements: ShortBuffer? = null

    var triStripElements: ShortBuffer? = null

    private var pool: Pool<BasicDrawableTerrain>? = null
    private fun setPool(pool: Pool<BasicDrawableTerrain>): BasicDrawableTerrain {
        this.pool = pool
        return this
    }


    override fun useVertexPointAttrib(dc: DrawContext, attribLocation: Int) {
        if (vertexPoints != null) {
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, vertexPoints)
        }
    }

    override fun useVertexTexCoordAttrib(dc: DrawContext, attribLocation: Int) {
        if (vertexTexCoords != null) {
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, vertexTexCoords)
        }
    }

    override fun drawLines(dc: DrawContext) {
        if (lineElements != null) {
            GLES20.glDrawElements(
                GLES20.GL_LINES,
                lineElements!!.remaining(),
                GLES20.GL_UNSIGNED_SHORT,
                lineElements
            )
        }
    }

    override fun drawTriangles(dc: DrawContext) {
        if (triStripElements != null) {
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLE_STRIP,
                triStripElements!!.remaining(),
                GLES20.GL_UNSIGNED_SHORT,
                triStripElements
            )
        }
    }

    override fun draw(dc: DrawContext) {
        drawTriangles(dc)
    }

    override fun recycle() {
        vertexPoints = null
        vertexTexCoords = null
        lineElements = null
        triStripElements = null
        pool?.release(this)
        pool = null
    }
}