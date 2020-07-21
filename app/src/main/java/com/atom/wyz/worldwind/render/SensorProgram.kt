package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil

class SensorProgram : GpuProgram {
    companion object {
        val KEY: Any = SensorProgram::class.java
    }

    protected var mvpMatrixId = 0

    protected var svpMatrixId = 0

    protected var rangeId = 0

    protected var depthSamplerId = 0

    protected var colorId = 0

    private val array = FloatArray(32)

    constructor(resources: Resources) {
        try {
            val vs: String =
                WWUtil.readResourceAsText(resources, R.raw.sensorprogram_vert)
            val fs: String =
                WWUtil.readResourceAsText(resources, R.raw.sensorprogram_frag)
            this.programSources = arrayOf(vs, fs)
            this.attribBindings = arrayOf("vertexPoint")
        } catch (logged: Exception) {
            Logger.logMessage(
                Logger.ERROR,
                "SensorProgram",
                "constructor",
                "errorReadingProgramSource",
                logged
            )
        }
    }

    override fun initProgram(dc: DrawContext) {
        mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
        svpMatrixId = GLES20.glGetUniformLocation(programId, "svpMatrix")
        GLES20.glUniformMatrix4fv(svpMatrixId, 2, false, array, 0)
        rangeId = GLES20.glGetUniformLocation(programId, "range")
        GLES20.glUniform1f(rangeId, 0f)
        colorId = GLES20.glGetUniformLocation(programId, "color")
        GLES20.glUniform4f(colorId, 1f, 1f, 1f, 1f)
        depthSamplerId = GLES20.glGetUniformLocation(programId, "depthSampler")
        GLES20.glUniform1i(depthSamplerId, 0) // GL_TEXTURE0
    }

    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }

    fun loadSensorviewProjection(projection: Matrix4, sensorview: Matrix4) {
        projection.transposeToArray(array, 0)
        sensorview.transposeToArray(array, 16)
        GLES20.glUniformMatrix4fv(svpMatrixId, 2, false, array, 0)
    }

    fun loadRange(range: Float) {
        GLES20.glUniform1f(rangeId, range)
    }

    fun loadColor(visibleColor: Color, occludedColor: Color) {
        visibleColor.premultiplyToArray(array, 0)
        occludedColor.premultiplyToArray(array, 4)
        GLES20.glUniform4fv(colorId, 2, array, 0)
    }
}