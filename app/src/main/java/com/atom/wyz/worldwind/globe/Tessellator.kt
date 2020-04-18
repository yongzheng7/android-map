package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.RenderContext

/**
 * 镶嵌器
 */
interface Tessellator {
    fun tessellate(rc : RenderContext)
}