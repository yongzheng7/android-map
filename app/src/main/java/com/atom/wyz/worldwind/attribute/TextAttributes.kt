package com.atom.wyz.worldwind.attribute

import android.graphics.Typeface
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.geom.Offset

class TextAttributes {
    var textColor: SimpleColor

    var textOffset: Offset

    var textSize = 0f

    var typeface: Typeface?

    var enableOutline = false

    var enableDepthTest = false

    var outlineWidth = 0f

    var outlineColor: SimpleColor

    constructor() {
        textColor = SimpleColor(1f, 1f, 1f, 1f)
        outlineColor = SimpleColor(0f, 0f, 0f, 1f)
        textOffset = Offset.bottomCenter()
        textSize = 24f
        typeface = null
        enableOutline = true
        enableDepthTest = true
        outlineWidth = 3f
    }

    constructor(attributes: TextAttributes) {
        textColor = SimpleColor(attributes.textColor)
        outlineColor =
            SimpleColor(attributes.outlineColor)
        textOffset = Offset(attributes.textOffset)
        textSize = attributes.textSize
        typeface = attributes.typeface
        enableOutline = attributes.enableOutline
        enableDepthTest = attributes.enableDepthTest
        outlineWidth = attributes.outlineWidth
    }

    fun set(attributes: TextAttributes): TextAttributes {
        textColor.set(attributes.textColor)
        outlineColor.set(attributes.textColor)
        textOffset.set(attributes.textOffset)
        textSize = attributes.textSize
        typeface = attributes.typeface
        enableOutline = attributes.enableOutline
        enableDepthTest = attributes.enableDepthTest
        outlineWidth = attributes.outlineWidth
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }

        val that = other as TextAttributes
        return (textColor == that.textColor
                && textOffset == that.textOffset
                && textSize == that.textSize
                && (if (typeface == null) that.typeface == null else typeface == that.typeface)
                && enableOutline == that.enableOutline
                && this.outlineColor == that.outlineColor
                && enableDepthTest == that.enableDepthTest
                && outlineWidth == that.outlineWidth)
    }

    override fun hashCode(): Int {
        var result = textColor.hashCode()
        result = 31 * result + textOffset.hashCode()
        result = 31 * result + if (textSize != +0.0f) java.lang.Float.floatToIntBits(textSize) else 0
        result = 31 * result + if (typeface != null) typeface.hashCode() else 0
        result = 31 * result + if (enableOutline) 1 else 0
        result = 31 * result + outlineColor.hashCode()
        result = 31 * result + if (enableDepthTest) 1 else 0
        result = 31 * result + if (outlineWidth != +0.0f) java.lang.Float.floatToIntBits(outlineWidth) else 0
        return result
    }
}