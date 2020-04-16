package com.atom.wyz.worldwind.render

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath

class GpuTexture : RenderResource {

    var textureId = IntArray(1)

    var textureWidth = 0

    var textureHeight = 0

    var textureFormat = 0


    var imageWidth = 0

    var imageHeight = 0

    var imageFormat = 0

    var imageByteCount = 0

    var imageBitmap: Bitmap? = null

    var texCoordTransform = Matrix3()

    fun setBitmap(bitmap : Bitmap?){
        if (bitmap == null || bitmap.isRecycled) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GpuTexture", "setImage", "invalidBitmap"))
        }
        imageWidth = bitmap.getWidth()
        imageHeight = bitmap.getHeight()
        imageFormat = GLUtils.getInternalFormat(bitmap)
        imageByteCount = bitmap.getByteCount()
        imageBitmap = bitmap
        texCoordTransform.setToVerticalFlip()
    }

    constructor(bitmap: Bitmap?) {
        setBitmap(bitmap) ;
    }

    override fun release(dc: DrawContext) {
        this.deleteTexture(dc)
        recycle()
        //imageBitmap = null
    }

    fun recycle(){
        imageBitmap ?.let {
            if(!it.isRecycled){
                it.recycle()
            }
        }
        imageBitmap = null
    }

    fun hasTexture(): Boolean {
        return textureId[0] != 0 || imageBitmap != null
    }

    fun bindTexture(dc: DrawContext): Boolean {
        if (imageBitmap != null) {
            this.loadImageBitmap(dc)
            imageBitmap = null // TODO 换成回收
        }
        if (textureId[0] != 0) {
            dc.bindTexture(this.textureId[0])
        }
        return textureId[0] != 0
    }
    // bitmap 不为空
    // 三步走 1 创建一个textureid  2 绑定上textureid  3 和bitmap连起来  4 绑定回去
    protected fun loadImageBitmap(dc: DrawContext) {
        val currentTexture = dc.currentTexture()
        try {
            if (textureId[0] == 0) {
                this.createTexture(dc)
            }
            dc.bindTexture(textureId[0])
            this.loadTexImage(dc)
        } catch (e: java.lang.Exception) {
            this.deleteTexture(dc)
            Logger.logMessage(Logger.ERROR, "GpuTexture", "loadTexImage", "Exception attempting to load texture image", e)
        } finally {
            dc.bindTexture(currentTexture)
        }
    }

    protected fun createTexture(dc: DrawContext) {
        GLES20.glGenTextures(1, textureId, 0)
    }

    protected fun deleteTexture(dc: DrawContext) {
        if (textureId[0] != 0) {
            GLES20.glDeleteTextures(1, textureId, 0)
            textureId[0] = 0
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
}