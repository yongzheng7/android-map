package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableShape : Drawable {
    companion object {
        const val MAX_PRIMITIVES = 4
        fun obtain(pool: Pool<DrawableShape>): DrawableShape =
            pool.acquire()?.setPool(pool) ?: DrawableShape().setPool(pool)
    }

    var program: BasicProgram? = null

    var vertexBuffer: BufferObject? = null

    var elementBuffer: BufferObject? = null

    var vertexOrigin: Vec3 = Vec3()

    var enableDepthTest = true

    private val primitives: Array<DrawableElements>


    private var primitiveCount = 0

    private var pool: Pool<DrawableShape>? = null

    private val scratchMatrix: Matrix4 = Matrix4()

    constructor() {
        val temp = mutableListOf<DrawableElements>()
        for (idx in 0 until MAX_PRIMITIVES) {
            temp.add(idx, DrawableElements())
        }
        primitives = temp.toTypedArray();
    }

    private fun setPool(pool: Pool<DrawableShape>): DrawableShape {
        this.pool = pool
        return this
    }

    fun addDrawElements(mode: Int, count: Int, type: Int, offset: Int): DrawableElements {
        return primitives[primitiveCount++].set(mode, count, type, offset)
    }

    override fun draw(dc: DrawContext) {
        val program = program ?: return
        val vertexBuffer = vertexBuffer ?: return
        val elementBuffer = elementBuffer ?: return
        if (!program.useProgram(dc) || !vertexBuffer.bindBuffer(dc) || !elementBuffer.bindBuffer(dc)) return

        // Use the draw context's pick mode.
        program.enablePickMode(dc.pickMode)

        // Disable texturing.
        program.enableTexture(false)

        // Use the draw context's modelview projection matrix, transformed to shape local coordinates.
        scratchMatrix.set(dc.modelviewProjection)
        scratchMatrix.multiplyByTranslation(vertexOrigin.x, vertexOrigin.y, vertexOrigin.z)
        program.loadModelviewProjection(scratchMatrix)

        // Disable depth testing if requested.
        if (!enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }

        // Disable polygon backface culling in order to draw both sides of the triangles.
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        // Use the shape's vertex point attribute.
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, 0)

        // Draw the specified primitives.
        for (idx in 0 until primitiveCount) {
            val prim = primitives[idx]
            program.loadColor(prim.color)
            GLES20.glLineWidth(prim.lineWidth)
            GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset)
        }

        // Restore the default World Wind OpenGL state.
        if (!enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        GLES20.glLineWidth(1f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    override fun recycle() {
        program = null
        vertexBuffer = null
        elementBuffer = null
        primitiveCount = 0
        pool?.release(this)
        pool = null
    }
}