package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.AtmosphereProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.render.SkyProgram
import com.atom.wyz.worldwind.util.pool.Pool

class DrawableSkyAtmosphere : Drawable {
    companion object {
        fun obtain(pool: Pool<DrawableSkyAtmosphere>): DrawableSkyAtmosphere {
            return pool.acquire()?.setPool(pool)
                ?: DrawableSkyAtmosphere().setPool(pool) // get an instance from the pool
        }
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

        if (vertexPoints == null || triStripElements == null) {
            return  // vertex buffer or element buffer unspecified
        }

        program.loadGlobeRadius(this.globeRadius) // TODO the Globe is rendering state

        program.loadEyePoint(dc.eyePoint)

        program.loadLightDirection(lightDirection)

        program.loadVertexOrigin(0.0, 0.0, 0.0)

        program.loadModelviewProjection(dc.modelviewProjection)

        program.loadFragMode(AtmosphereProgram.FRAGMODE_SKY)


        // Use the sky's vertex point attribute.
        vertexPoints!!.bindBuffer(dc)
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, 0)

        // Draw the inside of the sky without writing to the depth buffer.
        triStripElements!!.bindBuffer(dc)
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