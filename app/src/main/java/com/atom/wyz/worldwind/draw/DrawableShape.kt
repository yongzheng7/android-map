package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableShape() : Drawable {
    companion object {
        fun obtain(pool: Pool<DrawableShape>): DrawableShape =
            pool.acquire()?.setPool(pool) ?: DrawableShape().setPool(pool)
    }
    var drawState = DrawShapeState()

    private val mvpMatrix = Matrix4()

    private var pool: Pool<DrawableShape>? = null

    private fun setPool(pool: Pool<DrawableShape>): DrawableShape {
        this.pool = pool
        return this
    }


    override fun draw(dc: DrawContext) {
        val program = drawState.program ?: return

        if (!program.useProgram(dc)) {
            return  // program unspecified or failed to build
        }
        val vertexBuffer = drawState.vertexBuffer ?: return
        if ( !vertexBuffer.bindBuffer(dc)) {
            return  // vertex buffer unspecified or failed to bind
        }
        val elementBuffer = drawState.elementBuffer ?: return
        if (!elementBuffer.bindBuffer(dc)) {
            return  // element buffer unspecified or failed to bind
        }

        // Use the draw context's pick mode.
        program.enablePickMode(dc.pickMode)

        // Disable texturing.
        program.enableTexture(false)

        // Use the draw context's modelview projection matrix, transformed to shape local coordinates.
        mvpMatrix.set(dc.modelviewProjection)
        mvpMatrix.multiplyByTranslation(
            drawState.vertexOrigin.x,
            drawState.vertexOrigin.y,
            drawState.vertexOrigin.z
        )
        program.loadModelviewProjection(mvpMatrix)

        // Disable triangle backface culling if requested.
        if (!drawState.enableCullFace) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
        // Disable depth testing if requested.
        if (!drawState.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }


        // Use the shape's vertex point attribute.
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, 0)

        // Draw the specified primitives.
        for (idx in 0 until drawState.primCount) {
            val prim= drawState.prims[idx]
            program.loadColor(prim.color)
            GLES20.glLineWidth(prim.lineWidth)
            GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset)
        }

        // Restore the default World Wind OpenGL state.
        // Restore the default World Wind OpenGL state.
        if (!drawState.enableCullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }
        // Restore the default World Wind OpenGL state.
        if (!drawState.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        GLES20.glLineWidth(1f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    override fun recycle() {
        drawState.reset()
        pool?.release(this)
        pool = null
    }
}