package com.atom.map.util

class SynchronizedMemoryCache<K, V> : LruMemoryCache<K, V> {
    private val lock = Any()

    override var capacity = 0
        get() = synchronized(lock) { return field }

    override var lowWater = 0
        get() = synchronized(lock) { return field }

    override var usedCapacity = 0
        get() = synchronized(lock) { return field }

    constructor(capacity: Int, lowWater: Int) : super(capacity, lowWater)
    constructor(capacity: Int) : super(capacity)


    override fun get(key: K): V? {
        synchronized(lock) { return super.get(key) }
    }

    override fun put(key: K, value: V, size: Int): V? {
        synchronized(lock) { return super.put(key, value, size) }
    }

    override fun remove(key: K): V? {
        synchronized(lock) { return super.remove(key) }
    }

    override fun trimToAge(maxAgeMillis: Long): Int {
        synchronized(lock) { return super.trimToAge(maxAgeMillis) }
    }

    override fun containsKey(key: K): Boolean {
        synchronized(lock) { return super.containsKey(key) }
    }

    override fun clear() {
        synchronized(lock) { super.clear() }
    }
}