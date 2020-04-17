package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.DrawContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TerrainTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column) {

    var vertexOrigin = Vec3()

    var vertexPoints: FloatBuffer? = null

}