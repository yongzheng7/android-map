package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.LruMemoryCache
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class BasicTessellator : Tessellator, TileFactory {

    var levelSet: LevelSet = LevelSet(Sector().setFullSphere(), 90.0, 20, 32, 32)
    set(value) {
        field = value
        this.invalidateTiles()
    }

    var topLevelTiles = ArrayList<Tile>()

    protected var currentTerrain: BasicTerrain = BasicTerrain()
    /**
     * 缓存每个key 对应的四个瓦片
     */
    protected var tileCache: LruMemoryCache<String?, Array<Tile?>?> = LruMemoryCache(300) // cache for 300 tiles

    var detailControl = 80.0

    constructor() {
    }

    constructor(topLevelDelta: Double, numLevels: Int, tileWidth: Int, tileHeight: Int) {
        if (topLevelDelta <= 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidTileDelta"))
        }
        if (numLevels < 1) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidNumLevels"))
        }
        if (tileWidth < 1 || tileHeight < 1) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidWidthOrHeight"))
        }
        levelSet = LevelSet(Sector().setFullSphere(), topLevelDelta, numLevels, tileWidth, tileHeight)
    }

    /**
     * 获取棋盘地形
     */
    override fun tessellate(dc: DrawContext): Terrain? {
        this.assembleTiles(dc)
        this.assembleSharedBuffers()
        return currentTerrain
    }

    /**
     * 创建瓦片
     */
    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        return TerrainTile(sector, level, row, column)
    }

    /**
     * 废除数据
     */
    protected fun invalidateTiles() {
        topLevelTiles.clear()
        currentTerrain.clearTiles()
        tileCache.clear()
    }

    /**
     * 重新组装瓦片
     * 1 先清理  （清理地形中的瓦片集合，清理地形secort区域为0）
     * 2 判断顶层瓦片集合是否为空 （若为空开始组装顶层瓦片）
     * 3 迭代顶层瓦片，并逐层迭代知道最低曾瓦片位置
     */
    protected fun assembleTiles(dc: DrawContext) {
        // 地形清除瓦片
        currentTerrain.clearTiles() // 1

        if (topLevelTiles.isEmpty()) {
            createTopLevelTiles()
        }
        for (tile in topLevelTiles) {
            addTileOrDescendants(dc, tile as TerrainTile)
        }
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
    protected fun addTileOrDescendants(dc: DrawContext, tile: TerrainTile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(dc, dc.frustum)) {
            return
        }
        if ( tile.level.isLastLevel() || !tile.mustSubdivide(dc, detailControl)) {
            addTile(dc, tile)
            return  // 如果不需要细分，请使用图块
        }
        for (child in tile.subdivideToCache(this, tileCache, 4)!!) { // each tile has a cached size of 1每个图块的缓存大小为1
            addTileOrDescendants(dc, child as TerrainTile) // 递归处理磁贴的子代
        }
    }

    protected fun addTile(dc: DrawContext, tile: TerrainTile) {
        if (tile.mustAssembleTileVertices(dc)) {
            tile.assembleTileVertices(dc) // build the tile's geometry when necessary
        }
        currentTerrain.addTile(tile) //只添加最后等级的图块 或者 无需再次细分的图块
    }

    /**
     * 组装 纹理顶点， 顶点索引 ， 线顶点
     */
    protected fun assembleSharedBuffers() {
        val numLat = levelSet.tileHeight
        val numLon = levelSet.tileWidth
        // 瓦片的纹理顶点
        if (currentTerrain.tileTexCoords == null) {
            val buffer = ByteBuffer.allocateDirect(numLat * numLon * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            assembleTexCoords(numLat, numLon, buffer, 2).rewind()
            currentTerrain.tileTexCoords = buffer
        }
        if (currentTerrain.tileTriStripIndices == null) {
            val buffer = assembleTriStripIndices(numLat, numLon)
            currentTerrain.tileTriStripIndices = buffer
        }
        if (currentTerrain.tileLineIndices == null) {
            val buffer = assembleLineIndices(numLat, numLon)
            currentTerrain.tileLineIndices = buffer
        }
    }

    /**
     * 组装纹理顶点缓存
     */
    protected fun assembleTexCoords(numLat: Int, numLon: Int, result: FloatBuffer, stride: Int): FloatBuffer {
        val ds = 1f / if (numLon > 1) numLon - 1 else 1
        val dt = 1f / if (numLat > 1) numLat - 1 else 1
        val st = FloatArray(2)
        var sIndex: Int
        var tIndex: Int
        var pos: Int
        // Iterate over the number of latitude and longitude vertices, computing the parameterized S and T coordinates
        // corresponding to each vertex.
        tIndex = 0
        st[1] = 0f
        while (tIndex < numLat) {
            if (tIndex == numLat - 1) {
                st[1] = 1f // explicitly set the last T coordinate to 1 to ensure alignment
            }
            sIndex = 0
            st[0] = 0f
            while (sIndex < numLon) {
                if (sIndex == numLon - 1) {
                    st[0] = 1f // explicitly set the last S coordinate to 1 to ensure alignment
                }
                pos = result.position()
                result.put(st, 0, 2)
                if (result.limit() >= pos + stride) {
                    result.position(pos + stride)
                }
                sIndex++
                st[0] += ds
            }
            tIndex++
            st[1] += dt
        }
        return result
    }

    /**
     * 顶点的索引 通过索引法进行绘制
     */
    protected fun assembleTriStripIndices(numLat: Int, numLon: Int): ShortBuffer { // Allocate a buffer to hold the indices.
        val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
        val result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        val index = ShortArray(2)
        var vertex = 0
        for (latIndex in 0 until numLat - 1) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (lonIndex in 0 until numLon) {
                vertex = lonIndex + latIndex * numLon
                index[0] = (vertex + numLon).toShort()
                index[1] = vertex.toShort()
                result.put(index)
            }
            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                index[0] = vertex.toShort()
                index[1] = ((latIndex + 2) * numLon).toShort()
                result.put(index)
            }
        }
        return result.rewind() as ShortBuffer
    }

    /**
     * 组装纹理的线索引
     */
    protected fun assembleLineIndices(numLat: Int, numLon: Int): ShortBuffer { // Allocate a buffer to hold the indices.
        val count = (numLat * (numLon - 1) + numLon * (numLat - 1)) * 2
        val result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        val index = ShortArray(2)
        // Add a line between each row to define the horizontal cell outlines.
        for (latIndex in 0 until numLat) {
            for (lonIndex in 0 until numLon - 1) {
                val vertex = lonIndex + latIndex * numLon
                index[0] = vertex.toShort()
                index[1] = (vertex + 1).toShort()
                result.put(index)
            }
        }
        // Add a line between each column to define the vertical cell outlines.
        for (lonIndex in 0 until numLon) {
            for (latIndex in 0 until numLat - 1) {
                val vertex = lonIndex + latIndex * numLon
                index[0] = vertex.toShort()
                index[1] = (vertex + numLon).toShort()
                result.put(index)
            }
        }
        return result.rewind() as ShortBuffer
    }
}