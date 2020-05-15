package com.atom.wyz.worldwind.shape

import android.graphics.Typeface
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Offset

class TextAttributes {
    var color: Color? = null

    var font: Typeface? = null

    var offset: Offset? = null

    var scale = 0.0

    protected var depthTest = false

    constructor() {
        color = Color(1f, 1f, 1f, 1f)
        font = Typeface.DEFAULT
        offset = Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0)
        scale = 1.0
        depthTest = false
    }

    constructor(attributes: TextAttributes) {
        color = attributes.color?.let { Color(it) }
        font = attributes.font
        offset = attributes.offset?.let { Offset(it) }
        scale = attributes.scale
        depthTest = attributes.depthTest
    }

    fun set(attributes: TextAttributes): TextAttributes? {
        attributes.color?.let { color!!.set(it) }
        font = attributes.font
        attributes.offset?.let { offset!!.set(it) }
        scale = attributes.scale
        depthTest = attributes.depthTest
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that: TextAttributes = other as TextAttributes
        if (java.lang.Double.compare(that.scale, scale) != 0) return false
        if (depthTest != that.depthTest) return false
        if (if (color != null) color != that.color else that.color != null) return false
        return if (if (font != null) font != that.font else that.font != null) false else !if (offset != null) offset != that.offset else that.offset != null
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (color != null) color.hashCode() else 0
        result = 31 * result + if (font != null) font.hashCode() else 0
        result = 31 * result + if (offset != null) offset.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(scale)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (depthTest) 1 else 0
        return result
    }
}