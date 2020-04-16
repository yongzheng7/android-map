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

 class BasicProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = BasicProgram::class.java.name
    }

    var mvpMatrixId = 0

    var texCoordMatrixId = 0

    var texSamplerId = 0

    var enableTextureId = 0

    var colorId = 0

    var array = FloatArray(16)

    init {
        try {
            val vert = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_basicprogram_vert)
            val frag = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_basicprogram_frag)
            this.programSources = arrayOf(vert, frag)
            this.attribBindings = arrayOf("vertexPoint", "vertexTexCoord")
        } catch (e: Exception ) {
            Logger.logMessage(Logger.ERROR, "BasicProgram", "constructor", "errorReadingProgramSource", e);
        }
    }

    override fun initProgram( dc: DrawContext) {
        this.enableTextureId = GLES20.glGetUniformLocation(programId, "enableTexture")
        GLES20.glUniform1i(this.enableTextureId, 0)

        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        Matrix4().transposeToArray(this.array, 0) // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0)

        this.texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        Matrix3().transposeToArray(this.array, 0) // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0)

        this.colorId = GLES20.glGetUniformLocation(programId, "color")
        GLES20.glUniform4f(this.colorId, 1f, 1f, 1f, 1f)

        this.texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(this.texSamplerId, 0)

    }

    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }

    fun loadTexCoordMatrix(matrix: Matrix3) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 1, false, array, 0)
    }

    fun enableTexture(enable: Boolean) {
        GLES20.glUniform1i(enableTextureId, if (enable) 1 else 0)
    }

    fun loadColor(color: Color) {
        loadColor(color.red, color.green, color.blue, color.alpha)
    }

    fun loadColor(r: Float, g: Float, b: Float, a: Float) {
        GLES20.glUniform4f(colorId, r * a, g * a, b * a, a)
    }

}