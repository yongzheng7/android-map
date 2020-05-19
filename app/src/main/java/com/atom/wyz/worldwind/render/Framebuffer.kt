package com.atom.wyz.worldwind.render

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext

class Framebuffer : RenderResource {
    companion object{
        protected val UNINITIALIZED_NAME = IntArray(1)
    }

    var framebufferName = UNINITIALIZED_NAME



    override fun release(dc: DrawContext) {
        if (framebufferName[0] != 0) {
            this.deleteFramebuffer(dc)
        }
    }

    fun bindFramebuffer(dc: DrawContext): Boolean {
        if (framebufferName === UNINITIALIZED_NAME) {
            this.createFramebuffer(dc)
        }
        if (framebufferName[0] != 0) {
            dc.bindFramebuffer(framebufferName[0])
        }
        return framebufferName[0] != 0
    }

    fun attachTexture(dc: DrawContext, texture: GpuTexture): Boolean {
        if (framebufferName === UNINITIALIZED_NAME) {
            this.createFramebuffer(dc)
        }
        if (framebufferName[0] != 0) {
            this.framebufferTexture(dc, texture)
        }
        return framebufferName[0] != 0
    }

    fun isFramebufferComplete(dc: DrawContext): Boolean { // Get the OpenGL framebuffer object status code.
        val e: Int = this.framebufferStatus(dc)
        return e == GLES20.GL_FRAMEBUFFER_COMPLETE
    }

    protected fun createFramebuffer(dc: DrawContext) {
        val currentFramebuffer = dc.currentFramebuffer()
        try {
            framebufferName = IntArray(1)
            // Create the OpenGL framebuffer object.
            GLES20.glGenFramebuffers(1, framebufferName, 0)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferName[0])
        } finally { // Restore the current OpenGL framebuffer object binding.
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currentFramebuffer)
        }
    }

    protected fun deleteFramebuffer(dc: DrawContext) {
        GLES20.glDeleteFramebuffers(1, framebufferName, 0)
        framebufferName[0] = 0
    }

    protected fun framebufferTexture(dc: DrawContext, texture: GpuTexture?) {
        val currentFramebuffer = dc.currentFramebuffer()
        try { // Make the OpenGL framebuffer object the currently active framebuffer.
            dc.bindFramebuffer(framebufferName[0])
            // Configure the texture as the framebuffer object's color attachment, or remove any color attachment if
            // the texture is null.
            val textureName = if (texture != null) texture.getTextureName(dc) else 0
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                textureName, 0 /*level*/
            )
        } finally { // Restore the current OpenGL framebuffer object binding.
            dc.bindFramebuffer(currentFramebuffer)
        }
    }

    protected fun framebufferStatus(dc: DrawContext): Int {
        val currentFramebuffer = dc.currentFramebuffer()
        return try { // Make the OpenGL framebuffer object the currently active framebuffer.
            dc.bindFramebuffer(framebufferName[0])
            // Get the OpenGL framebuffer object status code.
            GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        } finally { // Restore the current OpenGL framebuffer object binding.
            dc.bindFramebuffer(currentFramebuffer)
        }
    }
}