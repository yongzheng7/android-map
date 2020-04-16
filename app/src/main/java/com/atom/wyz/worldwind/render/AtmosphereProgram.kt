package com.atom.wyz.worldwind.render

import android.opengl.GLES20
import androidx.annotation.IntDef
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.Globe
import java.util.*

open class AtmosphereProgram() : GpuProgram() {

    @IntDef(FRAGMODE_SKY, FRAGMODE_GROUND_PRIMARY, FRAGMODE_GROUND_SECONDARY, FRAGMODE_GROUND_PRIMARY_TEX_BLEND)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class FragMode

    companion object {
        const val FRAGMODE_SKY = 1

        const val FRAGMODE_GROUND_PRIMARY = 2

        const val FRAGMODE_GROUND_SECONDARY = 3

        const val FRAGMODE_GROUND_PRIMARY_TEX_BLEND = 4
    }

    var altitude = 0.0

    protected var fragModeId = 0

    protected var mvpMatrixId = 0

    protected var texCoordMatrixId = 0

    protected var texSamplerId = 0

    protected var eyePointId = 0

    protected var vertexOriginId = 0

    protected var eyeMagnitudeId = 0

    protected var eyeMagnitude2Id = 0

    protected var lightDirectionId = 0

    protected var invWavelengthId = 0

    protected var atmosphereRadiusId = 0

    protected var atmosphereRadius2Id = 0

    protected var globeRadiusId = 0

    protected var KrESunId = 0

    protected var KmESunId = 0

    protected var Kr4PIId = 0

    protected var Km4PIId = 0

    protected var scaleId = 0

    protected var scaleDepthId = 0

    protected var scaleOverScaleDepthId = 0

    protected var gId = 0

    protected var g2Id = 0

    protected var exposureId = 0

    protected var array = FloatArray(16)


