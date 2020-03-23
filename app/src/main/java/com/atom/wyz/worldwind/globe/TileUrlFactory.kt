package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.globe.Tile

interface TileUrlFactory {
    fun urlForTile(tile: Tile?, imageFormat: String?): String?
}