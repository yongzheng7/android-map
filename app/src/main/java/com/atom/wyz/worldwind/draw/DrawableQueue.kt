package com.atom.wyz.worldwind.draw

import java.util.*

class DrawableQueue {
    protected var entries = arrayOfNulls<Entry>(32)

    protected var size = 0

    protected var position = 0

    protected var backToFrontComparator = object : Comparator<Entry?> {
        override fun compare(lhs: Entry?, rhs: Entry?): Int {
            if (lhs != null && rhs != null) {
                return if (lhs.depth > rhs.depth) { // lhs is farther than rhs; sort lhs before rhs
                    -1
                } else if (lhs.depth < rhs.depth) { // lhs is closer than rhs; sort rhs before lhs
                    1
                } else { // lhs and rhs have the same depth; sort by insertion order
                    lhs.ordinal - rhs.ordinal
                }
            }
            return 0;
        }
    }

    fun DrawableQueue() {}

    fun recycle() {
        var i = 0
        val len = size
        while (i < len) {
            entries[i]!!.recycle()
            i++
        }
        size = 0
        position = 0
    }

    fun offerDrawable(drawable: Drawable, depth: Double) {
        if (entries.size <= size) {
            val newArray = arrayOfNulls<Entry>(size + (size shr 1))
            System.arraycopy(entries, 0, newArray, 0, size)
            entries = newArray
        }
        if (entries[size] == null) {
            entries[size] = Entry()
        }
        entries[size]!![drawable, depth] = size
        size++
    }

    fun peekDrawable(): Drawable? {
        return if (position < size) entries[position]!!.drawable else null
    }

    fun pollDrawable(): Drawable? {
        return if (position < size) entries[position++]!!.drawable else null
    }

    fun rewindDrawables() {
        position = 0
    }

    fun sortBackToFront() {
        Arrays.sort(entries, 0, size, backToFrontComparator)
    }

    protected class Entry {
        var drawable: Drawable? = null
        var depth = 0.0
        var ordinal = 0
        operator fun set(drawable: Drawable?, depth: Double, ordinal: Int) {
            this.drawable = drawable
            this.depth = depth
            this.ordinal = ordinal
        }

        fun recycle() {
            drawable!!.recycle()
            drawable = null
        }
    }
}