    override fun initProgram(dc: DrawContext) {

        altitude = 160000.0
        val invWavelength: Vec3 = Vec3(
            1 / Math.pow(0.650, 4.0),  // 650 nm for red
            1 / Math.pow(0.570, 4.0),  // 570 nm for green
            1 / Math.pow(0.475, 4.0)
        ) // 475 nm for blue

        val rayleighScaleDepth = 0.25
        val Kr = 0.0025 // Rayleigh scattering constant

        val Km = 0.0010 // Mie scattering constant

        val ESun = 20.0 // Sun brightness constant

        val g = -0.990 // The Mie phase asymmetry factor

        val exposure = 2.0

        fragModeId = GLES20.glGetUniformLocation(programId, "fragMode")
        GLES20.glUniform1i(fragModeId, FRAGMODE_SKY)

        mvpMatrixId = GLES20.glGetUniformLocation(programId, "mvpMatrix")
        Matrix4().transposeToArray(array, 0) // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)

        texCoordMatrixId = GLES20.glGetUniformLocation(programId, "texCoordMatrix")
        Matrix3().transposeToArray(array, 0) // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 1, false, array, 0)

        texSamplerId = GLES20.glGetUniformLocation(programId, "texSampler")
        GLES20.glUniform1i(texSamplerId, 0) // GL_TEXTURE0

        vertexOriginId = GLES20.glGetUniformLocation(programId, "vertexOrigin")
        Arrays.fill(array, 0f)
        GLES20.glUniform3fv(vertexOriginId, 1, array, 0)

        eyePointId = GLES20.glGetUniformLocation(programId, "eyePoint")
        Arrays.fill(array, 0f)
        GLES20.glUniform3fv(eyePointId, 1, array, 0)

        eyeMagnitudeId = GLES20.glGetUniformLocation(programId, "eyeMagnitude")
        GLES20.glUniform1f(eyeMagnitudeId, 0f)

        eyeMagnitude2Id = GLES20.glGetUniformLocation(programId, "eyeMagnitude2")
        GLES20.glUniform1f(eyeMagnitude2Id, 0f)

        lightDirectionId = GLES20.glGetUniformLocation(programId, "lightDirection")
        Arrays.fill(array, 0f)
        GLES20.glUniform3fv(lightDirectionId, 1, array, 0)

        invWavelengthId = GLES20.glGetUniformLocation(programId, "invWavelength")
        invWavelength.toArray(array, 0)
        GLES20.glUniform3fv(invWavelengthId, 1, array, 0)

        atmosphereRadiusId = GLES20.glGetUniformLocation(programId, "atmosphereRadius")
        GLES20.glUniform1f(atmosphereRadiusId, 0f)

        this.atmosphereRadius2Id = GLES20.glGetUniformLocation(programId, "atmosphereRadius2")
        GLES20.glUniform1f(this.atmosphereRadius2Id, 0f)

        globeRadiusId = GLES20.glGetUniformLocation(programId, "globeRadius")
        GLES20.glUniform1f(globeRadiusId, 0f)

        KrESunId = GLES20.glGetUniformLocation(programId, "KrESun")
        GLES20.glUniform1f(KrESunId, (Kr * ESun).toFloat())

        KmESunId = GLES20.glGetUniformLocation(programId, "KmESun")
        GLES20.glUniform1f(KmESunId, (Km * ESun).toFloat())

        Kr4PIId = GLES20.glGetUniformLocation(programId, "Kr4PI")
        GLES20.glUniform1f(Kr4PIId, (Kr * 4 * Math.PI).toFloat())

        Km4PIId = GLES20.glGetUniformLocation(programId, "Km4PI")
        GLES20.glUniform1f(Km4PIId, (Km * 4 * Math.PI).toFloat())

        scaleId = GLES20.glGetUniformLocation(programId, "scale")
        GLES20.glUniform1f(scaleId, (1 / altitude).toFloat())

        scaleDepthId = GLES20.glGetUniformLocation(programId, "scaleDepth")
        GLES20.glUniform1f(scaleDepthId, rayleighScaleDepth.toFloat())

        scaleOverScaleDepthId = GLES20.glGetUniformLocation(programId, "scaleOverScaleDepth")
        GLES20.glUniform1f(scaleOverScaleDepthId, (1 / altitude / rayleighScaleDepth).toFloat())

        gId = GLES20.glGetUniformLocation(programId, "g")
        GLES20.glUniform1f(gId, g.toFloat())

        g2Id = GLES20.glGetUniformLocation(programId, "g2")
        GLES20.glUniform1f(g2Id, (g * g).toFloat())

        exposureId = GLES20.glGetUniformLocation(programId, "exposure")
        GLES20.glUniform1f(exposureId, exposure.toFloat())

    }

    open fun loadFragMode(@FragMode fragMode: Int) {
        GLES20.glUniform1i(fragModeId, fragMode)
    }

    open fun loadVertexOrigin(x: Double, y: Double, z: Double) {
        GLES20.glUniform3f(vertexOriginId, x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun loadModelviewProjection(matrix: Matrix4) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixId, 1, false, array, 0)
    }

    open fun loadTexCoordMatrix(matrix: Matrix3) {
        matrix.transposeToArray(array, 0)
        GLES20.glUniformMatrix3fv(texCoordMatrixId, 1, false, array, 0)
    }

    open fun loadVertexOrigin(origin: Vec3) {
        origin.toArray(array, 0)
        GLES20.glUniform3fv(vertexOriginId, 1, array, 0)
    }

    open fun loadLightDirection(direction: Vec3) {
        direction.toArray(array, 0)
        GLES20.glUniform3fv(lightDirectionId, 1, array, 0)
    }

    open fun loadEyePoint(eyePoint: Vec3) {
        eyePoint.toArray(array, 0)
        GLES20.glUniform3fv(eyePointId, 1, array, 0)
        GLES20.glUniform1f(eyeMagnitudeId, eyePoint.magnitude().toFloat())
        GLES20.glUniform1f(eyeMagnitude2Id, eyePoint.magnitudeSquared().toFloat())
    }

    open fun loadGlobeRadius(equatorialRadius: Double) {
        val gr: Double = equatorialRadius
        val ar = gr + altitude
        GLES20.glUniform1f(globeRadiusId, gr.toFloat())
        GLES20.glUniform1f(atmosphereRadiusId, ar.toFloat())
        GLES20.glUniform1f(atmosphereRadius2Id, (ar * ar).toFloat())
    }

}

