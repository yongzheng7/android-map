package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.DrawContext

/**
 * 镶嵌器
 */
interface Tessellator {
    fun tessellate(dc : DrawContext)
}