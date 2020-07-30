package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import android.util.Log
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.pool.Pool

/**
 * 经纬度的drawable绘制
 */
class DrawableSurfaceShape : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableSurfaceShape>): DrawableSurfaceShape =
            pool.acquire()?.setPool(pool) ?: DrawableSurfaceShape().setPool(pool)
    }

    var sector = Sector()

    val mvpMatrix = Matrix4()

    private val textureMvpMatrix = Matrix4()

    private val identityMatrix3 = Matrix3()

    val color = Color()

    var drawState = DrawShapeState()

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
                } != null && next!!::class == this::class) {
                scratchList.add(dc.pollDrawable()!!) // take it off the queue
            }

            for (idx in 0 until dc.getDrawableTerrainCount()) {
                val terrain = dc.getDrawableTerrain(idx) ?: continue
                if (this.drawShapesToTexture(dc, terrain) > 0) {
                    this.drawTextureToTerrain(dc, terrain)
                }
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
        val basicProgram = drawState.program ?: return 0
        val scratchList = dc.scratchList()
        val terrainSector = terrain.sector
        var shapeCount = 0
        try {
            val framebuffer = dc.scratchFramebuffer()
            if (!framebuffer.bindFramebuffer(dc)) { // 绘制帧缓存上
                return shapeCount
            }
            val colorAttachment = framebuffer.getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0)
            GLES20.glViewport(0, 0, colorAttachment.textureWidth, colorAttachment.textureHeight)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)

            basicProgram.enablePickMode(dc.pickMode)

            textureMvpMatrix.setToIdentity()
            textureMvpMatrix.multiplyByTranslation(-0.0, -0.0, 0.0)
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
            val len = scratchList.size
            for (idx in 0 until len) {
                val shape = scratchList[idx] as DrawableSurfaceShape
                if (!shape.sector.intersectsOrNextTo(terrainSector)) {
                    continue
                }
                if (shape.drawState.vertexBuffer == null || !shape.drawState.vertexBuffer!!.bindBuffer(dc)) {
                    continue  // vertex buffer unspecified or failed to bind
                }
                if (shape.drawState.elementBuffer == null || !shape.drawState.elementBuffer!!.bindBuffer(dc)) {
                    continue  // element buffer unspecified or failed to bind
                }
                mvpMatrix.set(textureMvpMatrix)
                mvpMatrix.multiplyByTranslation(
                    shape.drawState.vertexOrigin.x,
                    shape.drawState.vertexOrigin.y,
                    shape.drawState.vertexOrigin.z
                )
                basicProgram.loadModelviewProjection(mvpMatrix)

                GLES20.glVertexAttribPointer(
                    0 /*vertexPoint*/, 3, GLES20.GL_FLOAT,
                    false, shape.drawState.vertexStride, 0)

                for (primIdx in 0 until shape.drawState.primCount) {
                    val prim = shape.drawState.prims[primIdx]
                    basicProgram.loadColor(prim.color)
                    if (prim.texture != null && prim.texture!!.bindTexture(dc)) {
                        basicProgram.loadTexCoordMatrix(prim.texCoordMatrix)
                        basicProgram.enableTexture(true)
                    } else {
                        basicProgram.enableTexture(false)
                    }
                    GLES20.glVertexAttribPointer(
                        1, prim.texCoordAttrib.size, GLES20.GL_FLOAT,
                        false, shape.drawState.vertexStride, prim.texCoordAttrib.offset
                    )
                    GLES20.glLineWidth(prim.lineWidth)
                    GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset)
                }
                shapeCount++
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
        Logger.logMessage(Log.ERROR, "DrawableSurfaceShape", "drawTextureToTerrain", "1")
        val basicProgram = drawState.program ?: return

        if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
            return  // terrain vertex attribute failed to bind
        }
        Logger.logMessage(Log.ERROR, "DrawableSurfaceShape", "drawTextureToTerrain", "2")

        if (!terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)) {
            return  // terrain vertex attribute failed to bind
        }
        Logger.logMessage(Log.ERROR, "DrawableSurfaceShape", "drawTextureToTerrain", "3")

        val colorAttachment = dc.scratchFramebuffer().getAttachedTexture(GLES20.GL_COLOR_ATTACHMENT0)
        if (!colorAttachment.bindTexture(dc)) {
            return  // framebuffer texture failed to bind
        }
        Logger.logMessage(Log.ERROR, "DrawableSurfaceShape", "drawTextureToTerrain", "4")

        basicProgram.enablePickMode(false)
        basicProgram.enableTexture(true)
        basicProgram.loadTexCoordMatrix(identityMatrix3)
        basicProgram.loadColor(this.color)

        // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
        val terrainOrigin = terrain.vertexOrigin
        mvpMatrix.set(dc.modelviewProjection)
        mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
        basicProgram.loadModelviewProjection(mvpMatrix)
        // Draw the terrain as triangles.
        terrain.drawTriangles(dc)
        Logger.logMessage(Log.ERROR, "DrawableSurfaceShape", "drawTextureToTerrain", "5")
    }

    override fun recycle() {
        drawState.reset()
        pool?.release(this)
        pool = null
    }
}