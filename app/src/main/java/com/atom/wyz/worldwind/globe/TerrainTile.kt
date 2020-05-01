package com.atom.wyz.worldwind.globe

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.Level
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TerrainTile(sector: Sector?, level: Level?, row: Int, column: Int) : Tile(sector, level, row, column) {

    var vertexOrigin = Vec3()

    var vertexPoints: FloatArray? = null

    var vertexPointKey: String

    init {
        vertexPointKey = this::javaClass.name + ".vertexPoint." + tileKey
    }

    fun getVertexPointBuffer(rc: RenderContext): BufferObject? {
        vertexPoints?.let {
            rc.getBufferObject(vertexPointKey)?.let {
                return it
            }

            val size = it.size * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(it).rewind()
            return rc.putBufferObject(vertexPointKey, BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer))

        } ?: return null

    }
}