package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.globe.TileUrlFactory
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.ImageTile
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LruMemoryCache
import java.util.*

open class TiledImageLayer : AbstractLayer, TileFactory {


    var levelSet: LevelSet = LevelSet()
        set(value) {
            field = value
            this.invalidateTiles()
        }
    var tileUrlFactory: TileUrlFactory? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }
    /**
     * 图片格式
     */
    var imageFormat: String? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }
    var detailControl = 4.0

    protected var tileCache: LruMemoryCache<String?, Array<Tile?>?>? = LruMemoryCache(600)

    protected var topLevelTiles = ArrayList<Tile>()

    protected var currentTiles = ArrayList<ImageTile>()

    protected var currentFallbackTile: ImageTile? = null

    constructor(displayName: String = "Tiled Image Layer") : super(displayName) {
        this.pickEnabled = false
    }

    override fun doRender(dc: DrawContext) {
        if (dc.terrain?.getTileCount() ?: return == 0) {
            return
        }

        this.assembleTiles(dc)

        if (currentTiles.size > 0) {
            dc.surfaceTileRenderer?.renderTiles(dc, currentTiles)
        }

        currentTiles.clear() // clear the tile list should there be no more calls to render
        currentFallbackTile = null
    }

    protected open fun fetchTileTexture(dc: DrawContext, tile: ImageTile): GpuTexture? {
        var texture: GpuTexture? = dc.getTexture(tile.imageSource!!)
        if (texture == null) {
            texture = dc.retrieveTexture(tile.imageSource!!)
        }
        return texture
    }

    protected fun assembleTiles(dc: DrawContext) {
        currentTiles.clear()

        if (topLevelTiles.isEmpty()) {
            this.createTopLevelTiles()
        }
        for (tile in topLevelTiles) {
            this.addTileOrDescendants(dc, tile as ImageTile)
        }
    }

    protected fun createTopLevelTiles() {
        val firstLevel: Level = levelSet.firstLevel() ?: return
        Tile.assembleTilesForLevel(firstLevel, this, topLevelTiles)
    }

    protected fun addTileOrDescendants(dc: DrawContext, tile: ImageTile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(dc, dc.frustum)) {
            return  // ignore the tile and its descendants if it's not visible
        }
        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, detailControl)) {
            addTile(dc, tile)
            return  // use the tile if it does not need to be subdivided
        }

        var fallbackTile: ImageTile? = currentFallbackTile

        if (this.fetchTileTexture(dc, tile) != null) { // use it as a fallback tile for descendants
            currentFallbackTile = tile
        }

        for (child in tile.subdivideToCache(this, tileCache, 4)!!) { // each tile has a cached size of 1
            addTileOrDescendants(dc, child as ImageTile) // recursively process the tile's children
        }

        currentFallbackTile = fallbackTile
    }

    protected fun addTile(dc: DrawContext, tile: ImageTile) {
        if (fetchTileTexture(dc, tile) != null) { // use the tile's own texture
            currentTiles.add(tile)
        } else if (currentFallbackTile != null) { // use the fallback tile's texture
            tile.fallbackTile = currentFallbackTile
            currentTiles.add(tile)
        }
    }

    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        val tile: ImageTile = ImageTile(sector, level, row, column)

        if (tileUrlFactory != null && this.imageFormat != null) {
            tile.imageSource = ImageSource.fromUrl(tileUrlFactory!!.urlForTile(tile, imageFormat))
        }

        return tile
    }

    protected open fun clearTiles() {
        for (tile in currentTiles) {
            tile.fallbackTile = (null) // avoid memory leaks due to fallback tile references
        }
        currentTiles.clear() // clear the tile list
        currentFallbackTile = null // clear the fallback tile
    }

    protected fun invalidateTiles() {
        topLevelTiles.clear()
        currentTiles.clear()
        tileCache!!.clear()
    }
}