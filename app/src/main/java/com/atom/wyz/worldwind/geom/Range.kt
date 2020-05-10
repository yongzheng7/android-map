package com.atom.wyz.worldwind.geom

class Range(
    /**
     * The range's upper bound, inclusive.
     * 范围的上限（含）。
     */
    var upper: Int,
    /**
     * The range's lower bound, inclusive.
     * 范围的下限（含）。
     */
    var lower: Int
) {

    constructor() : this(0, 0)
    constructor(range: Range) : this(range.upper, range.lower)


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