package com.atom.map.core.tile

import android.opengl.GLES20
import com.atom.map.core.shader.BufferObject
import com.atom.map.geom.Sector
import com.atom.map.geom.Vec3
import com.atom.map.layer.render.RenderContext
import com.atom.map.util.Level
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TerrainTile(sector: Sector, level: Level, row: Int, column: Int) : Tile(sector, level, row, column) {

    companion object{
        var pointBufferSequence: Long = 0
    }

    var minTerrainElevation = -Short.MAX_VALUE.toFloat()

    var heights: FloatArray? = null

    var points: FloatArray? = null
        set(value) {
            field = value
            pointBufferKey = "TerrainTile.points." + tileKey.toString() + "." + pointBufferSequence++
        }

    val origin = Vec3()

    var heightTimestamp: Long = 0

    var verticalExaggeration = 0.0

    var pointBufferKey: String? = null



    fun getPointBuffer(rc: RenderContext): BufferObject? {
        if (points == null) {
            return null
        }
        val bufferObject = pointBufferKey?.let { rc.getBufferObject(it) }
        if (bufferObject != null) {
            return bufferObject
        }

        // TODO consider a pool of terrain tiles
        // TODO consider a pool of terrain tile vertex buffers
        val size = points!!.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        return pointBufferKey?.let {
            rc.putBufferObject(
                it,
                BufferObject(
                    GLES20.GL_ARRAY_BUFFER,
                    size,
                    buffer
                )
            )
        }
    }
}