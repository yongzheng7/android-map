package com.atom.wyz.worldwind.layer

import android.opengl.GLES20
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawableGroundAtmosphere
import com.atom.wyz.worldwind.draw.DrawableSkyAtmosphere
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class AtmosphereLayer : AbstractLayer {

    companion object {
        private val VERTEX_POINTS_KEY = AtmosphereLayer::class.java.name + ".vertexPoints"
        private val TRI_STRIP_ELEMENTS_KEY = AtmosphereLayer::class.java.name + ".triStripElements"
    }

    var nightImageSource: ImageSource? = null

    var nightImageOptions: ImageOptions? = null

    var lightLocation: Location? = null

    var activeLightDirection = Vec3()


    private val fullSphereSector: Sector = Sector().setFullSphere()

    constructor() : super("Atmosphere") {
        this.pickEnabled = false
        nightImageSource = ImageSource.fromResource(R.drawable.gov_nasa_worldwind_night)
        nightImageOptions = ImageOptions(WorldWind.RGB_565)
    }

    override fun doRender(rc: RenderContext) {
        determineLightDirection(rc)
        //renderSky(rc)
        drawGround(rc)
    }

    protected fun determineLightDirection(rc: RenderContext) {
        if (lightLocation != null) {
            rc.globe!!.geographicToCartesianNormal(
                lightLocation!!.latitude,
                lightLocation!!.longitude,
                activeLightDirection
            )
        } else {
            rc.globe!!.geographicToCartesianNormal(
                rc.camera.latitude,
                rc.camera.longitude,
                activeLightDirection
            )
        }
    }

    protected fun drawGround(rc: RenderContext) {
        rc.terrain?.apply {
            if (this.sector.isEmpty()) {
                return  // no terrain surface to render on
            }
        } ?: return

        val drawable = DrawableGroundAtmosphere.obtain(rc.getDrawablePool(DrawableGroundAtmosphere::class.java))

        drawable.program = rc.getProgram(GroundProgram.KEY) as GroundProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(GroundProgram.KEY, GroundProgram(rc.resources!!)) as GroundProgram
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = rc.globe!!.equatorialRadius

        if (nightImageSource != null && lightLocation != null) {
            drawable.nightTexture = rc.getTexture(nightImageSource!!)
            if (drawable.nightTexture == null) {
                drawable.nightTexture = rc.retrieveTexture(nightImageSource, nightImageOptions)
            }
        } else {
            drawable.nightTexture = null
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

    protected fun renderSky(rc: RenderContext) {
        val drawable = DrawableSkyAtmosphere.obtain(rc.getDrawablePool(DrawableSkyAtmosphere::class.java))
        val size = 128
        drawable.program = rc.getProgram(SkyProgram.KEY) as SkyProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(SkyProgram.KEY, SkyProgram(rc.resources!!)) as SkyProgram
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = rc.globe!!.equatorialRadius


        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_KEY)
        if (drawable.vertexPoints == null) {
            drawable.vertexPoints = rc.putBufferObject(
                VERTEX_POINTS_KEY,
                this.assembleVertexPoints(rc, size, size, drawable.program!!.altitude)
            )
        }

        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_KEY)
        if (drawable.triStripElements == null) {
            drawable.triStripElements = rc.putBufferObject(
                TRI_STRIP_ELEMENTS_KEY, assembleTriStripElements(size, size)
            )
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = rc.globe!!.equatorialRadius

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

    protected fun assembleVertexPoints(
        rc: RenderContext,
        numLat: Int,
        numLon: Int,
        altitude: Double
    ): BufferObject {

        val count = numLat * numLon
        val altitudes = DoubleArray(count)
        Arrays.fill(altitudes, altitude)

        val points = FloatArray(count * 3)

        rc.globe!!.geographicToCartesianGrid(fullSphereSector, numLat, numLon, altitudes, null, points, 3, 0)

        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()

        return BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer)
    }

    protected fun assembleTriStripElements(
        numLat: Int,
        numLon: Int
    ): BufferObject { // Allocate a buffer to hold the indices.
        val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
        val elements = ShortArray(count)
        var pos = 0
        var vertex = 0

        for (latIndex in 0 until numLat - 1) { // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (lonIndex in 0 until numLon) {
                vertex = lonIndex + latIndex * numLon
                elements[pos++] = (vertex + numLon).toShort()
                elements[pos++] = vertex.toShort()
            }
            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                elements[pos++] = vertex.toShort()
                elements[pos++] = ((latIndex + 2) * numLon).toShort()
            }
        }
        val size = elements.size * 2
        val buffer =
            ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
        buffer.put(elements).rewind()

        return BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer)
    }
}