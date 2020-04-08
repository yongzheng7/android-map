package com.atom.wyz.worldwind.draw

import java.util.*

class DrawableQueue {
    protected var entries = arrayOfNulls<Entry>(32)

    protected var size = 0

    protected var position = 0

    protected var sortComparator = object : Comparator<Entry?> {
        override fun compare(lhs: Entry?, rhs: Entry?): Int {
            if (lhs != null && rhs != null) {
                if (lhs.groupId < rhs.groupId) { // lhs group is first; sort lhs before rhs
                    -1
                } else if (lhs.groupId > rhs.groupId) { // rhs group is first; sort rhs before lhs
                    1
                } else if (lhs.depth > rhs.depth) { // lhs is farther than rhs; sort lhs before rhs
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

    fun offerDrawable(drawable: Drawable, groupId: Int, depth: Double) {
        if (entries.size <= size) {
            val newArray = arrayOfNulls<Entry>(size + (size shr 1))
            System.arraycopy(entries, 0, newArray, 0, size)
            entries = newArray
        }
        if (entries[size] == null) {
            entries[size] = Entry()
        }
        entries[size]!!.set(drawable, groupId, depth, size)
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

    fun clearDrawables() {
        var i = 0
        val len = size
        while (i < len) {
            entries[i]!!.recycle()
            i++
        }
        size = 0
        position = 0
    }

    fun sortDrawables() {
        Arrays.sort(entries, 0, size, sortComparator)
    }

    protected class Entry {
        var drawable: Drawable? = null
        var depth = 0.0
        var groupId = 0
        var ordinal = 0
        operator fun set(drawable: Drawable?, groupId: Int, depth: Double, ordinal: Int) {
            this.drawable = drawable
            this.groupId = groupId
            this.depth = depth
            this.ordinal = ordinal
        }

        fun recycle() {
            drawable!!.recycle()
            drawable = null
        }
    }
}