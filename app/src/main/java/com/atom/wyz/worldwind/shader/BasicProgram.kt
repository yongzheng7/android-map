package com.atom.wyz.worldwind.shader

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class BasicProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = BasicProgram::class
    }

    var enablePickMode = false

    var enableTexture = false

    var mvpMatrix = Matrix4()

    var texCoordMatrix = Matrix3()

    var color = Color()

    var enablePickModeId = 0

    var mvpMatrixId = 0

    var texCoordMatrixId = 0

    var texSamplerId = 0

    var enableTextureId = 0

    var colorId = 0

    var array = FloatArray(16)

    init {
        try {
            val vert = WWUtil.readResourceAsText(resources, R.raw.basic_program_vert)
            val frag = WWUtil.readResourceAsText(resources, R.raw.basic_program_frag)
            this.programSources = arrayOf(vert, frag)
            this.attribBindings = arrayOf("vertexPoint", "vertexTexCoord")
        } catch (e: Exception) {
            Logger.logMessage(Logger.ERROR, "BasicProgram", "constructor", "errorReadingProgramSource", e);
        }
    }

    override fun initProgram(dc: DrawContext) {
        enablePickModeId = GLES20.glGetUniformLocation(programId, "enablePickMode")
        GLES20.glUniform1i(enablePickModeId, if (enablePickMode) 1 else 0) // disable pick mode

        this.enableTextureId = GLES20.glGetUniformLocation(programId, "enableTexture")
        GLES20.glUniform1i(this.enableTextureId, if (enableTexture) 1 else 0)

        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        this.mvpMatrix.transposeToArray(this.array, 0) // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0)

        this.texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        this.texCoordMatrix.transposeToArray(this.array, 0) // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0)

        this.colorId = GLES20.glGetUniformLocation(programId, "color")
        val alpha = color.alpha
        GLES20.glUniform4f(this.colorId, this.color.red * alpha, this.color.green * alpha, this.color.blue * alpha, alpha)

        this.texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(this.texSamplerId, 0)

    }

    fun enablePickMode(enable: Boolean) {
        if (enablePickMode != enable) {
            enablePickMode = enable
            GLES20.glUniform1i(enablePickModeId, if (enable) 1 else 0)
        }
    }
    fun enableTexture(enable: Boolean) {
        if (enableTexture != enable) {
            enableTexture = enable
            GLES20.glUniform1i(enableTextureId, if (enable) 1 else 0)
        }
    }

    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }

    fun loadTexCoordMatrix(matrix: Matrix3) {
        if (!texCoordMatrix.equals(matrix)) {
            texCoordMatrix.set(matrix)
            matrix.transposeToArray(array, 0)
            GLES20.glUniformMatrix3fv(texCoordMatrixId, 1, false, array, 0)
        }
    }


    fun loadColor(color: Color) {
        if (!this.color.equals(color)) {
            this.color.set(color)
            val alpha = color.alpha
            GLES20.glUniform4f(colorId, color.red * alpha, color.green * alpha, color.blue * alpha, alpha)
        }
    }
}