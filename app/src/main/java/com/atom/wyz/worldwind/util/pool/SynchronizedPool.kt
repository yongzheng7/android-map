package com.atom.wyz.worldwind.util.pool

class SynchronizedPool<T>(initialCapacity: Int) : BasicPool<T>(initialCapacity) {
    protected val lock = Any()
    override fun acquire(): T? {
        synchronized(lock) { return super.acquire() }
    }

    override fun release(instance: T?) {
        synchronized(lock) { super.release(instance) }
    }
}