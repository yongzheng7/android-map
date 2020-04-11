package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.DrawContext

interface Drawable {
    fun draw(dc: DrawContext)
    fun recycle()
}