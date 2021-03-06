package com.atom.map.renderable

import com.atom.map.core.shader.GpuTexture
import com.atom.map.core.shader.SurfaceTextureProgram
import com.atom.map.core.tile.ImageTile
import com.atom.map.core.tile.Tile
import com.atom.map.core.tile.TileFactory
import com.atom.map.geom.Matrix3
import com.atom.map.drawable.Drawable
import com.atom.map.drawable.DrawableSurfaceTexture
import com.atom.map.util.Level
import com.atom.map.util.LevelSet
import com.atom.map.util.LruMemoryCache
import com.atom.map.util.pool.Pool
import java.util.*

open class TiledSurfaceImage :
    AbstractRenderable {

    var levelSet: LevelSet = LevelSet()
        set(value) {
            field = value
            this.invalidateTiles()
        }
    var tileFactory: TileFactory? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }

    var imageFormat: String? = null
        set(value) {
            field = value
            this.invalidateTiles()
        }

    var imageOptions: ImageOptions? = null
        set(value) {
            field = value
            invalidateTiles()
        }

    var detailControl = 4.0

    protected var topLevelTiles = ArrayList<Tile>()

    protected var tileCache: LruMemoryCache<String?, Array<Tile?>?>? = LruMemoryCache(500)

    protected var activeProgram: SurfaceTextureProgram? = null

    protected var ancestorTile: ImageTile? = null

    protected var ancestorTexture: GpuTexture? = null

    protected var ancestorTexCoordMatrix: Matrix3 = Matrix3()

    constructor(displayName: String = "Tiled Image Layer") : super(displayName) {
    }


    override fun doRender(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return  // no terrain surface to render on
        }
        this.determineActiveProgram(rc)
        this.assembleTiles(rc)

        this.activeProgram = null // clear the active program to avoid leaking render resources
        this.ancestorTile = null // clear the ancestor tile and texture
        this.ancestorTexture = null
    }


    protected fun assembleTiles(rc: RenderContext) {
        if (topLevelTiles.isEmpty()) {
            this.createTopLevelTiles()
        }
        for (tile in topLevelTiles) {
            this.addTileOrDescendants(rc, tile as ImageTile)
        }
    }

    protected open fun determineActiveProgram(rc: RenderContext) {
        this.activeProgram = rc.getProgram(SurfaceTextureProgram.KEY) as SurfaceTextureProgram?
        if (this.activeProgram == null) {
            this.activeProgram =
                rc.putProgram(
                    SurfaceTextureProgram.KEY,
                    SurfaceTextureProgram(rc.resources)
                ) as SurfaceTextureProgram
        }
    }

    protected fun createTopLevelTiles() {
        val firstLevel: Level = levelSet.firstLevel() ?: return
        Tile.assembleTilesForLevel(firstLevel, tileFactory!!, topLevelTiles)
    }

    protected fun addTileOrDescendants(rc: RenderContext, tile: ImageTile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(
                rc,
                rc.frustum
            )
        ) {
            return  // ignore the tile and its descendants if it's not visible
        }
        if (tile.level.isLastLevel() || !tile.mustSubdivide(rc, detailControl)) {
            addTile(rc, tile)
            return  // use the tile if it does not need to be subdivided
        }

        val currentAncestorTile = this.ancestorTile
        val currentAncestorTexture = this.ancestorTexture

        val tileImageSource = tile.imageSource
        if (tileImageSource != null) { // tile has an image source; its level is not empty
            val tileTexture = rc.getTexture(tileImageSource)
            if (tileTexture != null) { // tile has a texture; use it as a fallback tile for descendants
                ancestorTile = tile
                ancestorTexture = tileTexture
            }
        }

        for (child in tile.subdivideToCache(
            tileFactory!!,
            tileCache,
            4
        )!!) { // each tile has a cached size of 1
            addTileOrDescendants(rc, child as ImageTile) // recursively process the tile's children
        }

        this.ancestorTile = currentAncestorTile // restore the last fallback tile, even if it was null
        this.ancestorTexture = currentAncestorTexture
    }

    protected fun addTile(rc: RenderContext, tile: ImageTile) {
        val imageSource = tile.imageSource ?: return  // no image source indicates an empty level or an image missing from the tiled data store
        var texture = rc.getTexture(imageSource) // try to get the texture from the cache
        if (texture == null) {
            texture = rc.retrieveTexture(imageSource, imageOptions , tile.distanceToCamera.toInt()) // puts retrieved textures in the cache
        }
        if (texture != null) { // use the tile's own texture
            val pool: Pool<DrawableSurfaceTexture> =
                rc.getDrawablePool(DrawableSurfaceTexture::class.java)
            val drawable = DrawableSurfaceTexture.obtain(pool)
                .set(this.activeProgram, tile.sector, texture, texture.texCoordTransform)
            rc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
        } else if (this.ancestorTile != null) {
            // use the ancestor tile's texture, transformed to fill the tile sector
            this.ancestorTexCoordMatrix.set(this.ancestorTexture!!.texCoordTransform)
            this.ancestorTexCoordMatrix.multiplyByTileTransform(
                tile.sector,
                this.ancestorTile!!.sector
            )
            val pool: Pool<DrawableSurfaceTexture> =
                rc.getDrawablePool(DrawableSurfaceTexture::class.java)
            val drawable: Drawable = DrawableSurfaceTexture.obtain(pool)
                .set(
                    this.activeProgram,
                    tile.sector,
                    this.ancestorTexture,
                    this.ancestorTexCoordMatrix
                )
            rc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
        }
    }

    protected fun invalidateTiles() {
        topLevelTiles.clear()
        tileCache!!.clear()
    }
}