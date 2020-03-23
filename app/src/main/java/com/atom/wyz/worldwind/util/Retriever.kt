package com.atom.wyz.worldwind.util

interface Retriever<K, V> {
    fun retrieve(key: K?, callback: Retriever.Callback<K, V>)

    interface Callback<K, V> {
        fun retrievalSucceeded(retriever: Retriever<K, V>?, key: K?, value: V?)
        fun retrievalFailed(retriever: Retriever<K, V>?, key: K?, ex: Throwable?)
        fun retrievalRejected(retriever: Retriever<K, V>?, key: K?)
    }
}