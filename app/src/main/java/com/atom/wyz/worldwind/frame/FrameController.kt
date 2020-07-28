package com.atom.wyz.worldwind.frame

import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.context.RenderContext

/**
 * 帧控制
 */
interface FrameController {

    fun renderFrame(rc: RenderContext)

    fun drawFrame(dc: DrawContext)
}