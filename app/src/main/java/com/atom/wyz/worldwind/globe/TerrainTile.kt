package com.atom.wyz.worldwind.globe

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.Level
import java.nio.FloatBuffer

class TerrainTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column) {

    var vertexOrigin = Vec3()

    var vertexPoints: FloatBuffer? = null

    var vertexPointKey: String

    init {
        vertexPointKey = this::javaClass.name + ".vertexPoint." + tileKey
    }

    fun getVertexPointBuffer(rc: RenderContext): BufferObject? {
        var buffer = rc.getBufferObject(vertexPointKey)
        if (buffer == null) {
            buffer = rc.putBufferObject(vertexPointKey, BufferObject(GLES20.GL_ARRAY_BUFFER, vertexPoints))
        }
        return buffer
    }
}