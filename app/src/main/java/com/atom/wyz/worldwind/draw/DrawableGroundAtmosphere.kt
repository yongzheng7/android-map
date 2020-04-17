package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.AtmosphereProgram
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.GroundProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableGroundAtmosphere : Drawable {
    companion object {
        fun obtain( pool : Pool<DrawableGroundAtmosphere>): DrawableGroundAtmosphere {
            return  pool.acquire()?.setPool(pool) ?: DrawableGroundAtmosphere().setPool(pool) // get an instance from the pool
        }
    }

     var program: GroundProgram? = null

    var globeRadius = 0.0

    var lightDirection = Vec3()

     var nightTexture: GpuTexture? = null

    protected var mvpMatrix: Matrix4 = Matrix4()

    protected var texCoordMatrix: Matrix3 = Matrix3()

    protected var fullSphereSector: Sector = Sector().setFullSphere()

    private var pool: Pool<DrawableGroundAtmosphere>? = null

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        program.loadGlobeRadius(this.globeRadius) // TODO the Globe is rendering state

        program.loadEyePoint(dc.eyePoint)

        program.loadLightDirection(lightDirection)

        val terrain= dc.terrain ?: return

        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)

        dc.activeTextureUnit(GLES20.GL_TEXTURE0)
        val textureBound = nightTexture != null && nightTexture!!.bindTexture(dc)

        var idx = 0
        val len: Int = terrain.getTileCount()
        while (idx < len) {
            // Use the vertex origin for the terrain tile.
            val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue
            program.loadVertexOrigin(terrainOrigin)
            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            mvpMatrix.set(dc.modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)
            // Use a tex coord matrix that registers the night texture correctly on each terrain tile.
            if (textureBound) {
                texCoordMatrix.set(nightTexture!!.texCoordTransform)
                texCoordMatrix.multiplyByTileTransform(terrain.getTileSector(idx), fullSphereSector)
                program.loadTexCoordMatrix(texCoordMatrix)
            }
            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0)
            // Draw the terrain tile as triangles, multiplying the current fragment color by the program's secondary color.
            program.loadFragMode(AtmosphereProgram.FRAGMODE_GROUND_SECONDARY)
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO)
            terrain.drawTileTriangles(dc, idx)
            // Draw the terrain tile as triangles, adding the current fragment color to the program's primary color.
            program.loadFragMode(if (textureBound) AtmosphereProgram.FRAGMODE_GROUND_PRIMARY_TEX_BLEND else AtmosphereProgram.FRAGMODE_GROUND_PRIMARY)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
            terrain.drawTileTriangles(dc, idx)
            idx++
        }

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDisableVertexAttribArray(1)
    }

    fun setPool(pool : Pool<DrawableGroundAtmosphere>) : DrawableGroundAtmosphere {
        this.pool = pool
        return this
    }
    override fun recycle() {
        this.program = null
        this.nightTexture = null
        pool?.release(this) // return this instance to the pool
        pool = null
    }
}