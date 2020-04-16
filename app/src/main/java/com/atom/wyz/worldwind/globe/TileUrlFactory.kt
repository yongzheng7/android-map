package com.atom.wyz.worldwind.globe


interface TileUrlFactory {
    fun urlForTile(tile: Tile?, imageFormat: String?): String?
}