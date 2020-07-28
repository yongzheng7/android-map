package com.atom.wyz.worldwind.tile

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.util.Level

class ImageTile(sector: Sector, level: Level, row: Int, column: Int) : Tile(sector, level, row, column) {

    var imageSource: ImageSource? = null
    override fun toString(): String {
        return "ImageTile(imageSource=$imageSource)"
    }
}