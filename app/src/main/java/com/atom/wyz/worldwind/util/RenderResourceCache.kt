package com.atom.wyz.worldwind.util

import android.content.res.Resources
import android.graphics.Bitmap
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.render.*
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentLinkedQueue

class RenderResourceCache : LruMemoryCache<Any, RenderResource> {

    var resources: Resources? = null
        set(value) {
            field = value
            imageRetriever.resources = value
        }
    protected var evictionQueue = ConcurrentLinkedQueue<Entry<out Any, out RenderResource>>()

    protected var pendingQueue = ConcurrentLinkedQueue<Entry<out Any, out RenderResource>>()

    protected var imageRetriever = ImageRetriever()

    protected var imageRetrieverCallback = object : Retriever.Callback<ImageSource, Bitmap> {
        override fun retrievalSucceeded(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?, value: Bitmap?) {
           // Log.e("Image_retrieval" , "retrievalSucceeded $key")
            val texture = GpuTexture(value)
            val entry = Entry(key!!, texture, texture.imageByteCount)
            pendingQueue.offer(entry)
            WorldWind.requestRender()
            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(Logger.DEBUG, "Image retrieval succeeded \'$key\'")
            }
        }

        override fun retrievalFailed(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?, ex: Throwable?) {
            //Log.e("Image_retrieval" , "retrievalFailed $key")
            if (ex is SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
                Logger.log(Logger.ERROR, "Socket timeout retrieving image \'$key\'")
            } else if (ex != null) { // log checked exceptions with the entire stack trace
                Logger.log(Logger.ERROR, "Image retrieval failed with exception \'$key\'")
            } else {
                Logger.log(Logger.ERROR, "Image retrieval failed \'$key\'")
            }
        }

        override fun retrievalRejected(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?) {
            //Log.e("Image_retrieval" , "retrievalRejected $key")
            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(Logger.DEBUG, "Image retrieval rejected \'$key\'")
            }
        }
    }

    constructor(capacity: Int, lowWater: Int) : super(capacity, lowWater)
    constructor(capacity: Int) : super(capacity)


    fun contextLost(dc: DrawContext?) {
        entries.clear() // the cache entries are invalid; clear but don't call entryRemoved
        evictionQueue.clear() // the eviction queue no longer needs to be processed
        pendingQueue.clear()
        usedCapacity = 0
    }

    fun releaseEvictedResources(dc: DrawContext) {
        var evicted: Entry<out Any, out RenderResource> ?= null
        while (evictionQueue.poll().also { it ?.let { evicted = it } } != null) {
            try {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Released render resource \'" + evicted ?.key + "\'")
                }
                evicted ?.value ?.release(dc) // TODO rename as dispose as release
            } catch (ignored: Exception) {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Exception releasing render resource \'" + evicted ?.key + "\'", ignored)
                }
            }
        }
    }


    override fun entryRemoved(entry: Entry<Any, RenderResource>) {
        evictionQueue.offer(entry)
    }

    fun retrieveTexture(imageSource: ImageSource?): GpuTexture? {
        if (imageSource == null) {
            return null
        }
        if (imageSource.isBitmap()) {
            val texture = GpuTexture(imageSource.asBitmap())
            put(imageSource, texture, texture.imageByteCount)
            return texture
        }
        val texture = this.processPendingQueue(imageSource) as GpuTexture?
        if (texture != null) {
            return texture
        }
        imageRetriever.retrieve(imageSource, imageRetrieverCallback) // adds entries to pendingQueue
        return null
    }

    protected fun processPendingQueue(key: Any): RenderResource? {
        var match: Entry<out Any, out RenderResource>? = null
        var pending: Entry< Any,  RenderResource>
        var poll = pendingQueue.poll() ?.let { it as  Entry< Any,  RenderResource> }
        while (poll != null) {
            pending = poll
            if (match == null && pending.key == key) {
                match = pending
            }
            putEntry(pending)
            poll = pendingQueue.poll() ?.let { it as  Entry< Any,  RenderResource> }
        }
        return if (match != null) match.value else null
    }
}