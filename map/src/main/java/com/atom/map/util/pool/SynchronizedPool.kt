package com.atom.map.util.pool

class SynchronizedPool<T>(initialCapacity: Int = 10) : BasicPool<T>(initialCapacity) {
    protected val lock = Any()
    override fun acquire(): T? {
        synchronized(lock) { return super.acquire() }
    }

    override fun release(instance: T?) {
        synchronized(lock) { super.release(instance) }
    }
}