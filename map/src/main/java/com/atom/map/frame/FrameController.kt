package com.atom.map.frame

import com.atom.map.drawable.DrawContext
import com.atom.map.renderable.RenderContext

/**
 * 帧控制
 */
interface FrameController {

    fun renderFrame(rc: RenderContext)

    fun drawFrame(dc: DrawContext)
}