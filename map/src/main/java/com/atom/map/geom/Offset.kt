package com.atom.map.geom

import com.atom.map.WorldWind

class Offset {

    companion object {

        fun center(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5)
        }

        fun centerLeft(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 0.5)
        }

        fun centerRight(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 0.5)
        }

        fun bottomLeft(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 0.0)
        }

        fun bottomCenter(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0)
        }

        fun bottomRight(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 0.0)
        }

        fun topLeft(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 1.0)
        }

        fun topCenter(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 1.0)
        }

        fun topRight(): Offset {
            return Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 1.0)
        }

        fun negate(res: Offset): Offset {
            val result = Offset(res)
            if (res.xUnits == WorldWind.OFFSET_FRACTION) {
                result.x = 1.0 - res.x
            }
            if (res.yUnits == WorldWind.OFFSET_FRACTION) {
                result.y = 1.0 - res.y
            }
            return result
        }
    }

    var x = 0.0
    var y = 0.0

    @WorldWind.OffsetMode
    var xUnits = 0

    @WorldWind.OffsetMode
    var yUnits = 0


    constructor(
        @WorldWind.OffsetMode xUnits: Int,
        x: Double,
        @WorldWind.OffsetMode yUnits: Int,
        y: Double
    ) {
        this.x = x
        this.y = y
        this.xUnits = xUnits
        this.yUnits = yUnits
    }

    /**
     * Creates a new copy of this offset with identical property values.
     */
    constructor(offset: Offset) {
        set(offset)
    }

    fun set(offset: Offset): Offset {
        x = offset.x
        y = offset.y
        xUnits = offset.xUnits
        yUnits = offset.yUnits
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }

        val that = other as Offset
        return x == that.x && y == that.y && xUnits == that.xUnits && yUnits == that.yUnits
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

    fun offsetForSize(width: Double, height: Double, result: Vec2): Vec2 {
        val x: Double =
            when (xUnits) {
                WorldWind.OFFSET_FRACTION -> {
                    width * this.x
                }
                WorldWind.OFFSET_INSET_PIXELS -> {
                    width - this.x
                }
                else -> { // default to OFFSET_PIXELS
                    this.x
                }
            }
        val y: Double = when (yUnits) {
            WorldWind.OFFSET_FRACTION -> {
                height * this.y
            }
            WorldWind.OFFSET_INSET_PIXELS -> {
                height - this.y
            }
            else -> { // default to OFFSET_PIXELS
                this.y
            }
        }
        return result.set(x, y)
    }


}