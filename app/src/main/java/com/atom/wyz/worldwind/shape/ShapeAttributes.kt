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

    var interiorImageSource: ImageSource? = null

    var outlineImageSource: ImageSource? = null

    var depthTest = false

    var drawVerticals = false

    constructor() {
        drawInterior = true
        drawOutline = true
        enableLighting = false
        interiorColor = Color(Color.WHITE)
        outlineColor = Color(Color.RED)
        outlineWidth = 1.0f
        interiorImageSource = null
        outlineImageSource = null
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
        interiorImageSource = attributes.interiorImageSource
        outlineImageSource = attributes.outlineImageSource
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
    }

    fun set(attributes: ShapeAttributes): ShapeAttributes {
        attributes.interiorColor.let { interiorColor.set(it) }
        attributes.outlineColor.let { outlineColor.set(it) }
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        enableLighting = attributes.enableLighting
        outlineWidth = attributes.outlineWidth
        interiorImageSource = attributes.interiorImageSource
        outlineImageSource = attributes.outlineImageSource
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that = other as ShapeAttributes
        return (drawInterior == that.drawInterior && drawOutline == that.drawOutline && drawVerticals == that.drawVerticals && depthTest == that.depthTest && enableLighting == that.enableLighting && interiorColor.equals(
            that.interiorColor
        )
                && outlineColor.equals(that.outlineColor)
                && outlineWidth == that.outlineWidth && (if (interiorImageSource == null) that.interiorImageSource == null else interiorImageSource!!.equals(
            that.interiorImageSource
        ))
                && if (outlineImageSource == null) that.outlineImageSource == null else outlineImageSource!!.equals(
            that.outlineImageSource
        ))
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = if (drawInterior) 1 else 0
        result = 31 * result + if (drawOutline) 1 else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + if (drawVerticals) 1 else 0
        result = 31 * result + if (enableLighting) 1 else 0
        result = 31 * result + interiorColor.hashCode()
        result = 31 * result + outlineColor.hashCode()
        temp = java.lang.Double.doubleToLongBits(outlineWidth.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + if (interiorImageSource != null) interiorImageSource.hashCode() else 0
        result = 31 * result + if (outlineImageSource != null) outlineImageSource.hashCode() else 0
        return result
    }
}