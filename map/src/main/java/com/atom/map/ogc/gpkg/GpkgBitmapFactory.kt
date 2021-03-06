package com.atom.map.ogc.gpkg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.atom.map.renderable.ImageSource
import com.atom.map.util.Logger

class GpkgBitmapFactory : ImageSource.BitmapFactory {

    protected var tiles: GpkgContent

    protected var zoomLevel = 0

    protected var tileColumn = 0

    protected var tileRow = 0

    constructor(
        tiles: GpkgContent,
        zoomLevel: Int,
        tileColumn: Int,
        tileRow: Int
    ) {
        this.tiles = tiles
        this.zoomLevel = zoomLevel
        this.tileColumn = tileColumn
        this.tileRow = tileRow
    }

    override fun createBitmap(): Bitmap? {
        // Attempt to read the GeoPackage tile user data, throwing an exception if it cannot be found.
        val geoPackage = tiles.container ?:return null
        val tileUserData= geoPackage.readTileUserData(tiles, zoomLevel, tileColumn, tileRow)

        // Throw an exception if the tile user data cannot be found, but let the caller (likely an ImageFactory)
        // determine whether to log a message.
        if (tileUserData == null) {
            Logger.logMessage(
                Logger.WARN, "GpkgBitmapFactory", "createBitmap",
                "The GeoPackage tile cannot be found (zoomLevel=" + zoomLevel + ", tileColumn=" + tileColumn + ", tileRow=" + tileRow + ")"
            )
            return null
        }

        // Decode the tile user data, either a PNG image or a JPEG image.
        val data = tileUserData.tileData ?: return null
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}