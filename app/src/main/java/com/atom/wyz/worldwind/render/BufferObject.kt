package com.atom.wyz.worldwind.render

import android.opengl.GLES20
import android.util.SparseArray
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Range
import com.atom.wyz.worldwind.util.Logger
import java.nio.Buffer

class BufferObject : RenderResource {

    var bufferId = IntArray(1)

    var bufferTarget = 0

    var bufferLength = 0

    var bufferByteCount = 0

    var buffer: Buffer? = null

    var ranges: SparseArray<Range> = SparseArray<Range>()

    constructor(target: Int, size: Int, buffer: Buffer?) {
        bufferTarget = target
        bufferLength = buffer?.remaining() ?: 0
        bufferByteCount = size
        this.buffer = buffer
    }

    fun bindBuffer(dc: DrawContext): Boolean {
        if (buffer != null) {
            this.loadBuffer(dc)
            buffer = null
        }
        if (bufferId[0] != 0) {
            dc.bindBuffer(bufferTarget, bufferId[0])
        }
        return bufferId[0] != 0
    }


    override fun release(dc: DrawContext) {
        this.deleteBufferObject(dc)
        buffer = null // buffer can be non-null if the object has not been bound
    }

    protected fun loadBuffer(dc: DrawContext) {
        val currentBuffer = dc.currentBuffer(bufferTarget)
        try {
            if (bufferId[0] == 0) {
                createBufferObject(dc)
            }

            dc.bindBuffer(bufferTarget, bufferId[0])

            loadBufferObjectData(dc)
        } catch (e: Exception) {
            deleteBufferObject(dc)
            Logger.logMessage(Logger.ERROR, "BufferObject", "loadBuffer", "Exception attempting to load buffer data", e)
        } finally {
            dc.bindBuffer(bufferTarget, currentBuffer)
        }
    }

    protected fun createBufferObject(dc: DrawContext) {
        GLES20.glGenBuffers(1, bufferId, 0)
    }

    protected fun deleteBufferObject(dc: DrawContext) {
        if (bufferId[0] != 0) {
            GLES20.glDeleteBuffers(1, bufferId, 0)
            bufferId[0] = 0
        }
    }

    protected fun loadBufferObjectData(dc: DrawContext) {
        GLES20.glBufferData(bufferTarget, bufferByteCount, buffer, GLES20.GL_STATIC_DRAW)
    }
}