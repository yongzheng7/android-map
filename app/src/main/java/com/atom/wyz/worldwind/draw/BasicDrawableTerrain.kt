package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Range
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.pool.Pool

class BasicDrawableTerrain : DrawableTerrain {

    companion object {
        fun obtain(pool: Pool<BasicDrawableTerrain>): BasicDrawableTerrain {
            val instance = pool.acquire() // get an instance from the pool
            return instance?.setPool(pool) ?: BasicDrawableTerrain().setPool(pool)
        }
    }

    override var sector = Sector()

    override var vertexOrigin = Vec3()

    var lineElementRange: Range = Range()

    var triStripElementRange: Range = Range()

    var vertexPoints: BufferObject? = null

    var vertexTexCoords: BufferObject? = null

    var elements: BufferObject? = null

    private var pool: Pool<BasicDrawableTerrain>? = null
    private fun setPool(pool: Pool<BasicDrawableTerrain>): BasicDrawableTerrain {
        this.pool = pool
        return this
    }


    override fun useVertexPointAttrib(dc: DrawContext, attribLocation: Int)  : Boolean{
        var bufferBound :Boolean
        bufferBound = vertexPoints?.bindBuffer(dc)?.also { bufferBound = it } ?: false
        if(bufferBound){
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, 0)
        }
        return bufferBound
    }

    override fun useVertexTexCoordAttrib(dc: DrawContext, attribLocation: Int)  : Boolean{
        var bufferBound :Boolean
        bufferBound = vertexTexCoords?.bindBuffer(dc)?.also { bufferBound = it } ?: false
        if(bufferBound){
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, 0)
        }
        return bufferBound
    }

    override fun drawLines(dc: DrawContext) : Boolean {
        var bufferBound :Boolean
        bufferBound = elements?.bindBuffer(dc)?.also { bufferBound = it } ?: false
        if(bufferBound){
            GLES20.glDrawElements(GLES20.GL_LINES, this.lineElementRange.length() , GLES20.GL_UNSIGNED_SHORT, 0)
        }
        return bufferBound
    }

    override fun drawTriangles(dc: DrawContext) : Boolean {
        var bufferBound :Boolean
        bufferBound = elements?.bindBuffer(dc)?.also { bufferBound = it } ?: false

        if(bufferBound){
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStripElementRange.length() , GLES20.GL_UNSIGNED_SHORT, this.triStripElementRange.lower * 2)
        }
        return bufferBound
    }

    override fun draw(dc: DrawContext) {
        drawTriangles(dc)
    }

    override fun recycle() {
        vertexPoints = null
        vertexTexCoords = null
        elements = null
        pool?.release(this)
        pool = null
    }
}