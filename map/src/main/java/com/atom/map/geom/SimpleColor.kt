package com.atom.map.geom

import android.graphics.Color
import com.atom.map.util.Logger

class SimpleColor {

    companion object {

        fun fromHex(WorldColor: String): SimpleColor {
            val red = WorldColor.substring(0, 2).toInt(16).toChar()
            val green = WorldColor.substring(2, 4).toInt(16).toChar()
            val blue = WorldColor.substring(4, 6).toInt(16).toChar()
            val alpha = WorldColor.substring(6, 8).toInt(16).toChar()
            return fromBytes(red, green, blue, alpha)
        }

        fun fromByteArray(bytes: CharArray): SimpleColor {
            return SimpleColor(
                bytes[0].toFloat() / 255,
                bytes[1].toFloat() / 255,
                bytes[2].toFloat() / 255,
                bytes[3].toFloat() / 255
            )
        }

        fun fromBytes(
            redByte: Char,
            greenByte: Char,
            blueByte: Char,
            alphaByte: Char
        ): SimpleColor {
            return SimpleColor(
                redByte.toFloat() / 255,
                greenByte.toFloat() / 255,
                blueByte.toFloat() / 255,
                alphaByte.toFloat() / 255
            )
        }

        fun random(): SimpleColor {
            return SimpleColor(
                Math.random().toFloat(),
                Math.random().toFloat(),
                Math.random().toFloat(),
                1f
            )
        }
    }

    var red = 1f

    var green = 1f

    var blue = 1f

    var alpha = 1f

    constructor() : this(1f, 1f, 1f, 1f)

    constructor(red: Float, green: Float, blue: Float, alpha: Float) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }


    constructor(WorldColor: SimpleColor) {
        red = WorldColor.red
        green = WorldColor.green
        blue = WorldColor.blue
        alpha = WorldColor.alpha
    }

    constructor(WorldColorInt: Int) {
        red = Color.red(WorldColorInt) / 0xFF.toFloat()
        green = Color.green(WorldColorInt) / 0xFF.toFloat()
        blue = Color.blue(WorldColorInt) / 0xFF.toFloat()
        alpha = Color.alpha(WorldColorInt) / 0xFF.toFloat()
    }


    fun set(red: Float, green: Float, blue: Float, alpha: Float): SimpleColor {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
        return this
    }

    fun set(WorldColor: SimpleColor): SimpleColor {
        red = WorldColor.red
        green = WorldColor.green
        blue = WorldColor.blue
        alpha = WorldColor.alpha
        return this
    }

    fun set(WorldColorInt: Int): SimpleColor {
        red = Color.red(WorldColorInt) / 0xFF.toFloat()
        green = Color.green(WorldColorInt) / 0xFF.toFloat()
        blue = Color.blue(WorldColorInt) / 0xFF.toFloat()
        alpha = Color.alpha(WorldColorInt) / 0xFF.toFloat()
        return this
    }

    fun toArray(result: FloatArray, offSet: Int): FloatArray {
        var offset = offSet
        if (result.size - offset < 4) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldColor", "toArray", "missingResult")
            )
        }
        result[offset++] = red
        result[offset++] = green
        result[offset++] = blue
        result[offset] = alpha
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (o == null || this::class != o::class) {
            return false
        }
        if (this === o) {
            return true
        }

        val that = o as SimpleColor
        return red == that.red && green == that.green && blue == that.blue && alpha == that.alpha
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(red.toDouble())
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(green.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(blue.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(alpha.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "{red=$red , green=$green , blue=$blue , alpha=$alpha}"
    }

    fun toByteString(): String {
        val rb = Math.round(red * 255)
        val gb = Math.round(green * 255)
        val bb = Math.round(blue * 255)
        val ab = Math.round(alpha * 255)
        return "($rb,$gb,$bb,$ab)"
    }

    fun premultipliedComponents(array: FloatArray): FloatArray {
        array[0] = red * alpha
        array[1] = green * alpha
        array[2] = blue * alpha
        array[3] = alpha
        return array
    }

    fun nextWorldColor(): SimpleColor {
        val rb = Math.round(red * 255)
        val gb = Math.round(green * 255)
        val bb = Math.round(blue * 255)
        if (rb < 255) {
            red = (rb + 1) / 255.toFloat()
        } else if (gb < 255) {
            red = 0f
            green = (gb + 1) / 255.toFloat()
        } else if (bb < 255) {
            red = 0f
            green = 0f
            blue = (bb + 1) / 255.toFloat()
        } else {
            red = 1 / 255.toFloat()
            green = 0f
            blue = 0f
        }
        return this
    }

    fun equalsBytes(bytes: CharArray): Boolean {
        val rb = Math.round(red * 255)
        val gb = Math.round(green * 255)
        val bb = Math.round(blue * 255)
        val ab = Math.round(alpha * 255)
        return rb == bytes[0].toInt() && gb == bytes[1].toInt() && bb == bytes[2].toInt() && ab == bytes[3].toInt()
    }

    fun toColorInt(): Int {
        val r8 = Math.round(red * 0xFF)
        val g8 = Math.round(green * 0xFF)
        val b8 = Math.round(blue * 0xFF)
        val a8 = Math.round(alpha * 0xFF)
        return Color.argb(a8, r8, g8, b8)
    }

    fun premultiply(): SimpleColor {
        red *= alpha
        green *= alpha
        blue *= alpha
        return this
    }

    fun premultiplyColor(WorldColor: SimpleColor): SimpleColor {
        red = WorldColor.red * WorldColor.alpha
        green = WorldColor.green * WorldColor.alpha
        blue = WorldColor.blue * WorldColor.alpha
        alpha = WorldColor.alpha
        return this
    }

    fun premultiplyToArray(result: FloatArray, offsetVal: Int): FloatArray {
        var offset = offsetVal
        require(!(result.size - offset < 4)) {
            Logger.logMessage(
                Logger.ERROR,
                "WorldColor",
                "premultiplyToArray",
                "missingResult"
            )
        }
        result[offset++] = red * alpha
        result[offset++] = green * alpha
        result[offset++] = blue * alpha
        result[offset] = alpha
        return result
    }
}