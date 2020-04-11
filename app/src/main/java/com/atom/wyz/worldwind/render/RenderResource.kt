package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext

interface RenderResource {

    fun release(dc : DrawContext)

}
