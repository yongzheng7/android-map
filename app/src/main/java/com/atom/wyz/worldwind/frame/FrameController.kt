package com.atom.wyz.worldwind.frame

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.RenderContext

/**
 * 帧控制
 */
interface FrameController {

    fun renderFrame(rc: RenderContext)

    fun drawFrame(dc: DrawContext)
}