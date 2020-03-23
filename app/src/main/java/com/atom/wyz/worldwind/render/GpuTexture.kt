package com.atom.wyz.worldwind.render

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath

class GpuTexture : RenderResource {

    var textureId = 0

    var textureWidth = 0

    var textureHeight = 0

    var textureFormat = 0


    var imageWidth = 0

    var imageHeight = 0

    var imageFormat = 0

    var imageByteCount = 0

    var imageBitmap: Bitmap? = null

    fun setBitmap(value : Bitmap?){
        if (value == null || value.isRecycled) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GpuTexture", "setImage", "invalidBitmap"))
        }
        imageWidth = value.getWidth()
        imageHeight = value.getHeight()
        imageFormat = GLUtils.getInternalFormat(value)
        imageByteCount = value.getByteCount()
        imageBitmap = value
    }

    constructor()

    constructor(bitmap: Bitmap?) {
        setBitmap(bitmap) ;
    }

    override fun release(dc: DrawContext) {
        this.deleteTexture(dc)
        imageBitmap = null
    }

    fun recycle(){
        imageBitmap ?.let {
            if(!it.isRecycled){
                it.recycle()
            }
        }
        imageBitmap = null ;
    }

    fun hasTexture(): Boolean {
        return textureId != 0 || imageBitmap != null
    }

    fun bindTexture(dc: DrawContext): Boolean {
        if (imageBitmap != null) {
            this.loadImageBitmap(dc)
            imageBitmap = null
        }
        if (textureId != 0) {
            dc.bindTexture(this.textureId)
        }
        return textureId != 0
    }

    protected fun loadImageBitmap(dc: DrawContext) {
        val currentTexture = dc.currentTexture()
        try {
            if (textureId == 0) {
                this.createTexture(dc)
            }
            dc.bindTexture(textureId)
            this.loadTexImage(dc)
        } catch (e: java.lang.Exception) {
            this.deleteTexture(dc)
            Logger.logMessage(Logger.ERROR, "GpuTexture", "loadTexImage", "Exception attempting to load texture image", e)
        } finally {
            dc.bindTexture(currentTexture)
        }
    }

    protected fun createTexture(dc: DrawContext) {
        val newTexture = IntArray(1)
        GLES20.glGenTextures(1, newTexture, 0)
        textureId = newTexture[0]
    }

    protected fun deleteTexture(dc: DrawContext) {
        if (textureId != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
    }

    fun loadTexImage(dc: DrawContext) {
        val isPowerOfTwo = WWMath.isPowerOfTwo(this.imageWidth) && WWMath.isPowerOfTwo(this.imageHeight)
        if (textureWidth != imageWidth || textureHeight != imageHeight || textureFormat != imageFormat) {
            textureWidth = imageWidth
            textureHeight = imageHeight
            textureFormat = imageFormat
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, if (isPowerOfTwo) GLES20.GL_LINEAR_MIPMAP_LINEAR else GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, imageBitmap, 0)
        } else {
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, imageBitmap)
        }
        if (isPowerOfTwo) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        }
    }

    fun applyTexCoordTransform(result: Matrix3?): Boolean {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GpuTexture", "applyTexCoordTransform", "missingResult"))
        }
        if (textureId != 0) {
            result.multiplyByVerticalFlip()
        }
        return textureId != 0
    }
}