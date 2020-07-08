package com.atom.wyz.worldwind.util

import android.util.Log
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.Pools
import java.util.concurrent.RejectedExecutionException

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
        this.asyncTaskPool = Pools.newPool(maxSimultaneousRetrievals)
    }

    protected abstract fun retrieveAsync(key: K, options: O?, callback: Callback<K, O, V>)

    protected open fun recycleAsyncTask(instance: AsyncTask<K, O, V>) {
        synchronized(lock) {
            asyncTaskSet.remove(instance.key)
            Log.e("asyncTaskSet" , "recycleAsyncTask > ${asyncTaskSet.size}") ;
            asyncTaskPool.release(instance.reset())
            Log.e("addTile" , "recycleAsyncTask end \'$instance.key\'  ${asyncTaskSet.size} >= ${maxAsyncTasks}")
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
            Log.e("asyncTaskSet" , "obtainAsyncTask > ${asyncTaskSet.size}") ;
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
        Log.e("addTile" , "retrieve start 1 ${key}")
        val task = obtainAsyncTask(key, options, callback) ?: let {
            callback.retrievalRejected(this, key, "obtain this key or ${asyncTaskSet.size} >= ${maxAsyncTasks}")
            return
        }
        Log.e("addTile" , "retrieve start 2 ${key}")
        try {
            WorldWind.taskService.execute(task)
        } catch (ignored: RejectedExecutionException) {
            Log.e("addTile" , "retrieve start 3 ${key}")
            recycleAsyncTask(task)
            callback.retrievalRejected(this, key, ignored.localizedMessage ?: ignored.toString())
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
            run(this.retriever, this.key, this.callback, { r, k, c ->
                try {
                    Log.e("addTile" , "AsyncTask  run() 1")
                    r.retrieveAsync(k, options, c)
                    Log.e("addTile" , "AsyncTask  run() 2")
                } catch (ex: Throwable) {
                    c.retrievalFailed(r, k, ex)
                } finally {
                    Log.e("addTile" , "recycleAsyncTask  run()")
                    r.recycleAsyncTask(this)
                }
            })
        }
        fun <R, K, C> run(retriever: R?, key: K?, callback: C?, block: (R, K, C) -> Unit) {
            if (retriever != null && key != null && callback != null) {
                block(retriever, key, callback)
            }
        }
    }
}