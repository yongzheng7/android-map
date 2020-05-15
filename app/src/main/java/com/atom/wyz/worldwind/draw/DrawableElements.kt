package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.geom.Color

class DrawableElements {
    var mode = 0

    var count = 0

    var type = 0

    var offset = 0

    var color: Color = Color()

    var lineWidth = 1f

    operator fun set(mode: Int, count: Int, type: Int, offset: Int): DrawableElements {
        this.mode = mode
        this.count = count
        this.type = type
        this.offset = offset
        return this
    }
}