package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Color {

    companion object {
        /**
         * The color white.
         */
        val WHITE: Color = Color(1f, 1f, 1f, 1f)

        /**
         * The color black.
         */
        val BLACK: Color = Color(0f, 0f, 0f, 1f)

        /**
         * The color red.
         */
        val RED: Color = Color(1f, 0f, 0f, 1f)

        /**
         * The color green.
         */
        val GREEN: Color = Color(0f, 1f, 0f, 1f)

        /**
         * The color blue.
         */
        val BLUE: Color = Color(0f, 0f, 1f, 1f)

        /**
         * The color cyan.
         */
        val CYAN: Color = Color(0f, 1f, 1f, 1f)

        /**
         * The color yellow.
         */
        val YELLOW: Color = Color(1f, 1f, 0f, 1f)

        /**
         * The color magenta.
         */
        val MAGENTA: Color = Color(1f, 0f, 1f, 1f)

        /**
         * A light gray (75% white).
         */
        val LIGHT_GRAY: Color = Color(0.75f, 0.75f, 0.75f, 1f)

        /**
         * A medium gray (50% white).
         */
        val MEDIUM_GRAY: Color = Color(0.5f, 0.5f, 0.5f, 1f)

        /**
         * A dark gray (25% white).
         */
        val DARK_GRAY: Color = Color(0.25f, 0.25f, 0.25f, 1f)

        /**
         * A transparent color.
         */
        val TRANSPARENT: Color = Color(0f, 0f, 0f, 0f)


        fun fromHex(color: String): Color? {
            val red = color.substring(0, 2).toInt(16).toChar()
            val green = color.substring(2, 4).toInt(16).toChar()
            val blue = color.substring(4, 6).toInt(16).toChar()
            val alpha = color.substring(6, 8).toInt(16).toChar()
            return Color.fromBytes(red, green, blue, alpha)
        }

        fun fromByteArray(bytes: CharArray): Color? {
            return Color(bytes[0].toFloat() / 255, bytes[1].toFloat() / 255, bytes[2].toFloat() / 255, bytes[3].toFloat() / 255)
        }


        fun fromBytes(redByte: Char, greenByte: Char, blueByte: Char, alphaByte: Char): Color? {
            return Color(redByte.toFloat() / 255, greenByte.toFloat() / 255, blueByte.toFloat() / 255, alphaByte.toFloat() / 255)
        }

        fun random(): Color {
            return Color(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat(), 1f)
        }
    }

    var red = 1f

    var green = 1f

    var blue = 1f

    var alpha = 1f

    constructor():this(0f,0f,0f,0f)

    constructor(red: Float, green: Float, blue: Float, alpha: Float) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }


    constructor(color: Color) {
        red = color.red
        green = color.green
        blue = color.blue
        alpha = color.alpha
    }

    constructor(colorInt: Int) {
        red = android.graphics.Color.red(colorInt) / 0xFF.toFloat()
        green = android.graphics.Color.green(colorInt) / 0xFF.toFloat()
        blue = android.graphics.Color.blue(colorInt) / 0xFF.toFloat()
        alpha = android.graphics.Color.alpha(colorInt) / 0xFF.toFloat()
    }


    operator fun set(red: Float, green: Float, blue: Float, alpha: Float): Color {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
        return this
    }

    fun set(color: Color): Color {
        red = color.red
        green = color.green
        blue = color.blue
        alpha = color.alpha
        return this
    }

    fun set(colorInt: Int): Color {
        red = android.graphics.Color.red(colorInt) / 0xFF.toFloat()
        green = android.graphics.Color.green(colorInt) / 0xFF.toFloat()
        blue = android.graphics.Color.blue(colorInt) / 0xFF.toFloat()
        alpha = android.graphics.Color.alpha(colorInt) / 0xFF.toFloat()
        return this
    }

    fun toArray(result: FloatArray?, offSet: Int): FloatArray? {
        var offset = offSet
        if (result == null || result.size - offset < 4) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Color", "toArray", "missingResult"))
        }
        result[offset++] = red
        result[offset++] = green
        result[offset++] = blue
        result[offset] = alpha
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Color = other as Color
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

    fun premultipliedComponents(array: FloatArray): FloatArray? {
        array[0] = red * alpha
        array[1] = green * alpha
        array[2] = blue * alpha
        array[3] = alpha
        return array
    }

    fun nextColor(): Color {
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
        return android.graphics.Color.argb(a8, r8, g8, b8)
    }

    fun premultiply(): Color {
        red *= alpha
        green *= alpha
        blue *= alpha
        return this
    }

    fun premultiplyColor(color: Color): Color {
        red = color.red * color.alpha
        green = color.green * color.alpha
        blue = color.blue * color.alpha
        alpha = color.alpha
        return this
    }

    fun premultiplyToArray(result: FloatArray, offsetVal: Int): FloatArray {
        var offset = offsetVal
        require(!(result.size - offset < 4)) {
            Logger.logMessage(
                Logger.ERROR,
                "Color",
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