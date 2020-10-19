package com.atom.map.util

class SimpleShortArray {
    companion object {
        protected val MIN_CAPACITY_INCREMENT = 12
        protected val EMPTY_ARRAY = ShortArray(0)
    }

    protected lateinit var array: ShortArray

    protected var size = 0

    constructor(initialCapacity: Int = 0) {
        if (initialCapacity == 0) {
            array = EMPTY_ARRAY
        } else {
            array = ShortArray(initialCapacity)
        }
    }

    fun array(): ShortArray {
        return array
    }

    fun size(): Int {
        return size
    }

    fun get(index: Int): Short {
        return array[index]
    }

    fun set(index: Int, value: Short): SimpleShortArray {
        array[index] = value
        return this
    }

    fun add(value: Short): SimpleShortArray {
        val capacity = array.size
        if (capacity == size) {
            val increment = Math.max(capacity shr 1, MIN_CAPACITY_INCREMENT)
            val newArray = ShortArray(capacity + increment)
            System.arraycopy(array, 0, newArray, 0, capacity)
            array = newArray
        }
        array[size++] = value
        return this
    }

    fun trimToSize(): SimpleShortArray {
        val size = size
        if (size == array.size) {
            return this
        }
        if (size == 0) {
            array = EMPTY_ARRAY
        } else {
            val newArray = ShortArray(size)
            System.arraycopy(array, 0, newArray, 0, size)
            array = newArray
        }
        return this
    }

    fun clear(): SimpleShortArray {
        array = EMPTY_ARRAY
        size = 0
        return this
    }

}