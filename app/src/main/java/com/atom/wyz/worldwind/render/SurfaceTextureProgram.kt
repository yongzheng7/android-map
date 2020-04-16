package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.util.*

class SurfaceTextureProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = SurfaceTextureProgram::class.java.name
    }

    // fragment
    protected var mvpMatrixId = 0

    protected var texCoordMatrixId = 0

    protected var texSamplerId = 0

    // vert
    protected var mvpMatrix: Matrix4 = Matrix4()

    protected var texCoordMatrix: Array<Matrix3> = arrayOf(Matrix3(), Matrix3())

    protected var mvpMatrixArray = FloatArray(16)

    protected var texCoordMatrixArray = FloatArray(9 * 2)

    protected var surfaceTextures = ArrayList<SurfaceTexture>()

    protected var intersectingTextures = ArrayList<SurfaceTexture>()

    init {
        try {
            val vs: String =
                WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_surfacetextureprogram_vert)
            val fs: String =
                WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_surfacetextureprogram_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint", "vertexTexCoord")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "SurfaceTextureProgram", "constructor", "errorReadingProgramSource", logged)
        }
    }

    override fun initProgram(dc: DrawContext) {
        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        val identity4x4 = Matrix4() // 4 x 4 identity matrix
        identity4x4.transposeToArray(mvpMatrixArray, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrixArray, 0)

        texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        val identity3x3 = Matrix3() // 3 x 3 identity matrix
        identity3x3.transposeToArray(texCoordMatrixArray, 0)
        identity3x3.transposeToArray(texCoordMatrixArray, 9)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 2, false, texCoordMatrixArray, 0)

        texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(texSamplerId, 0) // GL_TEXTURE0
    }

    fun addSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        if (surfaceTexture != null) {
            surfaceTextures.add(surfaceTexture)
        }
    }

    fun draw(dc: DrawContext) {
        // 获取到地形集合
        val terrain: Terrain = dc.terrain ?: return
        // 地形纹理
        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)
        // 绑定 纹理
        dc.activeTextureUnit(GLES20.GL_TEXTURE0)


        var idx = 0
        val len: Int = terrain.getTileCount()
        //遍历集合中的地形图块
        while (idx < len) {
            //获取的指定索引的土块的范围
            val terrainSector = terrain.getTileSector(idx) ?: continue
            intersectingTextures.clear() // 正在相交的
            run {
                var jidx = 0
                val jlen = this.surfaceTextures.size //所有的相同的图快
                while (jidx < jlen) {
                    val texture = this.surfaceTextures[jidx]
                    if (terrainSector.intersects(texture.sector)) {
                        this.intersectingTextures.add(texture)
                    }
                    jidx++
                }
            }
            // Skip terrain tiles that do not intersect any surface texture.
            if (intersectingTextures.isEmpty()) {
                idx++
                continue
            }
            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            val terrainOrigin = terrain.getTileVertexOrigin(idx) ?: continue
            mvpMatrix.set(dc.modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            mvpMatrix.transposeToArray(mvpMatrixArray, 0)
            GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrixArray, 0)
            // 绘制
            terrain.useVertexPointAttrib(dc, idx, 0)
            var jidx = 0
            val jlen = intersectingTextures.size
            while (jidx < jlen) {
                val texture = intersectingTextures[jidx]
                if (!texture.bindTexture(dc)) {
                    jidx++
                    continue  // texture failed to bind
                }
                // Get the surface texture's sector.
                val textureSector= texture.sector

                texCoordMatrix[0].set(texture.texCoordTransform)

                texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector)
                texCoordMatrix[0].transposeToArray(texCoordMatrixArray, 0)

                texCoordMatrix[1].setToTileTransform(terrainSector, textureSector)
                texCoordMatrix[1].transposeToArray(texCoordMatrixArray, 9)
                GLES20.glUniformMatrix3fv(texCoordMatrixId, 2, false, texCoordMatrixArray, 0)
                // Draw the terrain tile as triangles.
                terrain.drawTileTriangles(dc, idx)
                jidx++
            }
            idx++
        }
    }

    fun clear(dc: DrawContext) {
        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1)
        // Clear references to objects used during drawing.
        surfaceTextures.clear()
        intersectingTextures.clear()
    }
}