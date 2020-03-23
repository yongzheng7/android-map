package com.atom.wyz.worldwind.util

import android.util.Log
import androidx.core.util.Pools
import com.atom.wyz.worldwind.WorldWind
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

abstract class AbstractRetriever<K, V>(maxSimultaneousRetrievals: Int) : Retriever<K, V> {
    protected val lock = Any()

    protected var maxAsyncTasks = 0

    protected var asyncTaskSet: HashSet<K>

    protected var asyncTaskPool: Pools.Pool<AsyncTask<K, V>>

    init {
        maxAsyncTasks = maxSimultaneousRetrievals
        this.asyncTaskSet = HashSet<K>()
        asyncTaskPool = Pools.SimplePool(maxSimultaneousRetrievals)
    }

    /**
     * 执行任务
     */
    protected abstract fun retrieveAsync(key: K, callback: Retriever.Callback<K, V>?)

    /**
     * 回收任务
     */
    protected open fun recycleAsyncTask(instance: AsyncTask<K, V>) {
        synchronized(lock) {
            asyncTaskSet.remove(instance.key)
            asyncTaskPool.release(instance.reset())
        }
    }

    /**
     * 请求池获取一个任务
     */
    protected open fun obtainAsyncTask(key: K, callback: Retriever.Callback<K, V>): AsyncTask<K, V>? {
        synchronized(lock) {
            //asyncTaskSet.size >= maxAsyncTasks ||
            if ( asyncTaskSet.contains(key)) {
                return null
            }
            asyncTaskSet.add(key)

            return asyncTaskPool.acquire() ?: let { AsyncTask<K, V>().set(this, key, callback) }
        }
    }

    /**
     * 判断是否已有key任务
     */
    protected open fun hasTask(key: K): Boolean {
        synchronized(lock) { return asyncTaskSet.contains(key) }
    }

    override fun retrieve(key: K?, callback: Retriever.Callback<K, V>) {
        if (key == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "AbstractRetriever", "retrieve", "missingKey"))
        }

        val task: AsyncTask<K, V> = obtainAsyncTask(key, callback) ?: let {
            //Log.e("Image_retrieval" , "拒绝 因为已经有了 , $key");
            callback.retrievalRejected(this, key)
            return
        }

        try {
            WorldWind.taskService.execute(task)
        } catch (ignored: RejectedExecutionException) { // singleton task service is full
            //Log.e("Image_retrieval" , "拒绝 因为已经满了 , $key");
            recycleAsyncTask(task)
            callback.retrievalRejected(this, key)
        }
    }

    /**
     * 任务
     */
    protected class AsyncTask<K, V> : Runnable {
        var retriever: AbstractRetriever<K, V>? = null
        var key: K? = null
        var callback: Retriever.Callback<K, V>? = null

        operator fun set(retriever: AbstractRetriever<K, V>?, key: K, callback: Retriever.Callback<K, V>): AsyncTask<K, V> {
            this.retriever = retriever
            this.key = key
            this.callback = callback
            return this
        }

        fun reset(): AsyncTask<K, V> {
            retriever = null
            key = null
            callback = null
            return this
        }

        override fun run() {
            retriever?.let {
                try {
                    key?.apply { it.retrieveAsync(this, callback) }
                } catch (ex: Throwable) {
                    callback?.retrievalFailed(retriever, key, ex)
                } finally {
                    it.recycleAsyncTask(this)
                }
            }
        }
    }
}