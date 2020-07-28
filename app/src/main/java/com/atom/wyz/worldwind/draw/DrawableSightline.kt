package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.shader.SensorProgram
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSightline : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableSightline>): DrawableSightline =
            pool.acquire()?.setPool(pool) ?: DrawableSightline().setPool(pool)
    }

    var centerTransform: Matrix4 = Matrix4()

    var range = 0f

    var visibleColor: Color = Color(0f, 0f, 0f, 0f)

    var occludedColor: Color = Color(0f, 0f, 0f, 0f)

    var program: SensorProgram? = null

    val sensorView: Matrix4 = Matrix4()

    val matrix: Matrix4 = Matrix4()

    val cubeMapProjection: Matrix4 = Matrix4()

    val cubeMapFace: Array<Matrix4> = arrayOf(
        Matrix4().setToRotation(0.0, 0.0, 1.0, -90.0)
            .multiplyByRotation(1.0, 0.0, 0.0, 90.0),  // positive X

        Matrix4().setToRotation(0.0, 0.0, 1.0, 90.0)
            .multiplyByRotation(1.0, 0.0, 0.0, 90.0),  // negative X

        Matrix4().setToRotation(1.0, 0.0, 0.0, 90.0),  // positive Y

        Matrix4().setToRotation(0.0, 0.0, 1.0, 180.0)
            .multiplyByRotation(1.0, 0.0, 0.0, 90.0),  // negative Y
        /*new Matrix4().setToRotation(1, 0, 0, 180),*/ // positive Z, intentionally omitted as terrain is never visible when looking up
        Matrix4() // negative Z
    )

    private var pool: Pool<DrawableSightline>? = null

    constructor() {}

    private fun setPool(pool: Pool<DrawableSightline>): DrawableSightline {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val sensorProgram = this.program ?: return
        Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "draw_61" ," 开始渲染"))
        if (!sensorProgram.useProgram(dc)) {
            return  // program unspecified or failed to build
        }
        // Use the drawable's color.
        sensorProgram.loadRange(range)
        sensorProgram.loadColor(visibleColor, occludedColor)

        // Configure the cube map projection matrix to capture one face of the cube map as far as the sensor's range.
        cubeMapProjection.setToPerspectiveProjection(1.0, 1.0, 90.0, 1.0, range.toDouble())

        var idx = 0
        val len = cubeMapFace.size
        Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "draw" ," start -------------"))
        while (idx < len) {
            sensorView.set(centerTransform)
            sensorView.multiplyByMatrix(cubeMapFace[idx])
            sensorView.invertOrthonormal()
            if (this.drawSceneDepth(dc)) {
                this.drawSceneOcclusion(dc)
            }
            Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "draw" ," draw ------$idx----"))

            idx++
        }
    }

    protected fun drawSceneDepth(dc: DrawContext): Boolean {
        try {
            val framebuffer = dc.scratchFramebuffer()
            if (!framebuffer.bindFramebuffer(dc)) {
                return false // framebuffer failed to bind
            }
            // Clear the framebuffer.
            val depthTexture = framebuffer.getAttachedTexture(GLES20.GL_DEPTH_ATTACHMENT)
            GLES20.glViewport(0, 0, depthTexture.textureWidth, depthTexture.textureHeight)
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

            // Draw only depth values offset slightly away from the viewer.
            GLES20.glColorMask(false, false, false, false)
            GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL)
            GLES20.glPolygonOffset(4f, 4f)
            val len = dc.getDrawableTerrainCount()
            for (idx in 0 until len) {
                // Get the drawable terrain associated with the draw context.
                val terrain = dc.getDrawableTerrain(idx)
                val terrainOrigin = terrain?.vertexOrigin ?: continue
                // Use the terrain's vertex point attribute.
                if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                    continue  // vertex buffer failed to bind
                }
                // Draw the terrain onto one face of the cube map, from the sensor's point of view.
                matrix.setToMultiply(cubeMapProjection, sensorView)
                matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
                program!!.loadModelviewProjection(matrix)
                // Draw the terrain as triangles.
                terrain.drawTriangles(dc)
            }
        } finally {
            // Restore the default World Wind OpenGL state.
            dc.bindFramebuffer(0)
            GLES20.glViewport(dc.viewport.x, dc.viewport.y, dc.viewport.width, dc.viewport.height)
            GLES20.glColorMask(true, true, true, true)
            GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL)
            GLES20.glPolygonOffset(0f, 0f)
        }
        return true
    }

    protected fun drawSceneOcclusion(dc: DrawContext) {
        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        val depthTexture = dc.scratchFramebuffer().getAttachedTexture(GLES20.GL_DEPTH_ATTACHMENT)
        if (!depthTexture.bindTexture(dc)) {
            return  // framebuffer texture failed to bind
        }
        val len = dc.getDrawableTerrainCount()
        for (idx in 0 until len) {
            // Get the drawable terrain associated with the draw context.
            val terrain = dc.getDrawableTerrain(idx)
            val terrainOrigin = terrain?.vertexOrigin ?: continue
            // Use the terrain's vertex point attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                continue  // vertex buffer failed to bind
            }

            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            matrix.set(dc.modelviewProjection)
            matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program!!.loadModelviewProjection(matrix)

            // Map the terrain into one face of the cube map, from the sensor's point of view.
            matrix.set(sensorView)
            matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program!!.loadSensorviewProjection(cubeMapProjection, matrix)
            // Draw the terrain as triangles.
            terrain.drawTriangles(dc)
        }
    }

    override fun recycle() {
        visibleColor.set(0f, 0f, 0f, 0f)
        occludedColor.set(0f, 0f, 0f, 0f)
        program = null
        pool?.release(this)
        pool = null
    }

}