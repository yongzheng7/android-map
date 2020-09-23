package com.atom.wyz.worldwind.core.shader

import com.atom.wyz.worldwind.layer.draw.DrawContext

interface RenderResource {

    fun release(dc : DrawContext)

}
