package com.atom.wyz.worldwind.context

import android.opengl.GLES20
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.draw.DrawableTerrain
import com.atom.wyz.worldwind.frame.Framebuffer
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.pick.PickedObjectList
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.GpuTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

class DrawContext {

    var eyePoint: Vec3 = Vec3()

    var viewport: Viewport = Viewport()

    var modelview: Matrix4 = Matrix4()

    var projection: Matrix4 = Matrix4()

    var modelviewProjection: Matrix4 = Matrix4()

    var screenProjection: Matrix4 = Matrix4()

    var infiniteProjection = Matrix4()

    var drawableQueue: DrawableQueue? = null

    var drawableTerrain: DrawableQueue? = null

    var pickedObjects: PickedObjectList? = null

    var pickPoint: Vec2? = null

    var pickMode = false

    var pickViewport: Viewport? = null

    var framebufferId = 0

    var programId = 0

    var textureUnit = GLES20.GL_TEXTURE0

    var textureId = IntArray(32)

    var arrayBufferId = 0

    var elementArrayBufferId = 0

    var scratchFramebuffer: Framebuffer? = null

    var unitSquareBuffer: BufferObject? = null

    var scratchList = ArrayList<Any>()

    var scratchBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())

    var pixelArray = ByteArray(4)

    fun reset() {
        eyePoint.set(0.0, 0.0, 0.0)
        viewport.setEmpty()
        modelview.setToIdentity()
        projection.setToIdentity()
        modelviewProjection.setToIdentity()
        screenProjection.setToIdentity()
        infiniteProjection.setToIdentity()
        drawableQueue = null
        drawableTerrain = null

        pickViewport = null
        pickedObjects = null
        pickPoint = null
        pickMode = false

        scratchBuffer.clear()
        scratchList.clear()
    }

    fun contextLost() {
        programId = 0
        framebufferId = 0
        textureUnit = GLES20.GL_TEXTURE0
        arrayBufferId = 0
        elementArrayBufferId = 0
        unitSquareBuffer = null
        scratchFramebuffer = null
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

    fun currentFramebuffer(): Int {
        return framebufferId
    }

    fun bindFramebuffer(framebufferId: Int) {
        if (this.framebufferId != framebufferId) {
            this.framebufferId = framebufferId
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId)
        }
    }

    fun scratchFramebuffer(): Framebuffer {
        scratchFramebuffer?.let { return it }
        val framebuffer = Framebuffer()
        val colorAttachment = GpuTexture(
            1024,
            1024,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE
        )
        val depthAttachment =
            GpuTexture(
                1024,
                1024,
                GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT
            )
        depthAttachment.setTexParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        depthAttachment.setTexParameter(GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        framebuffer.attachTexture(this, colorAttachment, GLES20.GL_COLOR_ATTACHMENT0)
        framebuffer.attachTexture(this, depthAttachment, GLES20.GL_DEPTH_ATTACHMENT)
        return framebuffer.also { scratchFramebuffer = it }
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


    fun unitSquareBuffer(): BufferObject {
        unitSquareBuffer?.let {
            return it
        }
        val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f)
        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        val bufferObject = BufferObject(
            GLES20.GL_ARRAY_BUFFER,
            size,
            buffer
        )
        return bufferObject.also { unitSquareBuffer = it }
    }

    fun scratchList(): ArrayList<Any> {
        return scratchList
    }

    /**
     * 在当前活动的OpenGL帧缓冲区中的屏幕点读取片段颜色。 X和Y组件指示OpenGL屏幕坐标，该坐标起源于帧缓冲区的左下角。
     */
    fun readPixelColor(x: Int, y: Int, result_temp: Color?): Color {
        val result = result_temp ?: Color()

        // Read the fragment pixel as an RGBA 8888 color.
        val pixelBuffer = this.scratchBuffer(4).clear() as ByteBuffer
        GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer)
        pixelBuffer.get(this.pixelArray, 0, 4)

        result.red = (pixelArray[0] and 0xFF.toByte()) / 0xFF.toFloat()
        result.green = (pixelArray[1] and 0xFF.toByte()) / 0xFF.toFloat()
        result.blue = (pixelArray[2] and 0xFF.toByte()) / 0xFF.toFloat()
        result.alpha = (pixelArray[3] and 0xFF.toByte()) / 0xFF.toFloat()
        return result
    }

    fun readPixelColors(x: Int, y: Int, width: Int, height: Int): Set<Color> {
        // Read the fragment pixels as a tightly packed array of RGBA 8888 colors.
        val pixelCount = width * height
        val pixelBuffer = scratchBuffer(pixelCount * 4).clear() as ByteBuffer
        GLES20.glReadPixels(
            x,
            y,
            width,
            height,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            pixelBuffer
        )

        val resultSet = HashSet<Color>()
        var result = Color()

        var idx = 0
        while (idx < pixelCount) {
            // Copy each RGBA 888 color from the NIO buffer a heap array in bulk to reduce buffer access overhead.
            pixelBuffer[pixelArray, 0, 4]

            // Convert the RGBA 8888 color to a World Wind color.
            result.red = (pixelArray[0] and 0xFF.toByte()) / 0xFF.toFloat()
            result.green = (pixelArray[1] and 0xFF.toByte()) / 0xFF.toFloat()
            result.blue = (pixelArray[2] and 0xFF.toByte()) / 0xFF.toFloat()
            result.alpha = (pixelArray[3] and 0xFF.toByte()) / 0xFF.toFloat()

            // Accumulate the unique colors in a set.
            if (resultSet.add(result)) {
                result = Color()
            }
            idx++
        }
        return resultSet
    }

    fun scratchBuffer(capacity: Int): ByteBuffer {
        if (scratchBuffer.capacity() < capacity) {
            scratchBuffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
        }
        return scratchBuffer
    }
}