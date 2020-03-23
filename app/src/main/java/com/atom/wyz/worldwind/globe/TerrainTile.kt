package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.render.DrawContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TerrainTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column) {
    /**
     *  此瓦片跨越区域的经纬度中心点的笛卡尔坐标
     */
    var tileOrigin: Vec3? = null

    var tileVertices: FloatBuffer? = null

    /**
     * 必须重新组装瓦片
     */
    fun mustAssembleTileVertices(dc: DrawContext): Boolean {
        return tileVertices == null
    }
    /**
     * 重新组装瓦片的
     */
    fun assembleTileVertices(dc: DrawContext) {
        val numLat = level.tileWidth //该等级图块宽
        val numLon = level.tileHeight//该等级图块高
        if (tileOrigin == null) {
            tileOrigin = Vec3()
        }
        if (tileVertices == null) {
            tileVertices = ByteBuffer.allocateDirect(numLat * numLon * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        }
        val globe: Globe? = dc.globe
        // j经度纬度高度转 笛卡尔 xyz
        globe?.geographicToCartesian(sector.centroidLatitude(), sector.centroidLongitude(), 0.0, tileOrigin)
        // 生成笛卡尔坐标系 中 四个顶点
        globe?.geographicToCartesianGrid(sector, numLat, numLon, null, tileOrigin, tileVertices, 3)?.rewind()
    }
}