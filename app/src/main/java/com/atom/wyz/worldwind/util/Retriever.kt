package com.atom.wyz.worldwind.util

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.Pools

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
        Logger.log(
            Logger.ERROR,
            "recycleAsyncTask    recycle  start------------ ${Thread.currentThread().id}"
        )
        synchronized(lock) {
            Logger.log(
                Logger.ERROR,
                "recycleAsyncTask    synchronized 1  ${Thread.currentThread().id}  Set.size > ${asyncTaskSet.size}  Pool.size > ${asyncTaskPool.size()}  ${(instance.key.toString())}"
            )
            asyncTaskSet.remove(instance.key)
            Logger.log(Logger.ERROR, "recycleAsyncTask    synchronized 1.1  $asyncTaskSet")
            Logger.log(
                Logger.ERROR,
                "recycleAsyncTask    synchronized 2  ${Thread.currentThread().id}  Set.size > ${asyncTaskSet.size} "
            )
            asyncTaskPool.release(instance.reset())
            Logger.log(
                Logger.ERROR,
                "recycleAsyncTask    synchronized 3  ${Thread.currentThread().id}  Pool.size > ${asyncTaskPool.size()} "
            )
        }
        Logger.log(
            Logger.ERROR,
            "recycleAsyncTask    recycle  end  ------------  ${Thread.currentThread().id}"
        )
        Logger.log(
            Logger.ERROR,
            "obtainAsyncTask  4  add taskService  end   size>" + asyncTaskSet.size
        )
    }

    /**
     * 请求池获取一个任务
     */
    protected open fun obtainAsyncTask(
        key: K,
        options: O?,
        callback: Callback<K, O, V>
    ): AsyncTask<K, O, V>? {
        Logger.log(
            Logger.ERROR,
            "obtainAsyncTask  ---------------------------------------------------"
        )
        synchronized(lock) {
            if (asyncTaskSet.size >= maxAsyncTasks || asyncTaskSet.contains(key)) {
                return null
            }
            Logger.log(
                Logger.ERROR,
                "obtainAsyncTask  1  add asyncTaskSet " + asyncTaskSet.size + "--" + key.toString()
            )
            asyncTaskSet.add(key)
            Logger.log(Logger.ERROR, "obtainAsyncTask  2  add asyncTaskSet " + asyncTaskSet.size)
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

    fun retrieve(key: K, options: O?, callback: Callback<K, O, V>) {
        obtainAsyncTask(key, options, callback)?.let {
            try {
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  3  add taskService  start size>" + asyncTaskSet.size
                )
                WorldWind.taskService.execute(it)
            } catch (ignored: Exception) {
                Logger.log(Logger.ERROR, "obtainAsyncTask  5  add taskService  Exception ")
                recycleAsyncTask(it)
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  6  add taskService  Exception recycleAsyncTask end "
                )
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
            Logger.log(
                Logger.ERROR,
                "obtainAsyncTask  3  add taskService  start 1 > ${Thread.currentThread().id}"
            )
            run(this.retriever, this.key, this.callback)
            try {
                Logger.log(
                    Logger.ERROR,
                    "retrieveAsync    start --------------${Thread.currentThread().name}-${Thread.currentThread().id}"
                )
                run(this.retriever, this.key, this.callback, { r, k, c ->
                    Logger.log(
                        Logger.ERROR,
                        "obtainAsyncTask  3  add taskService  start 2 > ${Thread.currentThread().id}"
                    )
                    r.retrieveAsync(k, options, c)
                })
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  3  add taskService  start 3 > ${Thread.currentThread().id}"
                )
                Logger.log(
                    Logger.ERROR,
                    "retrieveAsync    end   ---------------${Thread.currentThread().name}-${Thread.currentThread().id}"
                )
            } catch (ex: Throwable) {
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  3  add taskService  start 31 > ${Thread.currentThread().id }   ${ex.localizedMessage}"
                )
                Logger.log(
                    Logger.ERROR,
                    "retrieveAsync    Throwable  ----------${Thread.currentThread().name}-${Thread.currentThread().id}"
                )
                run(this.retriever, this.key, this.callback, { r, k, c ->
                    c.retrievalFailed(r, k, ex)
                })
            } finally {
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  3  add taskService  start 4 > ${Thread.currentThread().id}"
                )
                Logger.log(
                    Logger.ERROR,
                    "retrieveAsync    recycle  -----A------${Thread.currentThread().name}-${Thread.currentThread().id}"
                )
                this.retriever?.recycleAsyncTask(this)
                Logger.log(
                    Logger.ERROR,
                    "obtainAsyncTask  3  add taskService  start 5 > ${Thread.currentThread().id}"
                )
                Logger.log(
                    Logger.ERROR,
                    "retrieveAsync    recycle  -----A1-----${Thread.currentThread().name}-${Thread.currentThread().id}"
                )
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
            Logger.log(Logger.ERROR, "obtainAsyncTask  3  add taskService  start 1.1 > $str")
        }
    }
}