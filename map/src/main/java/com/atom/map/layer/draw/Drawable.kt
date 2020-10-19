package com.atom.map.layer.draw

interface Drawable {

    fun draw(dc: DrawContext)

    fun recycle()
}