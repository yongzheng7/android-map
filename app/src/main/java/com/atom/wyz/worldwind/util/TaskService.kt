package com.atom.wyz.worldwind.util

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class TaskService {

    protected var executorService: ExecutorService? = null

    constructor()

    fun execute(command: Runnable?) {
        command ?.let { this.executorService().execute(it) }
    }

    protected fun executorService(): ExecutorService {
        if (executorService == null) {
            executorService = ThreadPoolExecutor(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
                    SynchronousQueue(),
                    this.threadFactory(),
                    this.rejectedExecutionHandler())
        }
        return executorService!!
    }

    protected fun threadFactory(): ThreadFactory? {
        val threadName = "World Wind Task Service "
        val threadNumber = AtomicInteger(1)
        return ThreadFactory { r ->
            val thread = Thread(r, threadName + threadNumber.getAndIncrement())
            thread.isDaemon = true // task threads do not prevent the process from terminating
            thread
        }
    }

    protected fun rejectedExecutionHandler(): RejectedExecutionHandler? {
        return RejectedExecutionHandler { r, executor ->
            throw RejectedExecutionException() // throw an exception but suppress the message to avoid string allocation
        }
    }
}