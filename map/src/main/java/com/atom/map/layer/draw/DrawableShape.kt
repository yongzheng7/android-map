package com.atom.map.layer.draw

import android.opengl.GLES20
import com.atom.map.geom.Matrix4
import com.atom.map.util.pool.Pool

class DrawableShape() : Drawable {
    companion object {
        fun obtain(pool: Pool<DrawableShape>): DrawableShape =
            pool.acquire()?.setPool(pool) ?: DrawableShape()
                .setPool(pool)
    }

    var drawState = DrawShapeState()

    val mvpMatrix = Matrix4()

    var pool: Pool<DrawableShape>? = null

    private fun setPool(pool: Pool<DrawableShape>): DrawableShape {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = drawState.program ?: return
        if (!program.useProgram(dc)) return

        val vertexBuffer = drawState.vertexBuffer ?: return
        if (!vertexBuffer.bindBuffer(dc)) return

        val elementBuffer = drawState.elementBuffer ?: return
        if (!elementBuffer.bindBuffer(dc)) return

        // Use the draw context's pick mode.
        program.enablePickMode(dc.pickMode)

        // Use the draw context's modelview projection matrix, transformed to shape local coordinates.
        if (drawState.depthOffset != 0.0) {
            mvpMatrix.set(dc.projection).offsetProjectionDepth(drawState.depthOffset)
            mvpMatrix.multiplyByMatrix(dc.modelview)
        } else {
            mvpMatrix.set(dc.modelviewProjection)
        }

        mvpMatrix.multiplyByTranslation(
            drawState.vertexOrigin.x,
            drawState.vertexOrigin.y,
            drawState.vertexOrigin.z
        )
        program.loadModelviewProjection(mvpMatrix)

        if (!drawState.enableCullFace) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }

        if (!drawState.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }

        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        GLES20.glEnableVertexAttribArray(1 /*vertexTexCoord*/)
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, this.drawState.vertexStride, 0)

        // Draw the specified primitives.
        for (idx in 0 until drawState.primCount) {
            val prim = drawState.prims[idx]
            program.loadColor(prim.color)
            if (prim.texture != null && prim.texture!!.bindTexture(dc)) {
                program.loadTexCoordMatrix(prim.texCoordMatrix)
                program.enableTexture(true)
            } else {
                program.enableTexture(false)
            }
            GLES20.glVertexAttribPointer(
                1 /*vertexTexCoord*/,
                prim.texCoordAttrib.size,
                GLES20.GL_FLOAT,
                false,
                drawState.vertexStride,
                prim.texCoordAttrib.offset
            )
            GLES20.glLineWidth(prim.lineWidth)
            GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset)
        }

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
        GLES20.glDisableVertexAttribArray(1 /*vertexTexCoord*/)
    }

    override fun recycle() {
        drawState.reset()
        pool?.release(this)
        pool = null
    }
}