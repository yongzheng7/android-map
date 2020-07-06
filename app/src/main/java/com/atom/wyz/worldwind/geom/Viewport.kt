package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Viewport() {
    // 原点在左下角
    var x = 0
    var y = 0
    var width = 0
    var height = 0

    constructor(x: Int, y: Int, width: Int, height: Int) : this() {
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

    fun set(x: Int, y: Int, width: Int, height: Int): Viewport {
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
    /**
     * 判断是否包含某点
     */
    fun contains(x: Int, y: Int): Boolean {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height
    }
    /**
     * 判断是否相交
     */
    fun intersects(viewport: Viewport): Boolean {
        return intersects(viewport.x , viewport.y , viewport.width , viewport.height)
    }
    fun intersects(x: Int, y: Int, width: Int, height: Int): Boolean {
        return this.width > 0 && this.height > 0 && width > 0 && height > 0 && this.x < x + width && x < this.x + this.width && this.y < y + height && y < this.y + this.height
    }
    /**
     * 判断是否相交,若相交则取出相交区域
     */
    fun intersect(viewport: Viewport): Boolean {
        return intersect(viewport.x , viewport.y , viewport.width , viewport.height)
    }
    fun intersect(x: Int, y: Int, width: Int, height: Int): Boolean {
        if (intersects(x , y , width , height)) {
            if (this.x < x) {
                this.width -= x - this.x
                this.x = x
            }
            if (this.y < y) {
                this.height -= y - this.y
                this.y = y
            }
            if (this.x + this.width > x + width) {
                this.width = x + width - this.x
            }
            if (this.y + this.height > y + height) {
                this.height = y + height - this.y
            }
            return true
        }
        return false
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