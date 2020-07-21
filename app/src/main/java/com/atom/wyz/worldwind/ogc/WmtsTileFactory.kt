package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.ImageTile
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.Logger

class WmtsTileFactory : TileFactory {
    companion object{
        var TILEMATRIX_TEMPLATE = "{TileMatrix}"

        var TILEROW_TEMPLATE = "{TileRow}"

        var TILECOL_TEMPLATE = "{TileCol}"
    }

     var template: String

     var tileMatrixIdentifiers: MutableList<String>

    constructor(
        template: String,
        tileMatrixIdentifiers: MutableList<String>
    ) {
        this.template = template
        this.tileMatrixIdentifiers = tileMatrixIdentifiers
    }

    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        val tile = ImageTile(sector, level, row, column)

        val urlString= this.urlForTile(level!!.levelNumber, row, column)
        if (urlString != null) {
            tile.imageSource = (ImageSource.fromUrl(urlString))
        }
        return tile
    }

    fun urlForTile(level: Int, row: Int, column: Int): String? {
        if (level >= tileMatrixIdentifiers.size) {
            Logger.logMessage(
                Logger.WARN,
                "WmtsTileFactory",
                "urlForTile",
                "invalid level for tileMatrixIdentifiers: $level"
            )
            return null
        }

        // flip the row index
        val rowHeight = 2 shl level
        val flipRow = rowHeight - row - 1
        var url = template.replace(
            TILEMATRIX_TEMPLATE,
            tileMatrixIdentifiers[level]
        )
        url = url.replace(TILEROW_TEMPLATE, flipRow.toString() + "")
        url = url.replace(TILECOL_TEMPLATE, column.toString() + "")
        return url
    }
}