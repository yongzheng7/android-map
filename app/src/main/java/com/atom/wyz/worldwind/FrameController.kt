package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.render.DrawContext

/**
 * 帧控制
 */
interface FrameController {
    var frameStatistics : FrameStatistics


    fun drawFrame(dc: DrawContext)
}