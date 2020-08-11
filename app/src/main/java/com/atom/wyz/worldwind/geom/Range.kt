package com.atom.wyz.worldwind.geom

class Range {

    var lower: Int = 0

    var upper: Int = 0

    constructor()
    constructor(range: Range) : this(range.lower , range.upper)
    constructor(lower: Int , upper: Int) {
        this.lower = lower
        this.upper = upper
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Range = other as Range
        return lower == that.lower && upper == that.upper
    }

    override fun hashCode(): Int {
        return 31 * lower + upper
    }
    fun length(): Int {
        return if (upper > lower) upper - lower else 0
    }

    fun set(lower: Int, upper: Int): Range {
        this.lower = lower
        this.upper = upper
        return this
    }

    fun set(range: Range) : Range {
        lower = range.lower
        upper = range.upper
        return this
    }

    fun setEmpty(): Range {
        lower = 0
        upper = 0
        return this
    }

    fun isEmpty(): Boolean {
        return lower >= upper
    }
}