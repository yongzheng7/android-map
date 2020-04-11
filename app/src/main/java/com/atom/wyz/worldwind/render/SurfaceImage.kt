package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.Logger

class SurfaceImage : AbstractRenderable, SurfaceTile {
    override var sector: Sector = Sector()
        set(value) {
            field.set(value)
        }
    var imageSource: ImageSource? = null

    constructor() : super("Surface Image")

    constructor(sector: Sector?, imageSource: ImageSource) : super("Surface Image") {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"))
        }
        this.sector.set(sector)
        this.imageSource = imageSource
    }

    override fun doRender(dc: DrawContext) {
        if (sector.isEmpty() ) {
            return
        }
        if (dc.terrain == null || !dc.terrain!!.sector.intersects(sector)) {
            return  // nothing to render on
        }
        var texture: GpuTexture? = dc.getTexture(imageSource!!)
        if (texture == null) {
            texture = dc.retrieveTexture(imageSource)
        }
        if (texture != null) {
            dc.surfaceTileRenderer!!.renderTile(dc, this)
        }
    }

    override fun bindTexture(dc: DrawContext): Boolean {
        val texture: GpuTexture? = dc.getTexture(imageSource!!)
        return texture != null && texture.bindTexture(dc)
    }

    override fun applyTexCoordTransform(dc: DrawContext, result: Matrix3): Boolean {
        val texture: GpuTexture? = dc.getTexture(imageSource!!)
        if (texture != null && texture.hasTexture()) {
            result.multiplyByMatrix(texture.texCoordTransform)
            return true
        }
        return false
    }

}