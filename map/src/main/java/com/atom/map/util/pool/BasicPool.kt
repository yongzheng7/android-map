package com.atom.map.util.pool

open class BasicPool<T>(initialCapacity: Int = 10) : Pool<T> {
    companion object{
        const val MIN_CAPACITY_INCREMENT = 12
    }
    protected var entries: Array<Any?>
    protected var size = 0
    init {
        entries = arrayOfNulls(initialCapacity)
    }
    override fun acquire(): T? {
        if (size > 0) {
            val last = --size
            val instance = entries[last] as T?
            entries[last] = null
            return instance
        }
        return null
    }

    override fun release(instance: T?) {
        if (instance != null) {
            val capacity = entries.size
            if (capacity == size) {
                val increment = Math.max(capacity shr 1, MIN_CAPACITY_INCREMENT)
                val newEntries = arrayOfNulls<Any>(capacity + increment)
                System.arraycopy(entries, 0, newEntries, 0, capacity)
                entries = newEntries
            }
            entries[size++] = instance
        }
    }

    override fun size() : Int = size
}