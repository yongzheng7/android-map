package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.render.DrawContext

/**
 * 镶嵌器
 */
interface Tessellator {
    fun tessellate(dc : DrawContext) : Terrain? ;
}