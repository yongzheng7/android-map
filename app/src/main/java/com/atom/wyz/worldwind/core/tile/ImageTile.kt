package com.atom.wyz.worldwind.core.tile

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.layer.render.ImageSource
import com.atom.wyz.worldwind.util.Level

class ImageTile(sector: Sector, level: Level, row: Int, column: Int) : Tile(sector, level, row, column) {

    var imageSource: ImageSource? = null
    override fun toString(): String {
        return "ImageTile(imageSource=$imageSource)"
    }
}