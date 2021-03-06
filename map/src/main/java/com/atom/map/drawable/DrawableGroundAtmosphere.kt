package com.atom.map.drawable

import android.opengl.GLES20
import com.atom.map.geom.Matrix3
import com.atom.map.geom.Matrix4
import com.atom.map.geom.Sector
import com.atom.map.geom.Vec3
import com.atom.map.core.shader.AtmosphereProgram
import com.atom.map.core.shader.GpuTexture
import com.atom.map.core.shader.GroundProgram
import com.atom.map.util.pool.Pool

class DrawableGroundAtmosphere : Drawable {

    companion object {
        fun obtain(pool: Pool<DrawableGroundAtmosphere>): DrawableGroundAtmosphere =
            pool.acquire()?.setPool(pool) ?: DrawableGroundAtmosphere()
                .setPool(pool)
    }

    var program: GroundProgram? = null

    var globeRadius = 0.0

    var lightDirection = Vec3()

    var nightTexture: GpuTexture? = null

    var mvpMatrix: Matrix4 = Matrix4()

    var texCoordMatrix: Matrix3 = Matrix3()

    var fullSphereSector: Sector = Sector().setFullSphere()

    var pool: Pool<DrawableGroundAtmosphere>? = null

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        program.loadGlobeRadius(this.globeRadius) // TODO the Globe is rendering state

        program.loadEyePoint(dc.eyePoint)

        program.loadLightDirection(lightDirection)

        GLES20.glEnableVertexAttribArray(1)
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        val textureBound = nightTexture != null && nightTexture!!.bindTexture(dc)


        for (idx in 0 until dc.getDrawableTerrainCount()) {

            // Get the drawable terrain associated with the draw context.
            val terrain = dc.getDrawableTerrain(idx) ?: continue

            // Use the terrain's vertex point attribute and vertex tex coord attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/) ||
                !terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)
            ) {
                continue  // vertex buffer failed to bind
            }

            val terrainOrigin = terrain.vertexOrigin

            program.loadVertexOrigin(terrainOrigin)

            mvpMatrix.set(dc.modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)

            // Use a tex coord matrix that registers the night texture correctly on each terrain tile.
            if (textureBound) {
                texCoordMatrix.set(nightTexture!!.texCoordTransform)
                texCoordMatrix.multiplyByTileTransform(terrain.sector, fullSphereSector)
                program.loadTexCoordMatrix(texCoordMatrix)
            }

            // Draw the terrain as triangles, multiplying the current fragment color by the program's secondary color.
            this.program!!.loadFragMode(AtmosphereProgram.FRAGMODE_SECONDARY)
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO)
            terrain.drawTriangles(dc)
            // Draw the terrain tile as triangles, adding the current fragment color to the program's primary color.
            // Draw the terrain as triangles, adding the current fragment color to the program's primary color.
            this.program!!.loadFragMode(if (textureBound) AtmosphereProgram.FRAGMODE_PRIMARY_TEX_BLEND else AtmosphereProgram.FRAGMODE_PRIMARY)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
            terrain.drawTriangles(dc)
        }

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDisableVertexAttribArray(1)
    }

    fun setPool(pool: Pool<DrawableGroundAtmosphere>): DrawableGroundAtmosphere {
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