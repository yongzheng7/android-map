package com.atom.map.core.egl

import javax.microedition.khronos.egl.EGL10

class EGLContextAttrs {
    private var version = 2

    fun version(v: Int): EGLContextAttrs {
        version = v
        return this
    }

    fun build(): IntArray {
        return intArrayOf(0x3098, version, EGL10.EGL_NONE)
    }
}