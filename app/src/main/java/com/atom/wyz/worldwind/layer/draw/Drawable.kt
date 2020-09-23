package com.atom.wyz.worldwind.layer.draw

interface Drawable {

    fun draw(dc: DrawContext)

    fun recycle()
}