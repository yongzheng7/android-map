package com.atom.wyz.worldwind.draw

import java.util.*

class DrawableList {

    protected var drawables = ArrayList<Drawable>()

    fun DrawableList() {}

    fun count(): Int {
        return drawables.size
    }

    fun offerDrawable(drawable: Drawable?) {
        if (drawable != null) {
            drawables.add(drawable)
        }
    }

    fun getDrawable(index: Int): Drawable? {
        return if (index < drawables.size) drawables[index] else null
    }

    fun clearDrawables() {
        var idx = 0
        val len = drawables.size
        while (idx < len) {
            drawables[idx].recycle()
            idx++
        }
        drawables.clear()
    }
}