package com.atom.map.globe

import com.atom.map.layer.render.RenderContext

/**
 * 镶嵌器
 */
interface Tessellator {
    fun tessellate(rc : RenderContext)
}