package com.atom.map.core.shader

import com.atom.map.drawable.DrawContext

interface RenderResource {

    fun release(dc : DrawContext)

}
