package com.atom.wyz.worldwind.draw

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.AtmosphereProgram
import com.atom.wyz.worldwind.render.SkyProgram
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.SynchronizedPool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class DrawableSkyAtmosphere : Drawable {
    companion object {
        fun obtain(pool: Pool<DrawableSkyAtmosphere>): DrawableSkyAtmosphere {
            return pool.acquire() ?.setPool(pool) ?: DrawableSkyAtmosphere().setPool(pool) // get an instance from the pool
        }

        protected fun assembleTriStripIndices(
            numLat: Int,
            numLon: Int
        ): ShortBuffer {
            val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
            val result =
                ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
            val index = ShortArray(2)
            var vertex = 0
            for (latIndex in 0 until numLat - 1) {
                for (lonIndex in 0 until numLon) {
                    vertex = lonIndex + latIndex * numLon
                    index[0] = (vertex + numLon).toShort()
                    index[1] = vertex.toShort()
                    result.put(index)
                }
                if (latIndex < numLat - 2) {
                    index[0] = vertex.toShort()
                    index[1] = ((latIndex + 2) * numLon).toShort()
                    result.put(index)
                }
            }
            return result.rewind() as ShortBuffer
        }
    }

    protected var program: SkyProgram? = null
    protected var lightDirection: Vec3 = Vec3()
    protected var skyWidth = 128
    protected var skyHeight = 128
    protected var skyPoints: FloatBuffer? = null
    protected var skyTriStrip: ShortBuffer? = null
    protected var fullSphereSector: Sector = Sector().setFullSphere()
    private var pool: Pool<DrawableSkyAtmosphere>? = null

    private fun setPool(pool: Pool<DrawableSkyAtmosphere>): DrawableSkyAtmosphere {
        this.pool = pool
        return this
    }
    public fun set(program: SkyProgram, lightDirection: Vec3?) :DrawableSkyAtmosphere {
        this.program = program
        if (lightDirection != null) {
            this.lightDirection.set(lightDirection)
        } else {
            this.lightDirection.set(0.0, 0.0, 1.0)
        }
        return this
    }

    override fun draw(dc: DrawContext) {
        val program = this.program ?: return

        if (!program.useProgram(dc)) {
            return
        }
        program.loadGlobeRadius(dc.globe!!.equatorialRadius) // TODO the Globe is rendering state

        program.loadEyePoint(dc.eyePoint)

        program.loadLightDirection(lightDirection)

        program.loadVertexOrigin(0.0, 0.0, 0.0)

        program.loadModelviewProjection(dc.modelviewProjection)

        program.loadFragMode(AtmosphereProgram.FRAGMODE_SKY)

        if (skyPoints == null) {
            val count = skyWidth * skyHeight
            val array = DoubleArray(count)
            Arrays.fill(array, program.altitude)
            skyPoints =
                ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer()
            dc.globe!!.geographicToCartesianGrid(
                fullSphereSector, skyWidth, skyHeight, array, null,
                skyPoints, 3
            ).rewind()
        }

        if (skyTriStrip == null) {
            skyTriStrip = DrawableSkyAtmosphere.assembleTriStripIndices(skyWidth, skyHeight)
        }

        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, skyPoints)

        GLES20.glDepthMask(false)
        GLES20.glFrontFace(GLES20.GL_CW)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP,
            skyTriStrip!!.remaining(),
            GLES20.GL_UNSIGNED_SHORT,
            skyTriStrip
        )

        GLES20.glDepthMask(true)
        GLES20.glFrontFace(GLES20.GL_CCW)
    }

    override fun recycle() {
        program = null
        pool?.release(this)
        pool = null
    }
}