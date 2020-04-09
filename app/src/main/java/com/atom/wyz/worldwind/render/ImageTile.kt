package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.util.Level

class ImageTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column), SurfaceTile {

    var imageSource: ImageSource? = null

    var fallbackTile: ImageTile? = null

    override fun bindTexture(dc: DrawContext): Boolean {
        var texture = dc.getTexture(imageSource!!)
        if (texture != null && texture.bindTexture(dc)) {
            return true;
        }
        return if (fallbackTile != null && fallbackTile!!.bindTexture(dc)) {
            true
        } else false
    }

    override fun applyTexCoordTransform(dc: DrawContext, result: Matrix3): Boolean {
        val texture: GpuTexture? = dc.getTexture(imageSource!!)
        if (texture != null) {
            result.multiplyByMatrix(texture.texCoordTransform)
            return true
        }

        if (fallbackTile != null && fallbackTile!!.applyTexCoordTransform(dc, result)) {
            result.multiplyByTileTransform(sector, fallbackTile!!.sector)
            return true
        }

        return false
    }

}