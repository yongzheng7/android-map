package com.atom.map.drawable

import android.opengl.GLES20
import com.atom.map.geom.Vec3
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.shader.SkyProgram
import com.atom.map.util.pool.Pool

class DrawableSkyAtmosphere : Drawable {
    companion object {
        fun obtain(pool: Pool<DrawableSkyAtmosphere>): DrawableSkyAtmosphere =
            pool.acquire()?.setPool(pool) ?: DrawableSkyAtmosphere()
                .setPool(pool)
    }

    var program: SkyProgram? = null
    var lightDirection: Vec3 = Vec3()
    var globeRadius = 0.0

    var vertexPoints: BufferObject? = null
    var triStripElements: BufferObject? = null

    private var pool: Pool<DrawableSkyAtmosphere>? = null

    private fun setPool(pool: Pool<DrawableSkyAtmosphere>): DrawableSkyAtmosphere {
        this.pool = pool
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return
        if (!program.useProgram(dc)) {
            return
        }

        if (vertexPoints == null || !vertexPoints!!.bindBuffer(dc)) {
            return  // vertex buffer or element buffer unspecified
        }
        if (triStripElements == null || !this.triStripElements!!.bindBuffer(dc)) {
            return  // vertex buffer or element buffer unspecified
        }

        program.loadGlobeRadius(this.globeRadius) // TODO the Globe is rendering state

        program.loadEyePoint(dc.eyePoint)

        program.loadLightDirection(lightDirection)

        program.loadVertexOrigin(0.0, 0.0, 0.0)

        program.loadModelviewProjection(dc.modelviewProjection)

        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glDepthMask(false)
        GLES20.glFrontFace(GLES20.GL_CW)
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, triStripElements!!.bufferLength, GLES20.GL_UNSIGNED_SHORT, 0)

        GLES20.glDepthMask(true)
        GLES20.glFrontFace(GLES20.GL_CCW)
    }

    override fun recycle() {
        program = null
        vertexPoints = null
        triStripElements = null
        pool?.release(this)
        pool = null
    }
}