package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableSurfaceTexture
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.globe.TileUrlFactory
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.ImageTile
import com.atom.wyz.worldwind.render.SurfaceTextureProgram
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LruMemoryCache
import com.atom.wyz.worldwind.util.pool.Pool
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

    var imageFormat: String? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }

    var detailControl = 4.0

    protected var topLevelTiles = ArrayList<Tile>()

    protected var tileCache: LruMemoryCache<String?, Array<Tile?>?>? = LruMemoryCache(600)

    protected var activeProgram: SurfaceTextureProgram? = null

    protected var ancestorTile: ImageTile? = null

    protected var ancestorTexture: GpuTexture? = null

    protected var ancestorTexCoordMatrix: Matrix3 = Matrix3()

    constructor(displayName: String = "Tiled Image Layer") : super(displayName) {
        this.init()
    }

    protected open fun init() {
        this.pickEnabled = false
    }

    override fun doRender(dc: DrawContext) {
        if (dc.terrain?.getTileCount() ?: return == 0) {
            return
        }

        this.determineActiveProgram(dc)
        this.assembleTiles(dc)

        this.activeProgram = null // clear the active program to avoid leaking render resources
        this.ancestorTile = null // clear the ancestor tile and texture
        this.ancestorTexture = null
    }


    protected fun assembleTiles(dc: DrawContext) {

        if (topLevelTiles.isEmpty()) {
            this.createTopLevelTiles()
        }
        for (tile in topLevelTiles) {
            this.addTileOrDescendants(dc, tile as ImageTile)
        }
    }

    protected open fun determineActiveProgram(dc: DrawContext) {
        this.activeProgram = dc.getProgram(SurfaceTextureProgram.KEY) as SurfaceTextureProgram
        if (this.activeProgram == null) {
            this.activeProgram =
                dc.putProgram(SurfaceTextureProgram.KEY, SurfaceTextureProgram(dc.resources!!)) as SurfaceTextureProgram
        }
    }

    protected fun createTopLevelTiles() {
        val firstLevel: Level = levelSet.firstLevel() ?: return
        Tile.assembleTilesForLevel(firstLevel, this, topLevelTiles)
    }

    protected open fun fetchTileTexture(dc: DrawContext, tile: ImageTile): GpuTexture? {
        var texture: GpuTexture? = dc.getTexture(tile.imageSource!!)
        if (texture == null) {
            texture = dc.retrieveTexture(tile.imageSource!!)
        }
        return texture
    }

    protected fun addTileOrDescendants(dc: DrawContext, tile: ImageTile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(dc, dc.frustum)) {
            return  // ignore the tile and its descendants if it's not visible
        }
        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, detailControl)) {
            addTile(dc, tile)
            return  // use the tile if it does not need to be subdivided
        }

        val currentAncestorTile = ancestorTile
        val currentAncestorTexture = ancestorTexture

        val tileTexture = fetchTileTexture(dc, tile)
        if (tileTexture != null) { // use it as a fallback tile for descendants
            ancestorTile = tile
            ancestorTexture = tileTexture
        }

        for (child in tile.subdivideToCache(this, tileCache, 4)!!) { // each tile has a cached size of 1
            addTileOrDescendants(dc, child as ImageTile) // recursively process the tile's children
        }

        ancestorTile = currentAncestorTile // restore the last fallback tile, even if it was null
        ancestorTexture = currentAncestorTexture
    }

    protected fun addTile(dc: DrawContext, tile: ImageTile) {
        val texture = fetchTileTexture(dc, tile)
        if (texture != null) { // use the tile's own texture
            val pool: Pool<DrawableSurfaceTexture> = dc.getDrawablePool(DrawableSurfaceTexture::class.java)
            val drawable = DrawableSurfaceTexture.obtain(pool)
                .set(this.activeProgram, tile.sector, texture, texture.texCoordTransform)
            dc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
        } else if (this.ancestorTile != null) { // use the ancestor tile's texture, transformed to fill the tile sector
            this.ancestorTexCoordMatrix.set(this.ancestorTexture!!.texCoordTransform)
            this.ancestorTexCoordMatrix.multiplyByTileTransform(tile.sector, this.ancestorTile!!.sector)
            val pool: Pool<DrawableSurfaceTexture> = dc.getDrawablePool(DrawableSurfaceTexture::class.java)
            val drawable: Drawable = DrawableSurfaceTexture.obtain(pool)
                .set(this.activeProgram, tile.sector, this.ancestorTexture, this.ancestorTexCoordMatrix)
            dc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
        }
    }

    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        val tile = ImageTile(sector, level, row, column)
        if (tileUrlFactory != null && this.imageFormat != null) {
            tile.imageSource = ImageSource.fromUrl(tileUrlFactory!!.urlForTile(tile, imageFormat))
        }
        return tile
    }

    protected fun invalidateTiles() {
        topLevelTiles.clear()
        tileCache!!.clear()
    }
}