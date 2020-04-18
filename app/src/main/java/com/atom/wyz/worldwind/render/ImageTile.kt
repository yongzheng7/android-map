package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.util.Level

class ImageTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column) {

    var imageSource: ImageSource? = null

}