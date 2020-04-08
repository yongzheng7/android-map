package com.atom.wyz.worldwind.layer

import android.opengl.GLES20
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.render.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class AtmosphereAndGroundLayer : AbstractLayer {

    companion object {
        protected fun assembleTriStripIndices(numLat: Int, numLon: Int): ShortBuffer? { // Allocate a buffer to hold the indices.
            val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
            val result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
            val index = ShortArray(2)
            var vertex = 0
            for (latIndex in 0 until numLat - 1) { // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
                // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
                // a counterclockwise winding order.
                for (lonIndex in 0 until numLon) {
                    vertex = lonIndex + latIndex * numLon
                    index[0] = (vertex + numLon).toShort()
                    index[1] = vertex.toShort()
                    result.put(index)
                }
                // Insert indices to create 2 degenerate triangles:
                // - one for the end of the current row, and
                // - one for the beginning of the next row
                if (latIndex < numLat - 2) {
                    index[0] = vertex.toShort()
                    index[1] = ((latIndex + 2) * numLon).toShort()
                    result.put(index)
                }
            }
            return result.rewind() as ShortBuffer
        }
    }

    protected var nightImageSource: ImageSource? = null

    protected var mvpMatrix: Matrix4 = Matrix4()

    var lightLocation: Location? = null

    protected var texCoordMatrix: Matrix3 = Matrix3()

    protected var vector: Vec3 = Vec3()

    protected var fullSphereSector: Sector = Sector().setFullSphere()

    protected var skyWidth = 128

    protected var skyHeight = 128

    protected var skyPoints: FloatBuffer? = null

    protected var skyTriStrip: ShortBuffer? = null

    constructor() : super("Atmosphere") {
        nightImageSource = ImageSource.fromResource(R.drawable.gov_nasa_worldwind_night)
    }

    override fun doRender(dc: DrawContext) {
        drawSky(dc)
        drawGround(dc)
    }

    protected fun drawGround(dc: DrawContext) {

        val terrain = dc.terrain ?: return
        if (terrain.getTileCount() == 0) return

        var program = dc.getProgram(GroundProgram.KEY) as AtmosphereAndGroundProgram?
        if (program == null) {
            program = dc.putProgram(GroundProgram.KEY, dc.resources?.let { GroundProgram(it) }
                    ?: return) as AtmosphereAndGroundProgram
        }
        if (!program.useProgram(dc)) return

        program.loadGlobe(dc.globe!!)

        program.loadEyePoint(dc.eyePoint)

        val loc: Location = if (lightLocation != null) lightLocation!! else dc.eyePosition
        dc.globe?.geographicToCartesianNormal(loc.latitude, loc.longitude, vector)
        program.loadLightDirection(vector)

        var texture: GpuTexture? = null
        var textureBound = false


        if (this.nightImageSource != null && this.lightLocation != null) {

            texture = dc.getTexture(nightImageSource!!)
            if (texture == null) {
                texture = dc.retrieveTexture(nightImageSource)
            }
            textureBound = texture?.bindTexture(dc) ?: false
        }
        // Get the draw context's tessellated terrain and modelview projection matrix.
        val modelviewProjection: Matrix4 = dc.modelviewProjection

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1)
        terrain.useVertexTexCoordAttrib(dc, 1)

        for (idx in 0 until terrain.getTileCount()) {

            val terrainOrigin: Vec3 = terrain.getTileVertexOrigin(idx) ?: continue
            program.loadVertexOrigin(terrainOrigin)

            mvpMatrix.set(modelviewProjection)
            mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z)
            program.loadModelviewProjection(mvpMatrix)

            if (textureBound) {
                texCoordMatrix.setToIdentity()
                texture!!.applyTexCoordTransform(texCoordMatrix)
                terrain.applyTexCoordTransform(idx, fullSphereSector, texCoordMatrix)
                program.loadTexCoordMatrix(texCoordMatrix)
            }

            terrain.useVertexPointAttrib(dc, idx, 0)

            program.loadFragMode(AtmosphereAndGroundProgram.FRAGMODE_GROUND_SECONDARY)
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO)
            terrain.drawTileTriangles(dc, idx)

            program.loadFragMode(if (textureBound) AtmosphereAndGroundProgram.FRAGMODE_GROUND_PRIMARY_TEX_BLEND else AtmosphereAndGroundProgram.FRAGMODE_GROUND_PRIMARY)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
            terrain.drawTileTriangles(dc, idx)
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDisableVertexAttribArray(1)

    }

    protected fun drawSky(dc: DrawContext) {

        var program = dc.getProgram(SkyProgram.KEY) as AtmosphereAndGroundProgram?

        if (program == null) {
            program = dc.putProgram(SkyProgram.KEY, dc.resources?.let { SkyProgram(it) }
                    ?: return) as AtmosphereAndGroundProgram
        }
        if (!program.useProgram(dc)) return


        program.loadGlobe(dc.globe!!)

        // Use the draw context's eye point.
        program.loadEyePoint(dc.eyePoint)

        // Use the vertex origin for the sky ellipsoid.
        program.loadVertexOrigin(vector.set(0.0, 0.0, 0.0))

        program.loadModelviewProjection(dc.modelviewProjection)

        program.loadFragMode(AtmosphereAndGroundProgram.FRAGMODE_SKY)

        // Use this layer's light direction.
        // TODO Make light/sun direction an optional property of the WorldWindow and attach it to the DrawContext each frame
        // TODO DrawContext property defaults to the eye lat/lon like we have below
        val loc: Location = if (lightLocation != null) lightLocation!! else dc.eyePosition

        dc.globe?.geographicToCartesianNormal(loc.latitude, loc.longitude, vector)

        program.loadLightDirection(vector)

        useSkyVertexPointAttrib(dc, program.altitude, 0)

        GLES20.glDepthMask(false)
        GLES20.glFrontFace(GLES20.GL_CW)

        drawSkyTriangles(dc)

        GLES20.glDepthMask(true)
        GLES20.glFrontFace(GLES20.GL_CCW)
    }

    protected fun useSkyVertexPointAttrib(dc: DrawContext, altitude: Double, attribLocation: Int) {
        if (skyPoints == null) {
            val count = skyWidth * skyHeight
            val array = DoubleArray(count)
            Arrays.fill(array, altitude)
            skyPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer()
            dc.globe?.geographicToCartesianGrid(fullSphereSector, skyWidth, skyHeight, array, null,
                    skyPoints, 3)?.rewind()
        }
        GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, skyPoints)
    }

    protected fun drawSkyTriangles(dc: DrawContext) {
        if (skyTriStrip == null) {
            skyTriStrip = assembleTriStripIndices(skyWidth, skyHeight)
        }
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, skyTriStrip!!.remaining(), GLES20.GL_UNSIGNED_SHORT, skyTriStrip)
    }


}