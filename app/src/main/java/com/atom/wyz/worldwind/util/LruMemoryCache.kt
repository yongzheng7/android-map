package com.atom.wyz.worldwind.util

import java.util.*
import kotlin.Comparator

open class LruMemoryCache<K, V> {

    class Entry<K, V>(val key: K, val value: V, val size: Int) {
        var lastUsed: Long = 0
    }

    protected val entries = hashMapOf<K, Entry<K, V>>()

    protected val lruComparator: Comparator<Entry<K, V>> =
        Comparator { lhs, rhs -> ((lhs.lastUsed - rhs.lastUsed).toInt()) }

    open var capacity = 0

    open var lowWater = 0

    open var usedCapacity = 0

    constructor (capacity: Int, lowWater: Int) {
        if (capacity < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified capacity is less than 1"
                )
            )
        }
        if (lowWater >= capacity || lowWater < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified low-water value is greater than or equal to the capacity, or less than 1"
                )
            )
        }
        this.capacity = capacity
        this.lowWater = lowWater
    }

    constructor(capacity: Int) {
        if (capacity < 1) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR, "LruMemoryCache", "constructor",
                    "The specified capacity is less than 1"
                )
            )
        }
        this.capacity = capacity
        lowWater = (capacity * 0.75).toInt()
    }


    open fun getEntryCount(): Int {
        return entries.size
    }

    open fun get(key: K): V? {
        val entry: Entry<K, V>? = entries[key]
        return if (entry != null) {
            entry.lastUsed = System.currentTimeMillis()
            entry.value
        } else {
            null
        }
    }

    open fun put(key: K, value: V, size: Int): V? {
        if (usedCapacity + size > capacity) {
            makeSpace(size)
        }
        val newEntry = Entry(key, value, size)
        newEntry.lastUsed = System.currentTimeMillis()
        this.usedCapacity += newEntry.size
        val oldEntry = entries.put(newEntry.key, newEntry)

        return oldEntry?.let {
            usedCapacity -= it.size
            if (newEntry.value !== it.value) {
                entryRemoved(it.key, it.value, newEntry.value, false)
                it.value
            } else {
                null
            }
        } ?: let {
            null
        }
    }

    open fun remove(key: K): V? {
        return entries.remove(key)?.let {
            usedCapacity -= it.size
            entryRemoved(it.key, it.value, null, false)
            it.value
        } ?: let {
            null
        }
    }

    open fun containsKey(key: K) = entries.containsKey(key)

    open fun trimToAge(maxAgeMillis: Long): Int {
        var trimmedCapacity = 0
        for (entry in this.assembleSortedEntries()) {
            if (entry.lastUsed < maxAgeMillis) {
                entries.remove(entry.key)
                usedCapacity -= entry.size
                trimmedCapacity += entry.size
                entryRemoved(entry.key, entry.value, null, false)
            } else {
                break
            }
        }
        return trimmedCapacity
    }

    open fun clear() {
        for (entry in entries.values) {
            entryRemoved(entry.key, entry.value, null, false)
        }
        entries.clear()
        usedCapacity = 0
    }

    protected open fun makeSpace(spaceRequired: Int) {
        for (entry in this.assembleSortedEntries()) {
            if (usedCapacity > lowWater || (capacity - usedCapacity) < spaceRequired) {
                entries.remove(entry.key)
                usedCapacity -= entry.size
                entryRemoved(entry.key, entry.value, null, true)
            } else {
                break
            }
        }
    }

    protected open fun assembleSortedEntries(): ArrayList<Entry<K, V>> {
        val sortedEntries: ArrayList<Entry<K, V>> = ArrayList(entries.size)
        for (entry in entries.values) {
            sortedEntries.add(entry)
        }
        Collections.sort(sortedEntries, lruComparator)
        return sortedEntries
    }

    protected open fun entryRemoved(key: K, oldValue: V, newValue: V?, evicted: Boolean) {}

}