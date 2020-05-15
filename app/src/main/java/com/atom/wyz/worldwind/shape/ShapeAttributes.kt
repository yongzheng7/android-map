package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.render.ImageSource

class ShapeAttributes {

    var drawInterior = false

    var drawOutline = false

    var enableLighting = false

    var interiorColor: Color

    var outlineColor: Color

    var outlineWidth = 0f

    var outlineStippleFactor = 0

    var outlineStipplePattern: Short = 0

    var imageSource: ImageSource? = null

    var depthTest = false

    var drawVerticals = false

    constructor() {
        drawInterior = true
        drawOutline = true
        enableLighting = false
        interiorColor = Color(Color.WHITE)
        outlineColor = Color( Color.RED)
        outlineWidth = 1.0f
        outlineStippleFactor = 0
        outlineStipplePattern = 0xF0F0.toShort()
        imageSource = null
        depthTest = true
        drawVerticals = false
    }

    constructor(attributes: ShapeAttributes) {
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        enableLighting = attributes.enableLighting
        interiorColor = attributes.interiorColor.let { Color(it) }
        outlineColor = attributes.outlineColor.let { Color(it) }
        outlineWidth = attributes.outlineWidth
        outlineStippleFactor = attributes.outlineStippleFactor
        outlineStipplePattern = attributes.outlineStipplePattern
        imageSource = attributes.imageSource
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
    }

    fun set(attributes: ShapeAttributes): ShapeAttributes {
        imageSource = attributes.imageSource
        attributes.interiorColor.let { interiorColor.set(it) }
        attributes.outlineColor.let { outlineColor.set(it) }
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        enableLighting = attributes.enableLighting
        outlineWidth = attributes.outlineWidth
        outlineStippleFactor = attributes.outlineStippleFactor
        outlineStipplePattern = attributes.outlineStipplePattern
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that: ShapeAttributes = other as ShapeAttributes
        if (drawInterior != that.drawInterior) return false
        if (drawOutline != that.drawOutline) return false
        if (enableLighting != that.enableLighting) return false
        if (java.lang.Double.compare(that.outlineWidth.toDouble(), outlineWidth.toDouble()) != 0) return false
        if (outlineStippleFactor != that.outlineStippleFactor) return false
        if (outlineStipplePattern != that.outlineStipplePattern) return false
        if (depthTest != that.depthTest) return false
        if (drawVerticals != that.drawVerticals) return false
        if (interiorColor != that.interiorColor) return false
        return if (outlineColor != that.outlineColor ) false else !if (imageSource != null) imageSource != that.imageSource else that.imageSource != null
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (drawInterior) 1 else 0
        result = 31 * result + if (drawOutline) 1 else 0
        result = 31 * result + if (enableLighting) 1 else 0
        result = 31 * result + interiorColor.hashCode()
        result = 31 * result + outlineColor.hashCode()
        temp = java.lang.Double.doubleToLongBits(outlineWidth.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + outlineStippleFactor
        result = 31 * result + outlineStipplePattern.toInt()
        result = 31 * result + if (imageSource != null) imageSource.hashCode() else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + if (drawVerticals) 1 else 0
        return result
    }
}