package com.atom.map.util

import com.atom.map.WorldWind
import com.atom.map.util.pool.Pool
import com.atom.map.util.pool.Pools

abstract class Retriever<K, O, V>(maxSimultaneousRetrievals: Int) {

    interface Callback<K, O, V> {
        fun retrievalSucceeded(retriever: Retriever<K, O, V>, key: K, options: O?, value: V)
        fun retrievalFailed(retriever: Retriever<K, O, V>, key: K, ex: Throwable?)
        fun retrievalRejected(retriever: Retriever<K, O, V>, key: K, msg: String)
    }

    protected val lock = Any()

    protected var maxAsyncTasks = 0

    protected var asyncTaskSet: MutableSet<K>

    protected var asyncTaskPool: Pool<AsyncTask<K, O, V>>

    init {
        this.maxAsyncTasks = maxSimultaneousRetrievals
        this.asyncTaskSet = mutableSetOf()
        this.asyncTaskPool = Pools.newPool()
    }

    @Throws(Exception::class)
    protected abstract fun retrieveAsync(key: K, options: O?, callback: Callback<K, O, V>)

    protected open fun recycleAsyncTask(instance: AsyncTask<K, O, V>) {
        synchronized(lock) {
            asyncTaskSet.remove(instance.key)
            asyncTaskPool.release(instance.reset())
        }
    }

    /**
     * 请求池获取一个任务
     */
    protected open fun obtainAsyncTask(
        key: K,
        options: O?,
        callback: Callback<K, O, V>
    ): AsyncTask<K, O, V>? {
        synchronized(lock) {
            if (asyncTaskSet.size >= maxAsyncTasks || asyncTaskSet.contains(key)) {
                return null
            }
            asyncTaskSet.add(key)
            return asyncTaskPool.acquire()?.set(this, key, options, callback)
                ?: let { AsyncTask<K, O, V>().set(this, key, options, callback) }
        }
    }

    /**
     * 判断是否已有key任务
     */
    protected open fun hasTask(key: K): Boolean {
        synchronized(lock) { return asyncTaskSet.contains(key) }
    }

    fun retrieve(
        key: K,
        options: O?,
        callback: Callback<K, O, V>
    ) {
        obtainAsyncTask(key, options, callback)?.let {
            try {
                WorldWind.taskService.execute(it)
            } catch (ignored: Exception) {
                recycleAsyncTask(it)
                callback.retrievalRejected(
                    this,
                    key,
                    ignored.localizedMessage ?: ignored.toString()
                )
            }
        } ?: let {
            callback.retrievalRejected(
                this,
                key,
                "obtain this key or ${asyncTaskSet.size} >= ${maxAsyncTasks}"
            )
        }
    }

    open fun retrieve(
        key: K,
        options: O?,
        callback: Callback<K, O, V>,
        id: Int
    ) {
        // TODO 有优先级的 交给子类实现
        retrieve(key , options , callback)
    }


    protected class Priority {
        private val id: Int
        private val task: AsyncTask<*, *, *>

        constructor(id: Int, task: AsyncTask<*, *, *>) {
            this.id = id
            this.task = task
        }

        fun id(): Int {
            return id
        }

        fun task(): AsyncTask<*, *, *> {
            return task
        }

    }

    /**
     * 任务
     */
    protected class AsyncTask<K, O, V> : Runnable {
        var retriever: Retriever<K, O, V>? = null
        var key: K? = null
        var callback: Callback<K, O, V>? = null
        var options: O? = null
        operator fun set(
            retriever: Retriever<K, O, V>,
            key: K,
            options: O?,
            callback: Callback<K, O, V>
        ): AsyncTask<K, O, V> {
            this.retriever = retriever
            this.key = key
            this.callback = callback
            this.options = options
            return this
        }

        fun reset(): AsyncTask<K, O, V> {
            this.retriever = null
            this.key = null
            this.callback = null
            this.options = null
            return this
        }

        override fun run() {
            try {
                run(this.retriever, this.key, this.callback, { r, k, c ->
                    r.retrieveAsync(k, options, c)
                })
            } catch (ex: Throwable) {
                run(this.retriever, this.key, this.callback, { r, k, c ->
                    c.retrievalFailed(r, k, ex)
                })
            } finally {
                this.retriever?.recycleAsyncTask(this)
            }
        }

        fun <R, K, C> run(retriever: R?, key: K?, callback: C?, block: (R, K, C) -> Unit) {
            if (retriever != null && key != null && callback != null) {
                block(retriever, key, callback)
            }
        }

        fun <R, K, C> run(retriever: R?, key: K?, callback: C?) {
            var str: StringBuilder = java.lang.StringBuilder()
            if (retriever == null) {
                str.append("retriever == null ")
            }
            if (key == null) {
                str.append("key == null ")
            }
            if (callback == null) {
                str.append("callback == null ")
            }
        }
    }
}