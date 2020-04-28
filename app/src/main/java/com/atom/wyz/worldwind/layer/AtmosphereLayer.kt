package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.DrawableGroundAtmosphere
import com.atom.wyz.worldwind.draw.DrawableSkyAtmosphere
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.GroundProgram
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.SkyProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.*

class AtmosphereLayer : AbstractLayer {

    companion object {
        protected fun assembleTriStripElements(
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

    protected var nightImageSource: ImageSource? = null

    public var lightLocation: Location? = null

    protected var activeLightDirection = Vec3()


    private val fullSphereSector: Sector = Sector().setFullSphere()

    constructor() : super("Atmosphere") {
        this.pickEnabled = false
        nightImageSource = ImageSource.fromResource(R.drawable.gov_nasa_worldwind_night)
    }

    override fun doRender(rc: RenderContext) {
        determineLightDirection(rc)
        drawSky(rc)
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
        val terrain = rc.terrain?.apply {
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
                drawable.nightTexture = rc.retrieveTexture(nightImageSource)
            }
        } else {
            drawable.nightTexture = null
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

    protected fun drawSky(rc: RenderContext) {
        val drawable = DrawableSkyAtmosphere.obtain(rc.getDrawablePool(DrawableSkyAtmosphere::class.java))

        drawable.program = rc.getProgram(SkyProgram.KEY) as SkyProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(SkyProgram.KEY, SkyProgram(rc.resources!!)) as SkyProgram
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = rc.globe!!.equatorialRadius

        val size = 128
        if (drawable.vertexPoints == null) {
            val count = size * size
            val array = DoubleArray(count)
            drawable.program?.let {
                Arrays.fill(array, it.altitude)
            }
            drawable.vertexPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer()
            rc.globe!!.geographicToCartesianGrid(
                fullSphereSector,
                size,
                size,
                array,
                null,
                drawable.vertexPoints,
                3
            ).rewind()
        }

        if (drawable.triStripElements == null) {
            drawable.triStripElements = AtmosphereLayer.assembleTriStripElements(size, size)
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }
}