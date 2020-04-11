package com.atom.wyz.worldwind.frame

import com.atom.wyz.worldwind.DrawContext

/**
 * 帧控制
 */
interface FrameController {

    fun renderFrame(dc: DrawContext)

    fun drawFrame(dc: DrawContext)
}