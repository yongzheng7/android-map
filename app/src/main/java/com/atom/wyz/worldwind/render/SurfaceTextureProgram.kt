package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.util.*

class SurfaceTextureProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = SurfaceTextureProgram::class
    }

    // fragment
    protected var mvpMatrixId = 0

    protected var texCoordMatrixId = 0

    protected var texSamplerId = 0

    // vert
    var mvpMatrix: Matrix4 = Matrix4()

    var texCoordMatrix: Array<Matrix3> = arrayOf(Matrix3(), Matrix3())

    private var mvpMatrixArray = FloatArray(16)

    private var texCoordMatrixArray = FloatArray(9 * 2)

    val surfaceTextures = ArrayList<SurfaceTexture>()

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


    fun loadModelviewProjection() {
        mvpMatrix.transposeToArray(mvpMatrixArray, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrixArray, 0)
    }

    fun loadTexCoordMatrix() {
        texCoordMatrix[0].transposeToArray(texCoordMatrixArray, 0)
        texCoordMatrix[1].transposeToArray(texCoordMatrixArray, 9)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 2, false, texCoordMatrixArray, 0)
    }
}