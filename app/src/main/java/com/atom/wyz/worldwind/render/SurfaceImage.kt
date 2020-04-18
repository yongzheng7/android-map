package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.RenderContext
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

    override fun doRender(rc: RenderContext) {
        if (sector.isEmpty()) {
            return
        }
        if (rc.terrain == null || !rc.terrain!!.sector.intersects(sector)) {
            return  // nothing to render on
        }
        var texture: GpuTexture? = rc.getTexture(imageSource!!)
        if (texture == null) {
            texture = rc.retrieveTexture(imageSource)
        }
        if (texture == null) {
            return  // no texture to draw
        }
        val program = this.getShaderProgram(rc)
        val drawable = DrawableSurfaceTexture.obtain(rc.getDrawablePool(DrawableSurfaceTexture::class.java)).set(program, sector, texture , texture.texCoordTransform)
        rc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
    }

    protected fun getShaderProgram(dc: RenderContext): SurfaceTextureProgram? {
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