package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSurfaceShape : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableSurfaceShape>): DrawableSurfaceShape =
            pool.acquire()?.setPool(pool) ?: DrawableSurfaceShape().setPool(pool)
    }

    var sector = Sector()

    val textureMvpMatrix = Matrix4()

    val identityMatrix3: Matrix3 = Matrix3()

    val color: Color = Color()

    var drawState = DrawShapeState()

    val mvpMatrix: Matrix4 = Matrix4()

    var pool: Pool<DrawableSurfaceShape>? = null

    private fun setPool(pool: Pool<DrawableSurfaceShape>): DrawableSurfaceShape {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = drawState.program ?: return
        if (!program.useProgram(dc)) return

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        GLES20.glEnableVertexAttribArray(1 /*vertexTexCoord*/)

        val scratchList = dc.scratchList()

        try {
            scratchList.add(this)
            var next: Drawable?
            while (dc.peekDrawable().also {
                    next = it
                } != null && next!!.javaClass == this.javaClass) {
                scratchList.add(dc.pollDrawable()) // take it off the queue
            }

            var idx = 0
            val len = dc.getDrawableTerrainCount()
            while (idx < len) {
                val terrain = dc.getDrawableTerrain(idx) ?: continue
                if (this.drawShapesToTexture(dc, terrain) > 0) {
                    this.drawTextureToTerrain(dc, terrain)
                }
                idx++
            }
        } finally {
            scratchList.clear()
            GLES20.glDisableVertexAttribArray(1 /*vertexTexCoord*/)
        }
    }

    protected fun drawShapesToTexture(
        dc: DrawContext,
        terrain: DrawableTerrain
    ): Int {
        val scratchList = dc.scratchList()
        val terrainSector = terrain.sector
        var shapeCount = 0
        try {
            val framebuffer = dc.scratchFramebuffer()
            if (!framebuffer.bindFramebuffer(dc)) {
                return 0
            }
            val colorAttachment = framebuffer.getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0)
            GLES20.glViewport(0, 0, colorAttachment.textureWidth, colorAttachment.textureHeight)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            drawState.program!!.enablePickMode(dc.pickMode)
            textureMvpMatrix.setToIdentity()
            textureMvpMatrix.multiplyByTranslation((-1).toDouble(), (-1).toDouble(), 0.0)
            textureMvpMatrix.multiplyByScale(
                2 / terrainSector.deltaLongitude(),
                2 / terrainSector.deltaLatitude(),
                0.0
            )
            textureMvpMatrix.multiplyByTranslation(
                -terrainSector.minLongitude,
                -terrainSector.minLatitude,
                0.0
            )
            var idx = 0
            val len = scratchList.size
            while (idx < len) {
                val shape = scratchList[idx] as DrawableSurfaceShape?
                if (!shape!!.sector.intersectsOrNextTo(terrainSector)) {
                    idx++
                    continue
                }
                if (shape.drawState.vertexBuffer == null || !shape.drawState.vertexBuffer!!.bindBuffer(
                        dc
                    )
                ) {
                    idx++
                    continue  // vertex buffer unspecified or failed to bind
                }
                if (shape.drawState.elementBuffer == null || !shape.drawState.elementBuffer!!.bindBuffer(
                        dc
                    )
                ) {
                    idx++
                    continue  // element buffer unspecified or failed to bind
                }

                mvpMatrix.set(textureMvpMatrix)
                mvpMatrix.multiplyByTranslation(
                    shape.drawState.vertexOrigin.x,
                    shape.drawState.vertexOrigin.y,
                    shape.drawState.vertexOrigin.z
                )
                drawState.program!!.loadModelviewProjection(mvpMatrix)

                GLES20.glVertexAttribPointer(
                    0 /*vertexPoint*/,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    shape.drawState.vertexStride,
                    0
                )
                for (primIdx in 0 until shape.drawState.primCount) {
                    val prim = shape.drawState.prims[primIdx]
                    drawState.program!!.loadColor(prim.color)

                    if (prim.texture != null && prim.texture!!.bindTexture(dc)) {
                        drawState.program!!.loadTexCoordMatrix(prim.texCoordMatrix)
                        drawState.program!!.enableTexture(true)
                    } else {
                        drawState.program!!.enableTexture(false)
                    }

                    GLES20.glVertexAttribPointer(
                        1 /*vertexTexCoord*/,
                        prim.texCoordAttrib.size,
                        GLES20.GL_FLOAT,
                        false,
                        shape.drawState.vertexStride,
                        prim.texCoordAttrib.offset
                    )

                    GLES20.glLineWidth(prim.lineWidth)
                    GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset)
                }
                shapeCount++
                idx++
            }
        } finally { // Restore the default World Wind OpenGL state.
            dc.bindFramebuffer(0)
            GLES20.glViewport(dc.viewport.x, dc.viewport.y, dc.viewport.width, dc.viewport.height)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glLineWidth(1f)
        }
        return shapeCount
    }

    protected fun drawTextureToTerrain(dc: DrawContext, terrain: DrawableTerrain) {
        if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
            return  // terrain vertex attribute failed to bind
        }
        if (!terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)) {
            return  // terrain vertex attribute failed to bind
        }
        val colorAttachment =
            dc.scratchFramebuffer().getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0)
        if (!colorAttachment.bindTexture(dc)) {
            return  // framebuffer texture failed to bind
        }
        drawState.program!!.enablePickMode(false)
        drawState.program!!.enableTexture(true)
        drawState.program!!.loadTexCoordMatrix(identityMatrix3)
        drawState.program!!.loadColor(this.color)
        // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
        val terrainOrigin = terrain.vertexOrigin
        mvpMatrix.set(dc.modelviewProjection)
        mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
        drawState.program!!.loadModelviewProjection(mvpMatrix)
        // Draw the terrain as triangles.
        terrain.drawTriangles(dc)
    }

    override fun recycle() {
        drawState.reset()
        pool?.release(this)
        pool = null
    }
}