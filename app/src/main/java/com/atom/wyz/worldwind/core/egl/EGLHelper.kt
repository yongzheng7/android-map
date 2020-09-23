package com.atom.wyz.worldwind.core.egl

import android.opengl.*


class EGLHelper {

    private var mEGLDisplay: EGLDisplay? = null
    private var mEGLConfig: EGLConfig? = null
    private var mEGLContext: EGLContext? = null
    private var mEGLSurface: EGLSurface? = null

    constructor() : this(EGL14.EGL_DEFAULT_DISPLAY)

    constructor(display: Int) {
        initDisplay(display)
    }

    /**
     * 初始化显示器 display
     */
    fun initDisplay(key: Int) {
        mEGLDisplay = EGL14.eglGetDisplay(key)
        //获取版本号，[0]为版本号，[1]为子版本号
        val versions = IntArray(2)
        EGL14.eglInitialize(mEGLDisplay, versions, 0, versions, 1)
    }

    /**
     * 获取EGL的config
     */
    fun getConfig(attrs: EGLConfigAttrs): EGLConfig? {
        val configs = arrayOfNulls<EGLConfig>(1)
        val configNum = IntArray(1)
        EGL14.eglChooseConfig(mEGLDisplay, attrs.build(), 0, configs, 0, 1, configNum, 0)
        return if (configNum[0] > 0) {  //选择的过程可能出现多个，也可能一个都没有，这里只用一个
            configs[0]
        } else null
    }

    fun getDefaultConfig(): EGLConfig? {
        return mEGLConfig
    }

    fun getDefaultSurface(): EGLSurface? {
        return mEGLSurface
    }

    fun getDefaultContext(): EGLContext? {
        return mEGLContext
    }

    fun getDisplay(): EGLDisplay? {
        return mEGLDisplay
    }

    /**
     * 创建EGL context
     */
    fun createContext(config: EGLConfig?, share: EGLContext?, attrs: EGLContextAttrs): EGLContext {
        return EGL14.eglCreateContext(mEGLDisplay, config, share, attrs.build(), 0)
    }

    /**
     * 创建一个window surface
     */
    fun createWindowSurface(surface: Any): EGLSurface {
        return createWindowSurface(mEGLConfig , surface)
    }

    fun createWindowSurface(config: EGLConfig?, surface: Any): EGLSurface {
        return EGL14.eglCreateWindowSurface(mEGLDisplay, config, surface, intArrayOf(EGL14.EGL_NONE), 0)
    }

    /**
     * 创建一个 后台 surface
     */
    fun createPBufferSurface(config: EGLConfig?, width: Int, height: Int): EGLSurface {
        return EGL14.eglCreatePbufferSurface(mEGLDisplay, config, intArrayOf(EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE), 0)
    }

    /**
     * 创建 EGL config context surface
     */
    fun createGLESWithSurface(attrs: EGLConfigAttrs, ctxAttrs: EGLContextAttrs, surface: Any): Boolean {
        mEGLConfig = getConfig(attrs.surfaceType(EGL14.EGL_WINDOW_BIT))
        if (mEGLConfig == null) {
            return false
        }
        mEGLContext = createContext(mEGLConfig!!, EGL14.EGL_NO_CONTEXT, ctxAttrs)
        if (mEGLContext === EGL14.EGL_NO_CONTEXT) {
            return false
        }
        mEGLSurface = createWindowSurface(surface)
        if (mEGLSurface === EGL14.EGL_NO_SURFACE) {
            return false
        }
        if (!makeCurrent()) {
            return false
        }
        return true
    }

    fun makeCurrent(draw: EGLSurface?, read: EGLSurface?, context: EGLContext?): Boolean {
        return  EGL14.eglMakeCurrent(mEGLDisplay, draw, read, context)
    }

    fun makeCurrent(surface: EGLSurface?, context: EGLContext?): Boolean {
        return makeCurrent(surface, surface, context)
    }

    fun makeCurrent(surface: EGLSurface?): Boolean {
        return makeCurrent(surface, mEGLContext)
    }

    fun makeCurrent(): Boolean {
        return makeCurrent(mEGLSurface, mEGLContext)
    }

    fun setPresentationTime(surface: EGLSurface?, time: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, surface, time)
    }

    /**
     * 创建一个后台EGL surface
     * 和 window surface 的不同的是, 该surface 可以配置其宽高
     */
    fun createGLESWithPBuffer(attrs: EGLConfigAttrs, ctxAttrs: EGLContextAttrs, width: Int, height: Int): EGLSurface? {
        mEGLConfig = getConfig(attrs.surfaceType(EGL14.EGL_PBUFFER_BIT))
        if (mEGLConfig == null) {
            return null
        }
        val eglContext = createContext(mEGLConfig, EGL14.EGL_NO_CONTEXT, ctxAttrs)
        if (eglContext === EGL14.EGL_NO_CONTEXT) {
            return null
        }
        val eglSurface = createPBufferSurface(mEGLConfig, width, height)
        if (eglSurface === EGL14.EGL_NO_SURFACE) {
            return null
        }
        if (!makeCurrent(eglSurface)) {
            return null
        }
        return eglSurface
    }

    fun createGLESWithPBuffer(attrs: EGLConfigAttrs, ctxAttrs: EGLContextAttrs, eglSurface: EGLSurface) : Boolean {
        mEGLConfig = getConfig(attrs.surfaceType(EGL14.EGL_PBUFFER_BIT))
        if (mEGLConfig == null) {
            return false
        }
        val eglContext = createContext(mEGLConfig, EGL14.EGL_NO_CONTEXT, ctxAttrs)
        if (eglContext === EGL14.EGL_NO_CONTEXT) {
            return false
        }
        if (!makeCurrent(eglSurface)) {
            return false
        }
        return true
    }

    fun swapBuffers(surface: EGLSurface?) {
        EGL14.eglSwapBuffers(mEGLDisplay, surface)
    }

    fun destroyGLES(surface: EGLSurface?, context: EGLContext?){
        EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
        if (surface != null) {
            EGL14.eglDestroySurface(mEGLDisplay, surface)
        }
        if (context != null) {
            EGL14.eglDestroyContext(mEGLDisplay, context)
        }
        EGL14.eglTerminate(mEGLDisplay)
    }

    fun destroySurface(surface: EGLSurface?) {
        EGL14.eglDestroySurface(mEGLDisplay, surface)
    }

    fun destroy() {
        EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
        EGL14.eglTerminate(mEGLDisplay)
    }

}