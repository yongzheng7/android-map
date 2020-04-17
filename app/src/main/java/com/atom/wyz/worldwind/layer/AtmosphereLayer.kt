package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
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
        nightImageSource = ImageSource.fromResource(R.drawable.gov_nasa_worldwind_night)
    }

    override fun doRender(dc: DrawContext) {
        determineLightDirection(dc)
        drawSky(dc)
        drawGround(dc)
    }

    protected fun determineLightDirection(dc: DrawContext) {
        if (lightLocation != null) {
            dc.globe!!.geographicToCartesianNormal(
                lightLocation!!.latitude,
                lightLocation!!.longitude,
                activeLightDirection
            )
        } else {
            dc.globe!!.geographicToCartesianNormal(
                dc.eyePosition.latitude,
                dc.eyePosition.longitude,
                activeLightDirection
            )
        }
    }

    protected fun drawGround(dc: DrawContext) {
        val terrain = dc.terrain?.apply {
            if (this.sector.isEmpty()) {
                return  // no terrain surface to render on
            }
        } ?: return

        val drawable = DrawableGroundAtmosphere.obtain(dc.getDrawablePool(DrawableGroundAtmosphere::class.java))

        drawable.program = dc.getProgram(GroundProgram.KEY) as GroundProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(GroundProgram.KEY, GroundProgram(dc.resources!!)) as GroundProgram
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = dc.globe!!.equatorialRadius

        if (nightImageSource != null && lightLocation != null) {
            drawable.nightTexture = dc.getTexture(nightImageSource!!)
            if (drawable.nightTexture == null) {
                drawable.nightTexture = dc.retrieveTexture(nightImageSource)
            }
        } else {
            drawable.nightTexture = null
        }

        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

    protected fun drawSky(dc: DrawContext) {
        val drawable = DrawableSkyAtmosphere.obtain(dc.getDrawablePool(DrawableSkyAtmosphere::class.java))

        drawable.program = dc.getProgram(SkyProgram.KEY) as SkyProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(SkyProgram.KEY, SkyProgram(dc.resources!!)) as SkyProgram
        }
        drawable.lightDirection.set(activeLightDirection)
        drawable.globeRadius = dc.globe!!.equatorialRadius

        val size = 128
        if (drawable.vertexPoints == null) {
            val count = size * size
            val array = DoubleArray(count)
            drawable.program?.let {
                Arrays.fill(array, it.altitude)
            }
            drawable.vertexPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer()
            dc.globe!!.geographicToCartesianGrid(
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

        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }
}