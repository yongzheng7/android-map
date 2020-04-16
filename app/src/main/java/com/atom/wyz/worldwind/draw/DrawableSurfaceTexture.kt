package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
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

    protected var texCoordMatrix = Matrix3()

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
        this.texture = texture
        if (sector != null) {
            this.sector.set(sector)
        } else {
            this.sector.setEmpty()
        }
        if (texCoordMatrix != null) {
            this.texCoordMatrix.set(texCoordMatrix)
        } else {
            this.texCoordMatrix.setToIdentity()
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
        try {
            program.addSurfaceTexture(this)

            var next: Drawable?
            while (dc.peekDrawable().also { next = it } != null
                && this.canBatchWith(next!!)) {
                program.addSurfaceTexture(dc.pollDrawable() as SurfaceTexture?) // take it off the queue
            }
            // Draw the accumulated  surface textures.
            this.drawSurfaceTextures(dc)

        } finally {
            // Clear the program's accumulated surface textures.
            this.program!!.surfaceTextures.clear()
        }
    }

    protected fun drawSurfaceTextures(dc: DrawContext) {
        val terrain = dc.terrain ?: return
        val program = program ?: return

        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        var idx = 0
        val len: Int = terrain.getTileCount()
        while (idx < len) {
            // Get the terrain tile's sector, and keep a flag to ensure we apply the terrain tile's state at most once.
            val terrainSector = terrain.getTileSector(idx) ?: continue
            var usingTerrainTileState = false
            var jidx = 0
            val jlen = program.surfaceTextures.size
            while (jidx < jlen) {

                val texture = program.surfaceTextures[jidx]
                val textureSector = texture.sector
                if (!textureSector.intersects(terrainSector)) {
                    jidx++
                    continue
                }
                if (!texture.bindTexture(dc)) {
                    jidx++
                    continue
                }
                if (!usingTerrainTileState) {

                    val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue
                    program.mvpMatrix.set(dc.modelviewProjection)
                    program.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
                    program.loadModelviewProjection()
                    terrain.useVertexPointAttrib(dc, idx, 0)
                    usingTerrainTileState = true
                }
                program.texCoordMatrix[0].set(texture.texCoordTransform)
                program.texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector)
                program.texCoordMatrix[1].setToTileTransform(terrainSector, textureSector)
                program.loadTexCoordMatrix()

                terrain.drawTileTriangles(dc, idx)
                jidx++
            }
            idx++
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