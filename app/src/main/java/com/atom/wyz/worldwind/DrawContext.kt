package com.atom.wyz.worldwind

import android.opengl.GLES20
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableList
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.draw.DrawableTerrain
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Vec2
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.pick.PickedObjectList
import com.atom.wyz.worldwind.render.BufferObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

class DrawContext {

    var eyePoint: Vec3 = Vec3()

    var modelview: Matrix4 = Matrix4()

    var projection: Matrix4 = Matrix4()

    var modelviewProjection: Matrix4 = Matrix4()

    var screenProjection: Matrix4 = Matrix4()

    var infiniteProjection = Matrix4()

    var drawableQueue: DrawableQueue? = null

    var drawableTerrain: DrawableList? = null

    var pickedObjects: PickedObjectList? = null

    var pickPoint: Vec2? = null

    var pickMode = false

    protected var programId = 0

    protected var textureUnit = GLES20.GL_TEXTURE0

    protected var textureId = IntArray(32)

    protected var arrayBufferId = 0

    protected var elementArrayBufferId = 0

    protected var unitSquareBuffer: BufferObject? = null

    protected var scratchList = ArrayList<Any?>()

    private val pixelBuffer =
        ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())

    private val pixelArray = ByteArray(4)


    fun reset() {
        eyePoint.set(0.0, 0.0, 0.0)
        modelview.setToIdentity()
        projection.setToIdentity()
        modelviewProjection.setToIdentity()
        screenProjection.setToIdentity()
        infiniteProjection.setToIdentity()
        drawableQueue = null
        drawableTerrain = null

        pickedObjects = null
        pickPoint = null
        pickMode = false

        scratchList.clear()
    }

    fun contextLost() {
        programId = 0
        textureUnit = GLES20.GL_TEXTURE0
        arrayBufferId = 0
        elementArrayBufferId = 0
        unitSquareBuffer = null
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

    val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f)

    fun unitSquareBuffer(): BufferObject {
        unitSquareBuffer?.let {
            return it
        }
        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        unitSquareBuffer = BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer)
        return unitSquareBuffer!!
    }

    fun scratchList(): ArrayList<Any?> {
        return scratchList
    }

    /**
     * 在当前活动的OpenGL帧缓冲区中的屏幕点读取片段颜色。 X和Y组件指示OpenGL屏幕坐标，该坐标起源于帧缓冲区的左下角。
     */
    fun readPixelColor(x: Int, y: Int, result_temp: Color?): Color {
        val result = result_temp ?: Color()
        GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer.rewind())
        pixelBuffer[pixelArray]
        result.red = (pixelArray[0] and 0xFF.toByte()) / 0xFF.toFloat()
        result.green = (pixelArray[1] and 0xFF.toByte()) / 0xFF.toFloat()
        result.blue = (pixelArray[2] and 0xFF.toByte()) / 0xFF.toFloat()
        result.alpha = (pixelArray[3] and 0xFF.toByte()) / 0xFF.toFloat()
        return result
    }

}