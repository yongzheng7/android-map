package com.atom.wyz.worldwind

import android.opengl.GLES20
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableList
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.draw.DrawableTerrain
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class DrawContext {

    var modelview: Matrix4 = Matrix4()

    var projection: Matrix4 = Matrix4()

    var modelviewProjection: Matrix4 = Matrix4()

    var screenProjection: Matrix4 = Matrix4()

    var eyePoint: Vec3 = Vec3()

    var drawableQueue: DrawableQueue? = null

    var drawableTerrain: DrawableList? = null

    protected var programId = 0

    protected var textureUnit = GLES20.GL_TEXTURE0

    protected var textureId = IntArray(32)

    protected var arrayBufferId = 0

    protected var elementArrayBufferId = 0

    protected var unitSquareBufferId = 0

    protected var scratchList = ArrayList<Any?>()


    fun reset() {
        modelview.setToIdentity()
        projection.setToIdentity()
        modelviewProjection.setToIdentity()
        screenProjection.setToIdentity()
        eyePoint.set(0.0, 0.0, 0.0)
        scratchList.clear()
        drawableQueue = null
        drawableTerrain = null
    }

    fun contextLost() {
        programId = 0
        textureUnit = GLES20.GL_TEXTURE0
        arrayBufferId = 0
        elementArrayBufferId = 0
        unitSquareBufferId = 0
        Arrays.fill(textureId, 0)
    }

    fun rewindDrawables() {
        drawableQueue?.sortDrawables()
    }

    fun peekDrawable(): Drawable? {
        return this.drawableQueue?.peekDrawable()
    }

    fun pollDrawable(): Drawable? {
        return this.drawableQueue?.pollDrawable()
    }

    fun getDrawableTerrainCount(): Int {
        return drawableTerrain?.count() ?: 0
    }

    fun getDrawableTerrain(index: Int): DrawableTerrain? {
        return drawableTerrain?.getDrawable(index) as DrawableTerrain?
    }

    fun currentProgram(): Int {
        return programId
    }

    fun useProgram(programId: Int) {
        if (this.programId != programId) {
            this.programId = programId
            GLES20.glUseProgram(programId)
        }
    }

    fun currentTextureUnit(): Int {
        return textureUnit
    }

    fun activeTextureUnit(textureUnit: Int) {
        if (this.textureUnit != textureUnit) {
            this.textureUnit = textureUnit
            GLES20.glActiveTexture(textureUnit)
        }
    }

    fun currentTexture(): Int {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        return textureId[textureUnitIndex]
    }

    fun currentTexture(textureUnit: Int): Int {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        return textureId[textureUnitIndex]
    }

    fun bindTexture(textureId: Int) {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        if (this.textureId[textureUnitIndex] != textureId) {
            this.textureId[textureUnitIndex] = textureId
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        }
    }

    fun currentBuffer(target: Int): Int {
        return if (target == GLES20.GL_ARRAY_BUFFER) {
            arrayBufferId
        } else if (target == GLES20.GL_ELEMENT_ARRAY_BUFFER) {
            elementArrayBufferId
        } else {
            0
        }
    }

    fun bindBuffer(target: Int, bufferId: Int) {
        if (target == GLES20.GL_ARRAY_BUFFER && arrayBufferId != bufferId) {
            arrayBufferId = bufferId
            GLES20.glBindBuffer(target, bufferId)
        } else if (target == GLES20.GL_ELEMENT_ARRAY_BUFFER && elementArrayBufferId != bufferId) {
            elementArrayBufferId = bufferId
            GLES20.glBindBuffer(target, bufferId)
        } else {
            GLES20.glBindBuffer(target, bufferId)
        }
    }

    fun unitSquareBuffer(): Int {
        if (unitSquareBufferId != 0) {
            return unitSquareBufferId
        }
        val newBuffer = IntArray(1)
        GLES20.glGenBuffers(1, newBuffer, 0)
        unitSquareBufferId = newBuffer[0]
        val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f) // lower right corner
        val size = points.size
        val quadBuffer =
            ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        quadBuffer.put(points).rewind()
        val currentBuffer = currentBuffer(GLES20.GL_ARRAY_BUFFER)
        try {
            bindBuffer(GLES20.GL_ARRAY_BUFFER, unitSquareBufferId)
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size * 4, quadBuffer, GLES20.GL_STATIC_DRAW)
        } finally {
            bindBuffer(GLES20.GL_ARRAY_BUFFER, currentBuffer)
        }
        return unitSquareBufferId
    }

    fun scratchList(): ArrayList<Any?> {
        return scratchList
    }

}