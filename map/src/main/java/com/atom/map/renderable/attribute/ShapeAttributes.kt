package com.atom.map.renderable.attribute

import android.graphics.Color
import com.atom.map.geom.Offset
import com.atom.map.geom.SimpleColor
import com.atom.map.renderable.ImageSource

class ShapeAttributes {

    companion object {
        private val attributes: ShapeAttributes =
            ShapeAttributes()

        fun defaults(attr: ShapeAttributes = attributes): ShapeAttributes {
            return ShapeAttributes(attr)
        }
    }

    var drawInterior: Boolean

    var drawOutline: Boolean

    var interiorColor: SimpleColor

    var outlineColor: SimpleColor

    var outlineWidth: Float = 0f

    var interiorImageSource: ImageSource? = null

    var outlineImageSource: ImageSource? = null

    var depthTest: Boolean

    /**
     * 向下拉伸的线
     */
    var drawVerticals: Boolean

    var imageOffset: Offset

    private constructor() {
        drawInterior = true
        drawOutline = true
        interiorColor = SimpleColor(Color.WHITE)
        outlineColor = SimpleColor(Color.RED)
        outlineWidth = 1.0f
        interiorImageSource = null
        outlineImageSource = null
        depthTest = true
        drawVerticals = false
        imageOffset = Offset(Offset.centerLeft())
    }

    private constructor(attributes: ShapeAttributes) {
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        interiorColor = SimpleColor(
            attributes.interiorColor
        )
        outlineColor = SimpleColor(
            attributes.outlineColor
        )
        outlineWidth = attributes.outlineWidth
        interiorImageSource = attributes.interiorImageSource
        outlineImageSource = attributes.outlineImageSource
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
        imageOffset = attributes.imageOffset
    }

    fun set(attributes: ShapeAttributes): ShapeAttributes {
        attributes.interiorColor.let { interiorColor.set(it) }
        attributes.outlineColor.let { outlineColor.set(it) }
        drawInterior = attributes.drawInterior
        drawOutline = attributes.drawOutline
        outlineWidth = attributes.outlineWidth
        interiorImageSource = attributes.interiorImageSource
        outlineImageSource = attributes.outlineImageSource
        depthTest = attributes.depthTest
        drawVerticals = attributes.drawVerticals
        imageOffset = attributes.imageOffset
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
        return (drawInterior == that.drawInterior &&
                drawOutline == that.drawOutline &&
                drawVerticals == that.drawVerticals &&
                depthTest == that.depthTest &&
                interiorColor == that.interiorColor &&
                outlineColor == that.outlineColor &&
                outlineWidth == that.outlineWidth &&
                imageOffset == that.imageOffset &&
                (if (interiorImageSource == null) that.interiorImageSource == null else interiorImageSource!! == that.interiorImageSource)
                && if (outlineImageSource == null) that.outlineImageSource == null else outlineImageSource!! == that.outlineImageSource)
    }

    override fun hashCode(): Int {
        var result: Int = if (drawInterior) 1 else 0
        result = 31 * result + if (drawOutline) 1 else 0
        result = 31 * result + if (depthTest) 1 else 0
        result = 31 * result + imageOffset.hashCode()
        result = 31 * result + if (drawVerticals) 1 else 0
        result = 31 * result + interiorColor.hashCode()
        result = 31 * result + outlineColor.hashCode()
        result = 31 * result + if (outlineWidth != 0.0f) outlineWidth.toBits() else 0
        result =
            31 * result + if (interiorImageSource != null) interiorImageSource.hashCode() else 0
        result = 31 * result + if (outlineImageSource != null) outlineImageSource.hashCode() else 0
        return result
    }
}