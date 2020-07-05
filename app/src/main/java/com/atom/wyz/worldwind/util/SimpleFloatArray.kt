package com.atom.wyz.worldwind.util

class SimpleFloatArray {
    companion object {
        protected val MIN_CAPACITY_INCREMENT = 12
        protected val EMPTY_ARRAY = FloatArray(0)
    }

    protected lateinit var array: FloatArray

    var size = 0

    constructor(initialCapacity: Int = 0) {
        if (initialCapacity == 0) {
            array = EMPTY_ARRAY
        } else {
            array = FloatArray(initialCapacity)
        }
    }

    fun array(): FloatArray {
        return array
    }

    fun size(): Int {
        return size
    }

    fun get(index: Int): Float {
        return array[index]
    }

    fun set(index: Int, value: Float): SimpleFloatArray {
        array[index] = value
        return this
    }

    fun add(value: Float): SimpleFloatArray {
        val capacity = array.size
        if (capacity == size) {
            val increment = Math.max(capacity shr 1, MIN_CAPACITY_INCREMENT)
            val newArray = FloatArray(capacity + increment)
            System.arraycopy(array, 0, newArray, 0, capacity)
            array = newArray
        }
        array[size++] = value
        return this
    }

    fun trimToSize(): SimpleFloatArray {
        val size = size
        if (size == array.size) {
            return this
        }
        if (size == 0) {
            array = EMPTY_ARRAY
        } else {
            val newArray = FloatArray(size)
            System.arraycopy(array, 0, newArray, 0, size)
            array = newArray
        }
        return this
    }

    fun clear(): SimpleFloatArray {
        array = EMPTY_ARRAY
        size = 0
        return this
    }

}