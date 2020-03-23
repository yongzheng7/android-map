package com.atom.wyz.worldwind.util

import java.util.*
import kotlin.Comparator

open class LruMemoryCache<K, V> {

    class Entry<K, V>(val key: K, val value: V, val size: Int) {
        var lastUsed: Long = 0
    }

    protected val entries = hashMapOf<K, Entry<K, V>>()

    protected val lruComparator: Comparator<Entry<K, V>> = Comparator { lhs, rhs -> ((lhs.lastUsed - rhs.lastUsed).toInt()) }

    protected var capacity = 0

    protected var lowWater = 0

    protected var usedCapacity = 0

    constructor (capacity: Int, lowWater: Int) {
        if (capacity < 1) {
            throw IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified capacity is less than 1"))
        }
        if (lowWater >= capacity || lowWater < 0) {
            throw IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified low-water value is greater than or equal to the capacity, or less than 1"))
        }
        this.capacity = capacity
        this.lowWater = lowWater
    }

    constructor(capacity: Int) {
        if (capacity < 1) {
            throw java.lang.IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified capacity is less than 1"))
        }
        this.capacity = capacity
        lowWater = (capacity * 0.75).toInt()
    }


    fun count(): Int {
        return entries.size
    }

    /**
     * 获取根据key
     */
    operator fun get(key: K): V? {
        val entry: Entry<K, V>? = entries[key]
        return if (entry != null) {
            entry.lastUsed = System.currentTimeMillis()
            entry.value
        } else {
            null
        }
    }

    fun put(key: K, value: V, size: Int): V? {
        val newEntry: Entry<K, V> = Entry(key, value, size)
        val oldEntry = putEntry(newEntry)
        return oldEntry?.value ?: let { null };
    }

    protected open fun putEntry(newEntry: Entry<K, V>): Entry<K, V>? {
        if (usedCapacity + newEntry.size > capacity) {
            makeSpace(newEntry.size)
        }
        newEntry.lastUsed = System.currentTimeMillis()
        usedCapacity += newEntry.size
        val oldEntry: Entry<K, V>? = entries.put(newEntry.key, newEntry)
        if (oldEntry != null) {
            usedCapacity -= oldEntry.size
            if (newEntry.value !== oldEntry.value) {
                entryRemoved(oldEntry)
                return oldEntry
            }
        }
        return null
    }

    fun remove(key: K): V? {
        val entry: Entry<K, V>? = entries.remove(key)

        return if (entry != null) {
            usedCapacity -= entry.size
            entryRemoved(entry)
            entry.value
        } else {
            null
        }
    }

    fun containsKey(key: K): Boolean {
        return entries.containsKey(key)
    }

    fun clear() {
        for (entry in entries.values) {
            entryRemoved(entry)
        }
        entries.clear()
        usedCapacity = 0
    }

    protected fun makeSpace(spaceRequired: Int) {
        // Sort the entries from least recently used to most recently used, then remove the least recently used entries
        // until the cache capacity reaches the low water and the cache has enough free capacity for the required space.
        val sortedEntries: ArrayList<Entry<K, V>> = ArrayList<Entry<K, V>>(entries.size)
        for (entry in entries.values) {
            sortedEntries.add(entry)
        }
        // 将所有的 value 进行更具时间排序
        Collections.sort(sortedEntries, lruComparator)
        for (entry in sortedEntries) {
            if (usedCapacity > lowWater || capacity - usedCapacity < spaceRequired) {
                entries.remove(entry.key)
                usedCapacity -= entry.size
                entryRemoved(entry)

            } else {
                break
            }
        }
    }

    protected open fun entryRemoved(entry: Entry<K, V>) {}
}