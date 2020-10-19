package com.atom.map.core.tile

import com.atom.map.geom.Sector
import com.atom.map.layer.render.ImageSource
import com.atom.map.util.Level

class ImageTile(sector: Sector, level: Level, row: Int, column: Int) : Tile(sector, level, row, column) {

    var imageSource: ImageSource? = null
    override fun toString(): String {
        return "ImageTile(imageSource=$imageSource)"
    }
}