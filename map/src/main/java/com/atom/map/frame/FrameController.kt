package com.atom.map.frame

import com.atom.map.layer.draw.DrawContext
import com.atom.map.layer.render.RenderContext

/**
 * 帧控制
 */
interface FrameController {

    fun renderFrame(rc: RenderContext)

    fun drawFrame(dc: DrawContext)
}