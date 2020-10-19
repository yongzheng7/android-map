package com.atom.map.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class ExampleUtil {
    companion object{
        /**
         * 组装texcoords 代码 st 01之间
         */
        fun assembleTexCoords(numLat: Int, numLon: Int, result: FloatBuffer, stride: Int): FloatBuffer {
            val ds = 1f / if (numLon > 1) numLon - 1 else 1
            val dt = 1f / if (numLat > 1) numLat - 1 else 1
            val st = FloatArray(2)
            var sIndex: Int
            var tIndex: Int
            var pos: Int
            // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian
            // point corresponding to each latitude and longitude.
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
         * 按照索引进行绘制
         */
        fun assembleTriStripIndices(numLat: Int, numLon: Int): ShortBuffer? { // Allocate a buffer to hold the indices.
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
         * 和球形相接
         */
        fun assembleLineIndices(numLat: Int, numLon: Int): ShortBuffer? { // Allocate a buffer to hold the indices.
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
}