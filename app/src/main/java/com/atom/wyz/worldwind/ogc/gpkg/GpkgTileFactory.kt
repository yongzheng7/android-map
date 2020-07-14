package com.atom.wyz.worldwind.ogc.gpkg

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.ImageTile
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.Logger

class GpkgTileFactory : TileFactory {

    protected var tiles: GpkgContents

    constructor(tiles: GpkgContents) {
        this.tiles = tiles
    }

    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        if (sector == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "missingSector")
            )
        }

        if (level == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "missingLevel")
            )
        }

        val tile = ImageTile(sector, level, row, column)
        // Attempt to find the GeoPackage tile matrix associated with the World Wind level. Assumes that the World Wind
        // levels match the GeoPackage tile matrix zoom levels. If there's no match then the GeoPackage contains no
        // tiles for this level and this tile has no image source.
        val geoPackage: GeoPackage = tiles.container ?:let {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "GeoPackage is null")
            )
        }
        val tileMatrices = geoPackage.getTileMatrices(tiles.tableName ?:let{
                throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "tableName is null")
                )
            }) ?:let{
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "tileMatrices is null")
            )
        }
        val tileMatrix = tileMatrices[level.levelNumber]
        if (tileMatrix != null) {
            // Convert the World Wind tile address to the equivalent GeoPackage tile address. Assumes that the World Wind
            // level set matchs the GeoPackage tile matrix set, with the exception of tile rows which are inverted.
            val zoomLevel = level.levelNumber
            val tileRow: Int = tileMatrix.matrixHeight - row - 1
            // Configure the tile with a bitmap factory that reads directly from the GeoPackage.
            val bitmapFactory: ImageSource.BitmapFactory = GpkgBitmapFactory(tiles, zoomLevel, column, tileRow)
            tile.imageSource = (ImageSource.fromBitmapFactory(bitmapFactory))
        }

        return tile
    }
}