package com.atom.wyz.worldwind.test

import android.graphics.Point

interface ITextureProvider<T> {
    /**
     * 打开视频流数据源
     */
    fun start(): Point


    /**
     * 获取一帧数据
     * @return 是否最后一帧
     */
    fun frame(): Boolean


    /**
     * 创建
     */
    fun create()

    /**
     * 大小改变
     */
    fun sizeChanged(width: Int, height: Int)

    /**
     * 渲染
     * @param texture 输入纹理
     */
    fun draw()


    /**
     * 获取当前帧时间戳
     * @return 时间戳
     */
    fun getTimeStamp(): Long

    /**
     * 关闭视频流数据源
     */
    fun destroy()
}