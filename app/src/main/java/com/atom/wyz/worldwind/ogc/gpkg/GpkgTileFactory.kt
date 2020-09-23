package com.atom.wyz.worldwind.ogc.gpkg

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.core.tile.Tile
import com.atom.wyz.worldwind.core.tile.TileFactory
import com.atom.wyz.worldwind.layer.render.ImageSource
import com.atom.wyz.worldwind.core.tile.ImageTile
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.Logger

class GpkgTileFactory : TileFactory {

    protected var tiles: GpkgContent

    constructor(tiles: GpkgContent) {
        this.tiles = tiles
    }

    override fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile {
        val tile =
            ImageTile(
                sector,
                level,
                row,
                column
            )
        val tableName = tiles.tableName
        val zoomLevel = level.levelNumber

        // Attempt to find the GeoPackage tile matrix associated with the World Wind level. Assumes that the World Wind
        // levels match the GeoPackage tile matrix zoom levels. If there's no match then the GeoPackage contains no
        // tiles for this level and this tile has no image source.
        val geoPackage: GeoPackage = tiles.container ?:let {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "GeoPackage is null")
            )
        }
        val tileMatrix = geoPackage.getTileMatrix(tableName ?:let{
                throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "tableName is null")
                )
            }) ?.get(zoomLevel)

        val tileUserMetrics = geoPackage.getTileUserMetrics(tableName) ?: return tile
        if (tileMatrix != null && tileUserMetrics.hasZoomLevel(zoomLevel)) {
            // Convert the World Wind tile address to the equivalent GeoPackage tile address. Assumes that the World
            // Wind level set matchs the GeoPackage tile matrix set, with the exception of tile rows which are inverted.
            val gpkgRow: Int = tileMatrix.matrixHeight - row - 1
            // Configure the tile with a bitmap factory that reads directly from the GeoPackage.
            val bitmapFactory: ImageSource.BitmapFactory =
                GpkgBitmapFactory(tiles, zoomLevel, column, gpkgRow)
            tile.imageSource = (ImageSource.fromBitmapFactory(bitmapFactory))
        }
        return tile
    }
}