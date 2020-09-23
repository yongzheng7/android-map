package com.atom.wyz.worldwind.core.api

import android.opengl.EGLSurface
import com.atom.wyz.worldwind.core.bean.TextureBean

interface TextureDrawedListener{
    /**
     * 渲染完成通知
     * @param surface 渲染的目标EGLSurface
     * @param bean 渲染用的资源
     */
    fun onDrawEnd(surface: EGLSurface?, bean: TextureBean)
}