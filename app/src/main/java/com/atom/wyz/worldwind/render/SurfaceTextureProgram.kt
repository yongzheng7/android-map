package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class SurfaceTextureProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = SurfaceTextureProgram::class
    }

    // fragment
    protected var mvpMatrixId = 0

    protected var texCoordMatrixId = 0

    protected var texSamplerId = 0

    protected var enablePickModeId = 0

    protected var enableTextureId = 0

    protected var colorId = 0

    private val color: Color = Color()

    // vert
    var mvpMatrix: Matrix4 = Matrix4()

    var texCoordMatrix: Array<Matrix3> = arrayOf(Matrix3(), Matrix3())

    private var mvpMatrixArray = FloatArray(16)

    private var texCoordMatrixArray = FloatArray(9 * 2)

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
        enablePickModeId = GLES20.glGetUniformLocation(programId, "enablePickMode")
        GLES20.glUniform1i(enablePickModeId, 0) // disable pick mode

        enableTextureId = GLES20.glGetUniformLocation(programId, "enableTexture")
        GLES20.glUniform1i(enableTextureId, 0) // disable texture

        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        Matrix4().transposeToArray(mvpMatrixArray, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, mvpMatrixArray, 0)

        texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        Matrix3().transposeToArray(texCoordMatrixArray, 0)
        Matrix3().transposeToArray(texCoordMatrixArray, 9)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 2, false, texCoordMatrixArray, 0)

        colorId = GLES20.glGetUniformLocation(programId, "color")
        color.set(1f, 1f, 1f, 1f) // opaque white
        GLES20.glUniform4f(colorId, color.red, color.green, color.blue, color.alpha)

        texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(texSamplerId, 0) // GL_TEXTURE0
    }

    fun enablePickMode(enable: Boolean) {
        GLES20.glUniform1i(enablePickModeId, if (enable) 1 else 0)
    }

    fun enableTexture(enable: Boolean) {
        GLES20.glUniform1i(enableTextureId, if (enable) 1 else 0)
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
    fun loadColor(color: Color) {
        if (!this.color.equals(color)) {
            // suppress unnecessary writes to GLSL uniform variables
            this.color.set(color)
            val a = color.alpha
            GLES20.glUniform4f(colorId, color.red * a, color.green * a, color.blue * a, a)
        }
    }
}