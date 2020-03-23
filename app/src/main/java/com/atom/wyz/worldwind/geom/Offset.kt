package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.WorldWind

class Offset {

    companion object{

        val CENTER: Offset = Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5)
        val BOTTOM_LEFT: Offset = Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 0.0)
        val BOTTOM_CENTER: Offset = Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0)
        val BOTTOM_RIGHT: Offset = Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 0.0)

        fun center(): Offset {
            return CENTER
        }
        fun bottomLeft(): Offset {
            return BOTTOM_LEFT
        }
        fun bottomCenter(): Offset {
            return BOTTOM_CENTER
        }
        fun bottomRight(): Offset {
            return BOTTOM_RIGHT
        }
    }
    /**
     * The offset in the X dimension, interpreted according to this instance's xUnits argument.
     */
    var x = 0.0

    /**
     * The offset in the Y dimension, interpreted according to this instance's yUnits argument.
     */
    var y = 0.0

    /**
     * The units of this instance's X offset. See this class' constructor description for a list of the possible
     * values.
     */
    @WorldWind.OffsetMode
    var xUnits = 0

    /**
     * The units of this instance's Y offset. See this class' constructor description for a list of the possible
     * values.
     */
    @WorldWind.OffsetMode
    var yUnits = 0

    
    constructor(@WorldWind.OffsetMode xUnits: Int, x: Double, @WorldWind.OffsetMode yUnits: Int, y: Double) {
        this.x = x
        this.y = y
        this.xUnits = xUnits
        this.yUnits = yUnits
    }

    /**
     * Creates a new copy of this offset with identical property values.
     */
    constructor(copy: Offset) {
        x = copy.x
        y = copy.y
        xUnits = copy.xUnits
        yUnits = copy.yUnits
    }

    fun set(offset: Offset): Offset {
        x = offset.x
        y = offset.y
        xUnits = offset.xUnits
        yUnits = offset.yUnits
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val offset: Offset = other as Offset
        if (java.lang.Double.compare(offset.x, x) != 0) return false
        if (java.lang.Double.compare(offset.y, y) != 0) return false
        return if (xUnits != offset.xUnits) false else yUnits == offset.yUnits
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        result = 31 * result + xUnits
        result = 31 * result + yUnits
        return result
    }

    override fun toString(): String {
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", xUnits=" + xUnits +
                ", yUnits=" + yUnits +
                '}'
    }

    fun offsetForSize(width: Double, height: Double): Vec2 {
        val x: Double
        val y: Double
        x = if (xUnits == WorldWind.OFFSET_FRACTION) {
            width * this.x
        } else if (xUnits == WorldWind.OFFSET_INSET_PIXELS) {
            width - this.x
        } else { // default to OFFSET_PIXELS
            this.x
        }
        y = if (yUnits == WorldWind.OFFSET_FRACTION) {
            height * this.y
        } else if (yUnits == WorldWind.OFFSET_INSET_PIXELS) {
            height - this.y
        } else { // default to OFFSET_PIXELS
            this.y
        }
        return Vec2(x, y)
    }

}