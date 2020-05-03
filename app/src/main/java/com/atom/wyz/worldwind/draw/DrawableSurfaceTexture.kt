package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.SurfaceTexture
import com.atom.wyz.worldwind.render.SurfaceTextureProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSurfaceTexture : Drawable, SurfaceTexture {
    companion object {
        fun obtain(pool: Pool<DrawableSurfaceTexture>): DrawableSurfaceTexture {
            return pool.acquire()?.setPool(pool) ?: DrawableSurfaceTexture().setPool(pool)
        }
    }

    protected var program: SurfaceTextureProgram? = null

    protected var texture: GpuTexture? = null

    var color: Color = Color()

    override var sector: Sector = Sector()
        get() = field
        set(value) {
            field.set(value)
        }

    override var texCoordTransform: Matrix3 = Matrix3()
        get() = field
        set(value) {
            field.set(value)
        }
    private var pool: Pool<DrawableSurfaceTexture>? = null
    private fun setPool(pool: Pool<DrawableSurfaceTexture>): DrawableSurfaceTexture {
        this.pool = pool
        return this
    }

    operator fun set(
        program: SurfaceTextureProgram?,
        sector: Sector?,
        texture: GpuTexture?,
        texCoordMatrix: Matrix3?
    ): DrawableSurfaceTexture {
        this.program = program
        this.color.set(1f, 1f, 1f, 1f)
        this.texture = texture
        if (sector != null) {
            this.sector.set(sector)
        } else {
            this.sector.setEmpty()
        }
        if (texCoordMatrix != null) {
            this.texCoordTransform.set(texCoordMatrix)
        } else {
            this.texCoordTransform.setToIdentity()
        }
        return this
    }

    override fun bindTexture(dc: DrawContext): Boolean {
        return texture != null && texture!!.bindTexture(dc)
    }


    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return  // program failed to build
        }
        val scratchList = dc.scratchList()
        try {
            scratchList.add(this)
            var next: Drawable?
            while (dc.peekDrawable().also { next = it } != null
                && this.canBatchWith(next!!)) {
                scratchList.add(dc.pollDrawable() as SurfaceTexture?) // take it off the queue
            }
            // Draw the accumulated  surface textures.
            this.drawSurfaceTextures(dc)

        } finally {
            scratchList.clear()
        }
    }

    protected fun drawSurfaceTextures(dc: DrawContext) {
        // Use the draw context's pick mode.
        program!!.enablePickMode(dc.pickMode)

        // Enable the program to display surface textures from multitexture unit 0.
        program!!.enableTexture(true)

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        GLES20.glEnableVertexAttribArray(1)
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        val scratchList = dc.scratchList()

        for (idx in 0 until dc.getDrawableTerrainCount()) {
            // Get the drawable terrain associated with the draw context.
            val terrain = dc.getDrawableTerrain(idx) ?: continue

            // Get the terrain's attributes, and keep a flag to ensure we apply the terrain's attributes at most once.
            val terrainSector = terrain.sector
            val terrainOrigin = terrain.vertexOrigin
            var usingTerrainAttrs = false

            for (i in 0 until scratchList.size) {
                // Get the surface texture and its sector.
                val texture = scratchList.get(i) as DrawableSurfaceTexture? ?:continue
                val textureSector = texture.sector

                if (!textureSector.intersects(terrainSector)) {
                    continue  // texture does not intersect the terrain
                }

                if (!texture.bindTexture(dc)) {
                    continue  // texture failed to bind
                }

                if (!usingTerrainAttrs ||
                    terrain.useVertexPointAttrib(dc, 0) &&
                    terrain.useVertexTexCoordAttrib(dc, 1)) {
                    // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
                    // Suppress subsequent tile state application until the next terrain.
                    usingTerrainAttrs = true
                    this.program!!.mvpMatrix.set(dc.modelviewProjection)
                    this.program!!.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
                    this.program!!.loadModelviewProjection()
                    // Use the terrain's vertex point attribute and vertex tex coord attribute.
                    terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)
                    terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)
                    // Suppress subsequent tile state application until the next terrain.
                }
                if (!usingTerrainAttrs) {
                    continue  // terrain vertex attribute failed to bind
                }
                program!!.texCoordMatrix[0].set(texture.texCoordTransform)
                program!!.texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector)
                program!!.texCoordMatrix[1].setToTileTransform(terrainSector, textureSector)
                program!!.loadTexCoordMatrix()
                // Use the surface texture's RGBA color.
                program!!.loadColor(texture.color)

                terrain.drawTriangles(dc)
            }

        }

        GLES20.glDisableVertexAttribArray(1)
    }

    override fun recycle() {
        texture = null
        program = null
        pool?.release(this)
        pool = null
    }

    protected fun canBatchWith(that: Drawable): Boolean {
        return this.javaClass == that::javaClass && this.program === (that as DrawableSurfaceTexture).program
    }
}