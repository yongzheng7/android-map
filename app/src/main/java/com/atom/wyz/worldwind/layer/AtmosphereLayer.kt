package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.draw.DrawableGroundAtmosphere
import com.atom.wyz.worldwind.draw.DrawableSkyAtmosphere
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.GroundProgram
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.SkyProgram

class AtmosphereLayer : AbstractLayer {

    protected var nightImageSource: ImageSource? = null

    public var lightLocation: Location? = null

    protected var activeLightDirection = Vec3()

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
            if (this.getTileCount() == 0) {
                return  // no terrain surface to render on
            }
        } ?: return
        var program = dc.getProgram(GroundProgram.KEY) as GroundProgram?
        if (program == null) {
            program = dc.putProgram(GroundProgram.KEY, GroundProgram(dc.resources!!)) as GroundProgram
        }

        var nightTexture: GpuTexture? = null
        if (nightImageSource != null && lightLocation != null) {
            nightTexture = dc.getTexture(nightImageSource!!)
            if (nightTexture == null) {
                nightTexture = dc.retrieveTexture(nightImageSource)
            }
        }
        val drawable= DrawableGroundAtmosphere.obtain(dc.getDrawablePool(DrawableGroundAtmosphere::class.java)).set(program, activeLightDirection, nightTexture)
        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

    protected fun drawSky(dc: DrawContext) {
        var program = dc.getProgram(SkyProgram.KEY) as SkyProgram?
        if (program == null) {
            program = dc.putProgram(SkyProgram.KEY, SkyProgram(dc.resources!!)) as SkyProgram
        }
        val drawable = DrawableSkyAtmosphere.obtain(dc.getDrawablePool(DrawableSkyAtmosphere::class.java)).set(program, activeLightDirection)
        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY)
    }

}