package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class SurfaceTileProgram(resources: Resources) : GpuProgram() {

    companion object {
        val KEY: Any = SurfaceTileProgram::class.java.name
    }

    protected var mvpMatrixId = 0

    protected var texCoordMatrixId = 0

    protected var texSamplerId = 0

    protected var array = FloatArray(18)

    init {
        try {
            val vert = WWUtil.readResourceAsText(resources, R.raw.surface_worldwind_vert)
            val frag = WWUtil.readResourceAsText(resources, R.raw.surface_worldwind_frag)
            this.programSources = arrayOf(vert, frag)
            this.attribBindings = arrayOf("vertexPoint", "vertexTexCoord")
        } catch (logged: Exception) {
            Logger.logMessage(Logger.ERROR, "SurfaceTileProgram", "constructor", "errorReadingProgramSource", logged);
        }
    }

    override fun initProgram(dc: DrawContext) {

        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        val identity4x4: Matrix4 = Matrix4()
        identity4x4.transposeToArray(this.array, 0)
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0)

        this.texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        val identity3x3: Matrix3 = Matrix3()
        identity3x3.transposeToArray(this.array, 0) // 3 x 3 identity matrix
        identity3x3.transposeToArray(this.array, 9) // 3 x 3 identity matrix

        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.array, 0)

        this.texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(this.texSamplerId, 0) // GL_TEXTURE0

    }


    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }

    fun loadTexCoordMatrix(matrix: Array<Matrix3>) {
        matrix[0].transposeToArray(array, 0)
        matrix[1].transposeToArray(array, 9)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 2, false, array, 0)
    }
}