package com.atom.wyz.worldwind.core

import android.opengl.EGLSurface
import android.opengl.GLES20
import android.util.Log
import com.atom.wyz.worldwind.core.api.IObserver
import com.atom.wyz.worldwind.core.api.TextureDrawedListener
import com.atom.wyz.worldwind.core.bean.TextureBean
import com.atom.wyz.worldwind.core.bean.gl.BaseShader
import com.atom.wyz.worldwind.core.bean.gl.LazyShader
import com.atom.wyz.worldwind.core.utils.MatrixUtils.Companion.TYPE_CENTERCROP
import com.atom.wyz.worldwind.core.utils.MatrixUtils.Companion.flip
import com.atom.wyz.worldwind.core.utils.MatrixUtils.Companion.getMatrix

class EarthShower : IObserver<TextureBean> {

    private var mShowSurface: EGLSurface? = null
    private var mFilter: BaseShader? = null

    @Volatile
    private var mSurface: Any? = null
    @Volatile
    private var isShow = false
    private var isRecycle = false

    private var mWidth = 0
    private var mHeight = 0
    private var mMatrixType = TYPE_CENTERCROP
    private var mListener: TextureDrawedListener? = null

    open fun setOutputSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun getWidth() : Int = this.mWidth
    fun getHeight() : Int = this.mHeight

    open fun setSurface(surface: Any?) {
        Log.e("EGLHelper" , "EarthShower  setSurface 1  ${if(surface == null) " null " else " nut null "} ")
        mSurface = surface
    }

    fun recycle(){
        isRecycle  =true
    }

    open fun open() {
        isShow = true
        Log.e("EGLHelper" , "EarthShower  open $isShow ")
    }

    open fun close() {
        isShow = false
    }


    var int = 0 ;
    override fun run(rb: TextureBean) {
        int++
        if(int > 100){
            int = 0
            Log.e("EGLHelper" , "EarthShower run show $isShow  + ${if(mSurface == null) " null " else " not null "}")
        }
        if (rb.endFlag && mShowSurface != null) {
            rb.egl?.destroySurface(mShowSurface)
            mShowSurface = null
        } else if (isShow && mSurface != null && !isRecycle) {
            if (mShowSurface == null) {
                mShowSurface = rb.egl?.createWindowSurface(mSurface!!)
                val lazyFilter = LazyShader()
                lazyFilter.create()
                lazyFilter.sizeChanged(rb.sourceWidth, rb.sourceHeight)
                getMatrix(lazyFilter.getVertexMatrix(), mMatrixType, rb.sourceWidth, rb.sourceHeight, mWidth, mHeight)
                flip(lazyFilter.getVertexMatrix(), false, true)
                mFilter = lazyFilter
            }
            rb.egl?.makeCurrent(mShowSurface)
            GLES20.glViewport(0, 0, mWidth, mHeight)
            mFilter?.draw(rb.textureId)
            mListener?.onDrawEnd(mShowSurface, rb)

            rb.egl?.swapBuffers(mShowSurface)
        }else if (isRecycle) {
            Log.e("FrameShower ${hashCode()}" , "isRecycle $isRecycle")
            isRecycle = false
            mShowSurface?.also {
                rb.egl?.destroySurface(it)
            }
            mShowSurface = null
            mFilter = null
            isShow = false
        }
    }

    /**
     * 设置单帧渲染完成监听器
     * @param listener 监听器
     */
    open fun setOnDrawEndListener(listener: TextureDrawedListener) {
        mListener = listener
    }

}