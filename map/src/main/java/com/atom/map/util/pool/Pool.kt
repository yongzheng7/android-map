package com.atom.map.util.pool

interface Pool<T> {
    /**
     * Acquires an instance from the pool. This returns null if the pool is empty.
     */
    fun acquire(): T?

    /**
     * Releases an instance to the pool. This has no effect if the instance is null.
     */
    fun release(instance: T?)


    fun size() : Int
}