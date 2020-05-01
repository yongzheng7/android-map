package com.atom.wyz.worldwind.globe

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.BasicDrawableTerrain
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.LruMemoryCache
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

    protected var tileCache: LruMemoryCache<String?, Array<Tile?>?> = LruMemoryCache(300) // cache for 300 tiles

    var detailControl = 80.0

    var levelSetVertexTexCoords: FloatArray? = null

    var levelSetLineElements: ShortArray? = null

    var levelSetTriStripElements: ShortArray? = null

    protected var levelSetBufferObjects: Array<BufferObject?> = arrayOfNulls<BufferObject>(3)

    protected var levelSetBufferKeys = arrayOf(
        this.javaClass.name + ".levelSetBuffer[0]",
        this.javaClass.name + ".levelSetBuffer[1]",
        this.javaClass.name + ".levelSetBuffer[2]"
    )

    constructor() {
    }

    constructor(topLevelDelta: Double, numLevels: Int, tileWidth: Int, tileHeight: Int) {
        if (topLevelDelta <= 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidTileDelta")
            )
        }
        if (numLevels < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidNumLevels")
            )
        }
        if (tileWidth < 1 || tileHeight < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidWidthOrHeight")
            )
        }
        levelSet = LevelSet(Sector().setFullSphere(), topLevelDelta, numLevels, tileWidth, tileHeight)
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
    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        return TerrainTile(sector, level, row, column)
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

        // Release references to render resources acquired while assembling tiles.
        Arrays.fill(levelSetBufferObjects, null)
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
        for (child in tile.subdivideToCache(this, tileCache, 4)!!) { // each tile has a cached size of 1每个图块的缓存大小为1
            addTileOrDescendants(rc, child as TerrainTile) // 递归处理磁贴的子代
        }
    }

    protected fun addTile(rc: RenderContext, tile: TerrainTile) {
        if (mustAssembleTilePoints(rc , tile )) {
            this.assembleTilePoints(rc, tile)
        }

        currentTerrain.addTile(tile) //只添加最后等级的图块 或者 无需再次细分的图块

        val pool: Pool<BasicDrawableTerrain> = rc.getDrawablePool(
            BasicDrawableTerrain::class.java)
        val drawable: BasicDrawableTerrain = BasicDrawableTerrain.obtain(pool)
        this.prepareDrawableTerrain(rc, tile, drawable)
        rc.offerDrawableTerrain(drawable)
    }

    protected fun prepareDrawableTerrain(
        rc: RenderContext,
        tile: TerrainTile,
        drawable: BasicDrawableTerrain
    ) {
        drawable.sector.set(tile.sector)
        drawable.vertexOrigin.set(tile.vertexOrigin)
        drawable.vertexPoints = tile.getVertexPointBuffer(rc)
        drawable.vertexTexCoords = levelSetBufferObjects[0]
        drawable.lineElements = levelSetBufferObjects[1]
        drawable.triStripElements = levelSetBufferObjects[2]
    }

    protected fun assembleLevelSetBuffers(rc: RenderContext) {
        val numLat = levelSet.tileHeight
        val numLon = levelSet.tileWidth
        // Assemble the level set's vertex tex coord buffer.
        if (levelSetVertexTexCoords == null) {
            levelSetVertexTexCoords = FloatArray(numLat * numLon * 2)
            assembleVertexTexCoords(numLat, numLon, levelSetVertexTexCoords!!, 2 , 0)
        }

        // Assemble the level set's line element buffer.
        if (levelSetLineElements == null) {
            levelSetLineElements = assembleLineElements(numLat, numLon)
        }

        // Assemble the shared triangle strip element buffer.
        if (levelSetTriStripElements == null) {
            levelSetTriStripElements = assembleTriStripElements(numLat, numLon)
        }

        levelSetBufferObjects[0] = rc.getBufferObject(levelSetBufferKeys[0])
        if (levelSetBufferObjects[0] == null) {
            levelSetVertexTexCoords ?.let{
                val size = it.size * 4
                val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                buffer.put(it).rewind()
                levelSetBufferObjects[0] = rc.putBufferObject(levelSetBufferKeys[0], BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer))
            }
        }

        levelSetBufferObjects[1] = rc.getBufferObject(levelSetBufferKeys[1])
        if (levelSetBufferObjects[1] == null) {
            levelSetLineElements ?.let {
                val size = it.size * 2
                val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
                buffer.put(it).rewind()
                levelSetBufferObjects[1] = rc.putBufferObject(levelSetBufferKeys[1], BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer))
            }
        }

        levelSetBufferObjects[2] = rc.getBufferObject(levelSetBufferKeys[2])
        if (levelSetBufferObjects[2] == null) {
            levelSetTriStripElements ?.let {
                val size = it.size * 2
                val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
                buffer.put(it).rewind()
                levelSetBufferObjects[2] = rc.putBufferObject(levelSetBufferKeys[2], BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer))
            }
        }
    }

    fun mustAssembleTilePoints(rc: RenderContext, tile: TerrainTile): Boolean {
        return tile.vertexPoints == null
    }
    protected fun assembleTilePoints(rc: RenderContext, tile: TerrainTile) {
        val globe = rc.globe ?: return

        val numLat = tile.level.tileWidth
        val numLon = tile.level.tileHeight
        val origin = tile.vertexOrigin

        var points = tile.vertexPoints
        if (points == null) {
            points = FloatArray(numLat * numLon * 3)
        }

        globe.geographicToCartesian(tile.sector.centroidLatitude(), tile.sector.centroidLongitude(), 0.0, origin)
        globe.geographicToCartesianGrid(tile.sector, numLat, numLon, null, origin, points, 3 ,0)
        tile.vertexOrigin.set(origin)
        tile.vertexPoints = points
    }

    /**
     * 组装纹理顶点缓存
     */
    protected fun assembleVertexTexCoords(numLat: Int, numLon: Int, result: FloatArray, stride: Int , poss :Int): FloatArray {
        val ds = 1f / if (numLon > 1) numLon - 1 else 1
        val dt = 1f / if (numLat > 1) numLat - 1 else 1
        val st = FloatArray(2)
        var sIndex: Int
        var tIndex: Int
        var pos = poss
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
                result[pos] = st[0]
                result[pos + 1] = st[1]
                pos += stride

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
    protected fun assembleTriStripElements(
        numLat: Int,
        numLon: Int
    ): ShortArray { // Allocate a buffer to hold the indices.
        val count = ((numLat - 1) * numLon + (numLat - 2)) * 2
        val result = ShortArray(count )
        var pos = 0
        var vertex = 0

        for (latIndex in 0 until numLat - 1) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
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
        val result = ShortArray(count )
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