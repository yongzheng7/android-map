package com.atom.map.core.shader

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.map.drawable.DrawContext
import com.atom.map.R
import com.atom.map.geom.Matrix4
import com.atom.map.util.Logger
import com.atom.map.util.WWUtil

class CartesianProgram(resources: Resources) : GpuProgram() {
    companion object {
        val KEY: Any = CartesianProgram::class
    }

    var mvpMatrix = Matrix4()

    var mvpMatrixId = 0

    var array = FloatArray(16)

    init {
        try {
            val vert = WWUtil.readResourceAsText(resources, R.raw.cartesian_program_vert)
            val frag = WWUtil.readResourceAsText(resources, R.raw.cartesian_program_frag)
            this.programSources = arrayOf(vert, frag)
            this.attribBindings = arrayOf("vertexPoint")
        } catch (e: Exception) {
            Logger.logMessage(Logger.ERROR, "CartesianProgram", "constructor", "errorReadingProgramSource", e);
        }
    }
    override fun initProgram(dc: DrawContext) {
        this.mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        this.mvpMatrix.transposeToArray(this.array, 0) // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0)

    }
    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }
}