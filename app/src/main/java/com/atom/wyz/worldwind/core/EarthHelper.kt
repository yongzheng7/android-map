package com.atom.wyz.worldwind.core

import android.view.MotionEvent
import android.view.View
import com.atom.wyz.worldwind.layer.Layer

class EarthHelper : View.OnTouchListener{
    companion object {
        val instance: EarthHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            EarthHelper()
        }

        fun instance() : EarthHelper{
            return instance
        }
    }
    private val mTextureProcessor: EarthProcessor

    private val mEarthProvider: EarthProvider

    private val mShower: EarthShower = EarthShower()

    constructor() {
        //用于预览图像
        // 展示消费
        mShower.setOutputSize(720, 1280) // 设置输出大小 可以后期调整为自定义获取机器的大小
        //用于处理视频图像
        val earthProvider = EarthProvider()
        val videoSurfaceProcessor = EarthProcessor()
        videoSurfaceProcessor.setTextureProvider(earthProvider) // 摄像头提供者
        videoSurfaceProcessor.addObserver(mShower)
        mTextureProcessor = videoSurfaceProcessor
        mEarthProvider = earthProvider
    }



    fun startPreview() {
        mShower.open()
    }

    fun stopPreview() {
        mShower.close()
    }

    fun recyclePreview() {
        mShower.recycle()
    }

    fun isRunning(): Boolean = mTextureProcessor.isRunning()


    fun setSurface(surface: Any?) {
        mShower.setSurface(surface)
    }

    fun setPreviewSize(width: Int, height: Int) {
        mShower.setOutputSize(width, height)
    }

    fun addLayer(layer : Layer){
        mEarthProvider.layers.addLayer(layer)
    }

    fun open() {
        if(!mTextureProcessor.isRunning()){
            mTextureProcessor.start()
        }
    }

    fun close() {
        if(mTextureProcessor.isRunning()){
            mShower.recycle()
            mTextureProcessor.stop()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mEarthProvider.onTouch(v  ,event)
    }

}