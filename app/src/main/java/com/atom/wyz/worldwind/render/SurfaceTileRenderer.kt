package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext

interface SurfaceTileRenderer {
    fun renderTile(dc: DrawContext, surfaceTile: SurfaceTile?)

    fun renderTiles(dc: DrawContext, surfaceTiles: Iterable<SurfaceTile?>?)
}