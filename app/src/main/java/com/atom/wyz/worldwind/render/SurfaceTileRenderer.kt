package com.atom.wyz.worldwind.render

interface SurfaceTileRenderer {
    fun renderTile(dc: DrawContext, surfaceTile: SurfaceTile?)

    fun renderTiles(dc: DrawContext, surfaceTiles: Iterable<SurfaceTile?>?)
}