package com.atom.map.drawable

interface Drawable {

    fun draw(dc: DrawContext)

    fun recycle()
}