package com.atom.wyz.worldwind.core

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES20
import android.util.Log
import com.atom.wyz.worldwind.core.api.IObserver
import com.atom.wyz.worldwind.core.api.ITextureProvider
import com.atom.wyz.worldwind.core.api.Observable
import com.atom.wyz.worldwind.core.bean.FrameBuffer
import com.atom.wyz.worldwind.core.bean.TextureBean
import com.atom.wyz.worldwind.core.egl.EGLConfigAttrs
import com.atom.wyz.worldwind.core.egl.EGLContextAttrs
import com.atom.wyz.worldwind.core.egl.EGLHelper

class EarthProcessor {

    private var mGLThreadFlag = false

    private var mGLThread: Thread? = null

    private val observable: Observable<TextureBean> = Observable()

    private var mProvider: ITextureProvider<TextureBean>? = null


    private val LOCK = Object()

    fun setTextureProvider(provider: ITextureProvider<TextureBean>) {
        mProvider = provider
    }

    fun isRunning(): Boolean = mGLThreadFlag

    fun start() {
        synchronized(LOCK) {
            if (!mGLThreadFlag) {
                if (mProvider == null) {
                    return
                }
                mGLThreadFlag = true
                mGLThread = Thread(Runnable { glRun() })
                mGLThread?.start()
                try {
                    LOCK.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {
        synchronized(LOCK) {
            if (mGLThreadFlag) {
                mGLThreadFlag = false
                mProvider?.destroy()
                try {
                    LOCK.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun glRun() {
        Log.e("EGLHelper" , "EarthProcessor glRun 1 ")
        val iTextureProvider = mProvider ?: let {
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val egl = EGLHelper()
        val ret = egl.createGLESWithSurface(EGLConfigAttrs(), EGLContextAttrs(), SurfaceTexture(1))
        if (!ret) {
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val size = iTextureProvider.start()
        if (size.x <= 0 || size.y <= 0) {
            destroyGL(egl)
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val mSourceWidth = size.x
        val mSourceHeight = size.y
        synchronized(LOCK) { LOCK.notifyAll() }
        //要求数据源提供者必须同步返回数据大小
        if (mSourceWidth <= 0 || mSourceHeight <= 0) {
            destroyGL(egl)
            return
        }
        iTextureProvider.create()
        iTextureProvider.sizeChanged(mSourceWidth, mSourceHeight)
        Log.e("EGLHelper" , "EarthProcessor glRun 2 ")

        //用于其他的回调
        val rb = TextureBean()
        rb.egl = egl
        rb.sourceWidth = mSourceWidth
        rb.sourceHeight = mSourceHeight
        rb.endFlag = false
        rb.threadId = Thread.currentThread().id
        val sourceFrame = FrameBuffer()
        //要求数据源必须同步填充SurfaceTexture，填充完成前等待
        while (!iTextureProvider.frame() && mGLThreadFlag) {
            sourceFrame.bindFrameBuffer(mSourceWidth, mSourceHeight)
            GLES20.glViewport(0, 0, mSourceWidth, mSourceHeight)
            iTextureProvider.draw()
            sourceFrame.unBindFrameBuffer()
            //接收数据源传入的时间戳
            Log.e("EGLHelper" , "EarthProcessor glRun 3 ${sourceFrame.cacheTextureId}")
            rb.textureId = sourceFrame.cacheTextureId
            rb.timeStamp = iTextureProvider.getTimeStamp()
            rb.textureTime = System.currentTimeMillis()
            observable.notify(rb)
        }
        synchronized(LOCK) {
            rb.endFlag = true
            observable.notify(rb)
            iTextureProvider.destroy()
            destroyGL(egl)
            LOCK.notifyAll()
        }
    }


    private fun destroyGL(egl: EGLHelper) {
        mGLThreadFlag = false
        EGL14.eglMakeCurrent(
            egl.getDisplay(),
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroyContext(egl.getDisplay(), egl.getDefaultContext())
        EGL14.eglTerminate(egl.getDisplay())
    }

    fun addObserver(observer: IObserver<TextureBean>) {
        observable.addObserver(observer)
    }
}