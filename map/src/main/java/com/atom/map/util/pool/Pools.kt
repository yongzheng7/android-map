package com.atom.map.util.pool

class Pools {
    companion object {
        fun <T> newPool(initialCapacity: Int = 0): Pool<T> {
            return BasicPool(initialCapacity)
        }

        fun <T> newSynchronizedPool(initialCapacity: Int = 0): Pool<T> {
            return SynchronizedPool(initialCapacity)
        }
    }
}