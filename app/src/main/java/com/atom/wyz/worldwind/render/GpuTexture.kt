package com.atom.wyz.worldwind.render

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath

class GpuTexture : RenderResource {
    companion object {
        protected val UNINITIALIZED_NAME = IntArray(1)
    }

    var textureName: IntArray = UNINITIALIZED_NAME

    var textureWidth = 0

    var textureHeight = 0

    var textureFormat = 0

    var textureByteCount = 0

    var imageBitmap: Bitmap? = null

    var texCoordTransform = Matrix3()

    constructor(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "GpuTexture", "setImage",
                    if (bitmap == null) "missingBitmap" else "invalidBitmap"
                )
            )
        }

        val width = bitmap.width
        val height = bitmap.height
        val format = GLUtils.getInternalFormat(bitmap)
        val type = GLUtils.getType(bitmap)

        textureWidth = width
        textureHeight = height
        textureFormat = format
        textureByteCount = estimateByteCount(width, height, format, type)
        texCoordTransform.setToVerticalFlip()
        imageBitmap = bitmap

    }

    constructor(width: Int, height: Int, format: Int) {
        require(!(width < 0 || height < 0)) {
            Logger.logMessage(
                Logger.ERROR,
                "Texture",
                "constructor",
                "invalidWidthOrHeight"
            )
        }

        textureWidth = width
        textureHeight = height
        textureFormat = format
        textureByteCount = estimateByteCount(width, height, format, GLES20.GL_UNSIGNED_BYTE)
        texCoordTransform.setToIdentity()
    }

    override fun release(dc: DrawContext) {
        if (textureName[0] != 0) {
            deleteTexture(dc)
        }

        if (imageBitmap != null) {
            imageBitmap = null // imageBitmap can be non-null if the texture has never been used
        }
    }

    fun getTextureName(dc: DrawContext): Int {
        if (textureName === UNINITIALIZED_NAME) {
            createTexture(dc)
        }
        return textureName[0]
    }

    fun bindTexture(dc: DrawContext): Boolean {
        if (textureName === UNINITIALIZED_NAME) {
            createTexture(dc)
        }

        if (textureName[0] != 0) {
            dc.bindTexture(textureName[0])
        }

        return textureName[0] != 0
    }

    // bitmap 不为空
    // 三步走 1 创建一个textureid  2 绑定上textureid  3 和bitmap连起来  4 绑定回去

    protected fun createTexture(dc: DrawContext) {
        val currentTexture = dc.currentTexture()
        try {
            textureName = IntArray(1)
            // Create the OpenGL texture 2D object.
            GLES20.glGenTextures(1, textureName, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureName[0])
            // Configure the texture object's filtering modes and wrap modes.
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            // Specify the texture object's image data, either by loading a bitmap or by allocating an empty image.

            imageBitmap ?.let{
                loadTexImage(dc, it)
            } ?:let{
                this.allocTexImage(dc)
            }
            imageBitmap = null
        } finally { // Restore the current OpenGL texture object binding.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currentTexture)
        }
    }

    protected fun allocTexImage(dc: DrawContext) {
        // Allocate texture memory for the OpenGL texture 2D object. The texture memory is initialized with 0.
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0 /*level*/,
            textureFormat, textureWidth, textureHeight,
            0 /*border*/,
            textureFormat, GLES20.GL_UNSIGNED_BYTE, null /*pixels*/
        )
    }

    protected fun deleteTexture(dc: DrawContext) {
        GLES20.glDeleteTextures(1, textureName, 0)
        textureName[0] = 0
    }

    fun loadTexImage(dc: DrawContext, bitmap: Bitmap) {
        try {
            // Specify the OpenGL texture 2D object's base image data (level 0).
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0 /*level*/, bitmap, 0 /*border*/)
            // If the bitmap has power-of-two dimensions, generate the texture object's image data for image levels 1
            // through level N, and configure the texture object's filtering modes to use those image levels.
            val isPowerOfTwo = WWMath.isPowerOfTwo(bitmap.getWidth()) && WWMath.isPowerOfTwo(bitmap.getHeight())
            if (isPowerOfTwo) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR
                )
            }
        } catch (e: Exception) { // The Android utility was unable to load the texture image data.
            Logger.logMessage(
                Logger.ERROR, "Texture", "loadTexImage",
                "Exception attempting to load texture image \'$bitmap\'", e
            )
        }
    }

    protected fun estimateByteCount(width: Int, height: Int, format: Int, type: Int): Int {
        // Compute the number of bytes per row of texture image level 0. Use a default of 32 bits per pixel when either
        // of the bitmap's type or internal format are unrecognized. Adjust the width to the next highest power-of-two
        // to better estimate the memory consumed by non power-of-two images.
        val widthPow2 = WWMath.powerOfTwoCeiling(width)
        var bytesPerRow = widthPow2 * 4
        when (type) {
            GLES20.GL_UNSIGNED_BYTE -> when (format) {
                GLES20.GL_ALPHA -> bytesPerRow = widthPow2 // 8 bits per pixel
                GLES20.GL_RGB -> bytesPerRow = widthPow2 * 3 // 24 bits per pixel
                GLES20.GL_RGBA -> bytesPerRow = widthPow2 * 4 // 32 bits per pixel
                GLES20.GL_LUMINANCE -> bytesPerRow = widthPow2 // 8 bits per pixel
                GLES20.GL_LUMINANCE_ALPHA -> bytesPerRow = widthPow2 * 2 // 16 bits per pixel
            }
            GLES20.GL_UNSIGNED_SHORT_5_6_5,
            GLES20.GL_UNSIGNED_SHORT_4_4_4_4,
            GLES20.GL_UNSIGNED_SHORT_5_5_5_1 -> bytesPerRow = widthPow2 * 2 // 16 bits per pixel
        }

        // Compute the number of bytes for the entire texture image level 0 (i.e. bytePerRow * numRows). Adjust the
        // height to the next highest power-of-two to better estimate the memory consumed by non power-of-two images.
        val heightPow2 = WWMath.powerOfTwoCeiling(height)
        var byteCount = bytesPerRow * heightPow2

        // If the texture will have mipmaps, add 1/3 to account for the bytes used by texture image level 1 through
        // texture image level N.
        val isPowerOfTwo = WWMath.isPowerOfTwo(width) && WWMath.isPowerOfTwo(height)
        if (isPowerOfTwo) {
            byteCount += byteCount / 3
        }

        return byteCount
    }
}