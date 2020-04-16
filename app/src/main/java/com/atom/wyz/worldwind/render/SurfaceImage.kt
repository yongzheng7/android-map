package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableSurfaceTexture
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.Logger

class SurfaceImage : AbstractRenderable {
    var sector: Sector = Sector()
        set(value) {
            field.set(value)
        }
    var imageSource: ImageSource? = null

    constructor() : super("Surface Image")

    constructor(sector: Sector?, imageSource: ImageSource) : super("Surface Image") {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector")
            )
        }
        this.sector.set(sector)
        this.imageSource = imageSource
    }

    override fun doRender(dc: DrawContext) {
        if (sector.isEmpty()) {
            return
        }
        if (dc.terrain == null || !dc.terrain!!.sector.intersects(sector)) {
            return  // nothing to render on
        }
        var texture: GpuTexture? = dc.getTexture(imageSource!!)
        if (texture == null) {
            texture = dc.retrieveTexture(imageSource)
        }
        if (texture == null) {
            return  // no texture to draw
        }
        val program = this.getShaderProgram(dc)
        val drawable = DrawableSurfaceTexture.obtain(dc.getDrawablePool(DrawableSurfaceTexture::class.java)).set(program, sector, texture , texture.texCoordTransform)
        dc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
    }

    protected fun getShaderProgram(dc: DrawContext): SurfaceTextureProgram? {
        var program: SurfaceTextureProgram? = dc.getProgram(SurfaceTextureProgram.KEY) as SurfaceTextureProgram?
        if (program == null) {
            program = dc.putProgram(
                SurfaceTextureProgram.KEY,
                SurfaceTextureProgram(dc.resources!!)
            ) as SurfaceTextureProgram?
        }
        return program
    }

}