package com.atom.wyz.worldwind.util

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.Pools
import java.util.concurrent.RejectedExecutionException

abstract class Retriever<K, O, V>(maxSimultaneousRetrievals: Int) {

    interface Callback<K, O, V> {
        fun retrievalSucceeded(retriever: Retriever<K, O, V>, key: K, options: O?, value: V)
        fun retrievalFailed(retriever: Retriever<K, O, V>, key: K, ex: Throwable?)
        fun retrievalRejected(retriever: Retriever<K, O, V>, key: K)
    }

    protected val lock = Any()

    protected var maxAsyncTasks = 0

    protected var asyncTaskSet: HashSet<K>

    protected var asyncTaskPool: Pool<AsyncTask<K, O, V>>

    init {
        this.maxAsyncTasks = maxSimultaneousRetrievals
        this.asyncTaskSet = HashSet()
        this.asyncTaskPool = Pools.newPool(maxSimultaneousRetrievals)
    }

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
    protected open fun obtainAsyncTask(key: K, options: O?, callback: Callback<K, O, V>): AsyncTask<K, O, V>? {
        synchronized(lock) {
            if (asyncTaskSet.size >= maxAsyncTasks || asyncTaskSet.contains(key)) {
                return null
            }
            asyncTaskSet.add(key)

            return asyncTaskPool.acquire() ?: let { AsyncTask<K, O, V>().set(this, key, options, callback) }
        }
    }

    /**
     * 判断是否已有key任务
     */
    protected open fun hasTask(key: K): Boolean {
        synchronized(lock) { return asyncTaskSet.contains(key) }
    }

    fun retrieve(key: K, options: O?, callback: Callback<K, O, V>) {
        val task = obtainAsyncTask(key, options, callback) ?: let {
            callback.retrievalRejected(this, key)
            return
        }

        try {
            WorldWind.taskService.execute(task)
        } catch (ignored: RejectedExecutionException) {
            recycleAsyncTask(task)
            callback.retrievalRejected(this, key)
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
            retriever: Retriever<K, O, V>?,
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
            if (this.retriever == null || this.key == null || this.callback == null ) return
            try {
                retriever!!.retrieveAsync(key!!, options, callback!!)
            } catch (ex: Throwable) {
                callback!!.retrievalFailed(retriever!!, key!!, ex)
            } finally {
                retriever!!.recycleAsyncTask(this)
            }
        }
    }
}