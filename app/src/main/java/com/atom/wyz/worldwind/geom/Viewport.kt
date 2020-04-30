package com.atom.wyz.worldwind.geom

class Viewport() {
    /**
     * The X component of the viewport's origin.
     */
    var x = 0

    /**
     * The Y component of the viewport's origin.
     */
    var y = 0

    /**
     * The viewport's width.
     */
    var width = 0

    /**
     * The viewport's height.
     */
    var height = 0

    constructor(x: Int, y: Int, width: Int, height: Int):this() {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun Viewport(viewport: Viewport) {
        x = viewport.x
        y = viewport.y
        width = viewport.width
        height = viewport.height
    }

    operator fun set(x: Int, y: Int, width: Int, height: Int): Viewport {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        return this
    }

    fun set(viewport: Viewport): Viewport {
        x = viewport.x
        y = viewport.y
        width = viewport.width
        height = viewport.height
        return this
    }

    fun setEmpty(): Viewport {
        width = 0
        height = 0
        return this
    }

    fun isEmpty(): Boolean {
        return width <= 0 || height <= 0
    }

    fun intersects(viewport: Viewport): Boolean {
        val that: Viewport = viewport
        return x < that.x + that.width && that.x < x + width && y < that.y + that.height && that.y < y + height
    }

    fun contains(x: Int, y: Int): Boolean {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that = other as Viewport
        return x == that.x && y == that.y && width == that.width && height == that.height
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x.toDouble())
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(width.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(height.toDouble())
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "Viewport{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}'
    }
}