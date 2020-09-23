package com.atom.wyz.worldwind.core.api

interface Renderer {

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
    fun draw(texture: Int)

    /**
     * 渲染并输出
     * @param texture 输入纹理
     */
    fun drawToTexture(texture: Int): Int

    /**
     * 销毁
     */
    fun destroy()
}