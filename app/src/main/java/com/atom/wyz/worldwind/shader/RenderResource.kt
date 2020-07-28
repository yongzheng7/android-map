package com.atom.wyz.worldwind.shader

import com.atom.wyz.worldwind.context.DrawContext

interface RenderResource {

    fun release(dc : DrawContext)

}
