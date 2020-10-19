package com.atom.map.core.shader

import com.atom.map.layer.draw.DrawContext

interface RenderResource {

    fun release(dc : DrawContext)

}
