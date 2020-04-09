package com.atom.wyz.worldwind.util.pool

class Pools {
    companion object {
        fun <T> newPool(initialCapacity: Int = 10): Pool<T> {
            return BasicPool(initialCapacity)
        }

        fun <T> newSynchronizedPool(initialCapacity: Int = 10): Pool<T> {
            return SynchronizedPool(initialCapacity)
        }
    }
}