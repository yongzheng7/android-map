package com.atom.map.globe

import android.opengl.GLES20
import com.atom.map.renderable.RenderContext
import com.atom.map.drawable.BasicDrawableTerrain
import com.atom.map.geom.Range
import com.atom.map.geom.Sector
import com.atom.map.geom.Vec3
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.tile.TerrainTile
import com.atom.map.core.tile.Tile
import com.atom.map.core.tile.TileFactory
import com.atom.map.util.Level
import com.atom.map.util.LevelSet
import com.atom.map.util.Logger
import com.atom.map.util.LruMemoryCache
import com.atom.map.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BasicTessellator : Tessellator,
    TileFactory {

    var levelSet: LevelSet = LevelSet(Sector().setFullSphere(), 90.0, 20, 32, 32)
        set(value) {
            field = value
            this.invalidateTiles()
        }
    var detailControl = 80.0

    var topLevelTiles = ArrayList<Tile>()

    var currentTerrain: BasicTerrain = BasicTerrain()

    var tileCache: LruMemoryCache<String?, Array<Tile?>?> =
        LruMemoryCache(200) // cache for 300 tiles

    var levelSetVertexTexCoords: FloatArray? = null

    var levelSetLineElements: ShortArray? = null

    var levelSetTriStripElements: ShortArray? = null

    var levelSetLineElementRange: Range = Range()

    var levelSetTriStripElementRange: Range = Range()

    var levelSetVertexTexCoordBuffer: BufferObject? = null

    var levelSetElementBuffer: BufferObject? = null

    var levelSetVertexTexCoordKey = this.javaClass.name + ".vertexTexCoordKey"

    var levelSetElementKey = this.javaClass.name + ".elementKey"

    constructor() {
    }

    constructor(topLevelDelta: Double, numLevels: Int, tileWidth: Int, tileHeight: Int) {
        if (topLevelDelta <= 0) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "BasicTessellator",
                    "constructor",
                    "invalidTileDelta"
                )
            )
        }
        if (numLevels < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "BasicTessellator",
                    "constructor",
                    "invalidNumLevels"
                )
            )
        }
        if (tileWidth < 1 || tileHeight < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "BasicTessellator",
                    "constructor",
                    "invalidWidthOrHeight"
                )
            )
        }
        levelSet =
            LevelSet(Sector().setFullSphere(), topLevelDelta, numLevels, tileWidth, tileHeight)
    }

    /**
     * 获取棋盘地形
     */
    override fun tessellate(rc: RenderContext) {
        currentTerrain.clear()
        this.assembleTiles(rc)
        rc.terrain = currentTerrain
    }

    /**
     * 创建瓦片
     */
    override fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile {
        return TerrainTile(
            sector,
            level,
            row,
            column
        )
    }

    /**
     * 废除数据
     */
    protected fun invalidateTiles() {
        topLevelTiles.clear()
        currentTerrain.clear()
        tileCache.clear()
        levelSetVertexTexCoords = null
        levelSetLineElements = null
        levelSetTriStripElements = null
    }

    /**
     * 重新组装瓦片
     * 1 先清理  （清理地形中的瓦片集合，清理地形secort区域为0）
     * 2 判断顶层瓦片集合是否为空 （若为空开始组装顶层瓦片）
     * 3 迭代顶层瓦片，并逐层迭代知道最低曾瓦片位置
     */
    protected fun assembleTiles(rc: RenderContext) {

        this.assembleLevelSetBuffers(rc)
        currentTerrain.triStripElements = levelSetTriStripElements

        if (topLevelTiles.isEmpty()) {
            createTopLevelTiles()
        }
        for (tile in topLevelTiles) {
            addTileOrDescendants(rc, tile as TerrainTile)
        }

        levelSetVertexTexCoordBuffer = null
        levelSetElementBuffer = null
    }

    protected fun createTopLevelTiles() {
        val firstLevel: Level = this.levelSet.firstLevel() ?: return
        Tile.assembleTilesForLevel(firstLevel, this, topLevelTiles)
    }

    /**
     *  获取根据瓦片
     *  判断是否在视锥体内
     *  判断是否是最底层瓦片和是否需要细分（若需要则开始细分）并将瓦片加到地形中
     */
    protected fun addTileOrDescendants(rc: RenderContext, tile: TerrainTile) {
        if (!tile.intersectsFrustum(rc, rc.frustum)) {
            return
        }
        if (tile.level.isLastLevel() || !tile.mustSubdivide(rc, detailControl)) {
            addTile(rc, tile)
            return  // 如果不需要细分，请使用图块
        }
        for (child in tile.subdivideToCache(
            this,
            tileCache,
            4
        )!!) { // each tile has a cached size of 1每个图块的缓存大小为1
            addTileOrDescendants(rc, child as TerrainTile) // 递归处理磁贴的子代
        }
    }

    protected fun addTile(rc: RenderContext, tile: TerrainTile) {
        // Assemble the terrain tile's vertex points and add the terrain tile.
        this.prepareTile(rc, tile)
        currentTerrain.addTile(tile) //只添加最后等级的图块 或者 无需再次细分的图块

        val pool: Pool<BasicDrawableTerrain> = rc.getDrawablePool(
            BasicDrawableTerrain::class.java)
        val drawable: BasicDrawableTerrain = BasicDrawableTerrain.obtain(pool)
        this.prepareDrawableTerrain(rc, tile, drawable)
        rc.offerDrawableTerrain(drawable , tile.distanceToCamera)
    }

    protected fun prepareDrawableTerrain(
        rc: RenderContext,
        tile: TerrainTile,
        drawable: BasicDrawableTerrain
    ) {
        drawable.sector.set(tile.sector)
        drawable.vertexOrigin.set(tile.origin)
        // Assemble the drawable's element buffer ranges.
        drawable.lineElementRange.set(levelSetLineElementRange)
        drawable.triStripElementRange.set(levelSetTriStripElementRange)

        // Assemble the drawable's OpenGL buffer objects.
        drawable.vertexPoints = tile.getPointBuffer(rc)
        drawable.vertexTexCoords = levelSetVertexTexCoordBuffer
        drawable.elements = levelSetElementBuffer
    }

    protected fun assembleLevelSetBuffers(rc: RenderContext) {
        val numLat = levelSet.tileHeight + 2
        val numLon = levelSet.tileWidth + 2
        // Assemble the level set's vertex tex coord buffer.
        if (levelSetVertexTexCoords == null) {
            levelSetVertexTexCoords = FloatArray(numLat * numLon * 2)
            assembleVertexTexCoords(numLat, numLon, levelSetVertexTexCoords!!)
        }

        // Assemble the level set's line element buffer.
        if (levelSetLineElements == null) {
            levelSetLineElements = assembleLineElements(numLat, numLon)
        }

        // Assemble the shared triangle strip element buffer.
        if (levelSetTriStripElements == null) {
            levelSetTriStripElements = assembleTriStripElements(numLat, numLon)
        }

        levelSetVertexTexCoordBuffer = rc.getBufferObject(this.levelSetVertexTexCoordKey)
        if (levelSetVertexTexCoordBuffer == null) {
            levelSetVertexTexCoords?.let {
                val size = it.size * 4
                val buffer =
                    ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                buffer.put(it).rewind()
                levelSetVertexTexCoordBuffer =
                    rc.putBufferObject(
                        levelSetVertexTexCoordKey,
                        BufferObject(
                            GLES20.GL_ARRAY_BUFFER,
                            size,
                            buffer
                        )
                    )
            }
        }

        levelSetElementBuffer = rc.getBufferObject(levelSetElementKey)
        if (levelSetElementBuffer == null) {
            ifNotNull(
                levelSetLineElements,
                levelSetTriStripElements,
                { levelSetLineElements: ShortArray, levelSetTriStripElements: ShortArray ->
                    val size = (levelSetLineElements.size + levelSetTriStripElements.size) * 2
                    val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                        .asShortBuffer()
                    buffer.put(this.levelSetLineElements)
                    levelSetLineElementRange.upper = buffer.position()
                    levelSetTriStripElementRange.lower = buffer.position()
                    buffer.put(this.levelSetTriStripElements)
                    levelSetTriStripElementRange.upper = buffer.position()
                    buffer.rewind()
                    levelSetElementBuffer = rc.putBufferObject(
                        levelSetElementKey,
                        BufferObject(
                            GLES20.GL_ELEMENT_ARRAY_BUFFER,
                            size,
                            buffer
                        )
                    )
                })
        }
    }

    fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
        if (value1 != null && value2 != null) {
            bothNotNull(value1, value2)
        }
    }

    protected fun prepareTile(rc: RenderContext, tile: TerrainTile) {
        val tileWidth = tile.level.tileWidth
        val tileHeight = tile.level.tileHeight
        val elevationTimestamp: Long = rc.globe.elevationModel.getTimestamp()
        if (elevationTimestamp != tile.heightTimestamp) {
            var heights = tile.heights
            if (heights == null) {
                heights = FloatArray(tileWidth * tileHeight)
            }
            Arrays.fill(heights, 0f)
            rc.globe.elevationModel.getHeightGrid(tile.sector, tileWidth, tileHeight, heights)
            tile.heights = (heights)
        }
        val verticalExaggeration = rc.verticalExaggeration
        if (verticalExaggeration != tile.verticalExaggeration || elevationTimestamp != tile.heightTimestamp) {
            val origin: Vec3 = tile.origin
            val heights = tile.heights
            var points = tile.points
            val borderHeight = (tile.minTerrainElevation * verticalExaggeration).toFloat()

            if (points == null) {
                points = FloatArray((tileWidth + 2) * (tileHeight + 2) * 3)
            }
            val rowStride = (tileWidth + 2) * 3
            rc.globe.geographicToCartesian(
                tile.sector.centroidLatitude(),
                tile.sector.centroidLongitude(),
                0.0,
                origin
            )
            rc.globe.geographicToCartesianGrid(
                tile.sector,
                tileWidth,
                tileHeight,
                heights,
                verticalExaggeration.toFloat(),
                origin,
                points,
                rowStride + 3,
                rowStride
            )
            rc.globe.geographicToCartesianBorder(
                tile.sector,
                tileWidth + 2,
                tileHeight + 2,
                borderHeight,
                origin,
                points
            )
            tile.origin.set(origin)
            tile.points = (points)
        }
        tile.heightTimestamp = (elevationTimestamp)
        tile.verticalExaggeration = (verticalExaggeration)
    }

    /**
     * 组装纹理顶点缓存
     */
    protected fun assembleVertexTexCoords(
        numLat: Int,
        numLon: Int,
        result: FloatArray
    ): FloatArray {
        val ds = 1f / if (numLon > 1) numLon - 3 else 1
        val dt = 1f / if (numLat > 1) numLat - 3 else 1 // 16+ 2 - 3 = 15
        var s = 0f
        var t = 0f
        var sidx: Int
        var tidx: Int
        var resultIdx = 0
        // Iterate over the number of latitude and longitude vertices, computing the parameterized S and T coordinates
        // corresponding to each vertex.
        tidx = 0
        while (tidx < numLat) { 0 < 18
            if (tidx < 2) {
                t = 0f // explicitly set the first T coordinate to 0 to ensure alignment
            } else if (tidx < numLat - 2) {
                t += dt
            } else {
                t = 1f // explicitly set the last T coordinate to 1 to ensure alignment
            }
            sidx = 0
            while (sidx < numLon) {
                if (sidx < 2) {
                    s = 0f // explicitly set the first S coordinate to 0 to ensure alignment
                } else if (sidx < numLon - 2) {
                    s += ds
                } else {
                    s = 1f // explicitly set the last S coordinate to 1 to ensure alignment
                }
                result[resultIdx++] = s
                result[resultIdx++] = t
                sidx++
            }
            tidx++
        }
        return result
    }

    /**
     * 顶点的索引 通过索引法进行绘制
     */
    protected fun assembleTriStripElements(
        numLat: Int,
        numLon: Int
    ): ShortArray { // Allocate a buffer to hold the indices.
        val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
        val result = ShortArray(count)
        var pos = 0
        var vertex = 0

        for (latIndex in 0 until numLat - 1) {
            for (lonIndex in 0 until numLon) {
                vertex = lonIndex + latIndex * numLon
                result[pos++] = (vertex + numLon).toShort()
                result[pos++] = vertex.toShort()
            }
            if (latIndex < numLat - 2) {
                result[pos++] = vertex.toShort()
                result[pos++] = ((latIndex + 2) * numLon).toShort()
            }
        }
        return result
    }

    /**
     * 组装纹理的线索引
     */
    protected fun assembleLineElements(
        numLat: Int,
        numLon: Int
    ): ShortArray { // Allocate a buffer to hold the indices.
        val count = (numLat * (numLon - 1) + numLon * (numLat - 1)) * 2
        val result = ShortArray(count)
        var pos = 0
        var vertex: Int
        // Add a line between each row to define the horizontal cell outlines.
        for (latIndex in 0 until numLat) {
            for (lonIndex in 0 until numLon - 1) {
                vertex = lonIndex + latIndex * numLon
                result[pos++] = vertex.toShort()
                result[pos++] = (vertex + 1).toShort()
            }
        }
        // Add a line between each column to define the vertical cell outlines.
        for (lonIndex in 0 until numLon) {
            for (latIndex in 0 until numLat - 1) {
                vertex = lonIndex + latIndex * numLon
                result[pos++] = vertex.toShort()
                result[pos++] = (vertex + numLon).toShort()
            }
        }
        return result
    }
}