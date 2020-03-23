package com.atom.wyz.worldwind.render

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.util.Logger
import java.util.*

class BasicSurfaceTileRenderer : SurfaceTileRenderer {

    protected var mvpMatrix: Matrix4 = Matrix4()

    protected var texCoordMatrix: Array<Matrix3> = arrayOf(Matrix3(), Matrix3())

    protected var intersectingSurfaceTiles  = ArrayList<SurfaceTile>()

    override fun renderTile(dc: DrawContext, surfaceTile: SurfaceTile?) {

        if (surfaceTile == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicSurfaceTileRenderer", "renderTile", "missingTile"))
        }

        // Make multitexture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        if (!surfaceTile.bindTexture(dc)) {
            return  // surface tile's texture is not in the GPU object cache yet
        }

        var program = dc.getProgram(SurfaceTileProgram.KEY) as SurfaceTileProgram? // program is not in the GPU object cache yet

        if (program == null) {
            program = (dc.putProgram(SurfaceTileProgram.KEY, SurfaceTileProgram(dc.resources!!)) as SurfaceTileProgram )
        }
        // Use World Wind's surface tile GLSL program.
        if (!program.useProgram(dc)) {
            return  // program failed to build
        }

        // Get the draw context's tessellated terrain and the surface tile's sector.
        val terrain: Terrain = dc.terrain ?: return
        val textureSector: Sector = surfaceTile.sector

        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)

        for (idx in 0 until (terrain.getTileCount())){

            val terrainSector: Sector = terrain.getTileSector(idx) ?: continue
            if (!terrainSector.intersects(textureSector)) {
                continue
            }

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue
            mvpMatrix.set(dc.modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)

            // Transform the terrain tile's tex coords to position the surface tile correctly.
            texCoordMatrix[0] .setToIdentity()
            texCoordMatrix[1] .setToIdentity()
            surfaceTile.applyTexCoordTransform(dc, texCoordMatrix[0])
            terrain.applyTexCoordTransform(idx, textureSector, texCoordMatrix[0])
            terrain.applyTexCoordTransform(idx, textureSector, texCoordMatrix[1])
            program.loadTexCoordMatrix(texCoordMatrix)

            terrain.useVertexPointAttrib(dc, idx, 0)

            terrain.drawTileTriangles(dc, idx)
        }

        GLES20.glDisableVertexAttribArray(1)
    }

    override fun renderTiles(dc: DrawContext, surfaceTiles: Iterable<out SurfaceTile?>?) {
        if (surfaceTiles == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicSurfaceTileRenderer", "renderTiles", "missingList"))
        }

        var program = dc.getProgram( SurfaceTileProgram.KEY) as SurfaceTileProgram?

        if(program == null){
            program = dc.putProgram( SurfaceTileProgram.KEY ,  dc.resources ?.let { SurfaceTileProgram(it)} ?: return) as SurfaceTileProgram
        }
        // Use World Wind's surface tile GLSL program.
        if(!program.useProgram(dc)){
            return
        }
        // Get the draw context's tessellated terrain.
        val terrain: Terrain = dc.terrain ?: return
        
        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)

        // Make multitexture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)

        for (idx in 0 until terrain.getTileCount()){
            // Collect the surface tiles that intersect the terrain tile.
            val terrainSector: Sector = terrain.getTileSector(idx) ?: continue

            intersectingSurfaceTiles.clear()
            for (surfaceTile in surfaceTiles) {
                if (terrainSector.intersects(surfaceTile?.sector)) {
                    intersectingSurfaceTiles.add(surfaceTile ?: continue)
                }
            }
            // Skip terrain tiles that do not intersect any surface tile.
            if (this.intersectingSurfaceTiles.isEmpty()) {
                continue
            }

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue

            mvpMatrix.set(dc.modelviewProjection)
                    .multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)

            program.loadModelviewProjection(mvpMatrix)

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0)

            for (texture in intersectingSurfaceTiles) {

                if (!texture.bindTexture(dc)) {
                    continue  // surface tile's texture is not in the GPU object cache yet
                }
                // Get the surface tile's sector.
                val textureSector: Sector = texture.sector
                // Transform the terrain tile's tex coords to position the surface tile correctly.
                texCoordMatrix[0].setToIdentity()
                texCoordMatrix[1].setToIdentity()

                texture.applyTexCoordTransform(dc, texCoordMatrix[0])

                terrain.applyTexCoordTransform(idx, textureSector, texCoordMatrix[0])
                terrain.applyTexCoordTransform(idx, textureSector, texCoordMatrix[1])

                program.loadTexCoordMatrix(texCoordMatrix)
                // Draw the terrain tile vertices as triangles.
                terrain.drawTileTriangles(dc, idx)
            }
        }
    }
}