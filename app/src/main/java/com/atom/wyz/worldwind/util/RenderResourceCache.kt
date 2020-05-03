package com.atom.wyz.worldwind.util

import android.content.res.Resources
import android.graphics.Bitmap
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.ImageRetriever
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.RenderResource
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentLinkedQueue

class RenderResourceCache : LruMemoryCache<Any, RenderResource> , Retriever.Callback<ImageSource, Bitmap> {

    var resources: Resources? = null
        set(value) {
            field = value
            imageRetriever.resources = value
        }
    // 回收队列
    protected var evictionQueue = ConcurrentLinkedQueue<RenderResource>()
    // 陈俄共队列
    protected var pendingQueue = ConcurrentLinkedQueue<Entry<out Any, out RenderResource>>()

    protected var imageRetriever = ImageRetriever()


    constructor(capacity: Int, lowWater: Int) : super(capacity, lowWater)
    constructor(capacity: Int) : super(capacity)


    override fun clear() {
        entries.clear() // the cache entries are invalid; clear but don't call entryRemoved
        evictionQueue.clear() // the eviction queue no longer needs to be processed
        usedCapacity = 0
    }

    fun releaseEvictedResources(dc: DrawContext) {
        var evicted: RenderResource?
        while (evictionQueue.poll().also { evicted = it } != null) {
            try {
                evicted?.release(dc)
                if (Logger.isLoggable(Logger.INFO)) {
                    Logger.log(Logger.INFO, "Released render resource \'$evicted\'")
                }
            } catch (ignored: Exception) {
                if (Logger.isLoggable(Logger.INFO)) {
                    Logger.log(Logger.INFO, "Exception releasing render resource \'$evicted\'", ignored)
                }
            }
        }
    }

    override fun entryRemoved(entry: Entry<Any, RenderResource>) {
        evictionQueue.offer(entry.value)
    }

    override fun entryReplaced(
        oldEntry: Entry<Any, RenderResource>,
        newEntry: Entry<Any, RenderResource>
    ) {
        evictionQueue.offer(oldEntry.value)
    }

    fun retrieveTexture(imageSource: ImageSource?): GpuTexture? {
        if (imageSource == null) {
            return null
        }
        if (imageSource.isBitmap()) {
            val texture = GpuTexture(imageSource.asBitmap())
            put(imageSource, texture, texture.textureByteCount)
            return texture
        }
        val texture = this.processPendingQueue(imageSource) as GpuTexture?
        if (texture != null) {
            return texture
        }
        imageRetriever.retrieve(imageSource, this) // adds entries to pendingQueue
        return null
    }

    protected fun processPendingQueue(key: Any): RenderResource? {
        var match: Entry<out Any, out RenderResource>? = null
        var pending: Entry<Any, RenderResource>
        var poll = pendingQueue.poll()?.let { it as Entry<Any, RenderResource> }
        while (poll != null) {
            pending = poll
            if (match == null && pending.key == key) {
                match = pending
            }
            putEntry(pending)
            poll = pendingQueue.poll()?.let { it as Entry<Any, RenderResource> }
        }
        return match?.value
    }

    override fun retrievalSucceeded(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?, value: Bitmap?) {
        val texture = GpuTexture(value)
        val entry = Entry(key!!, texture, texture.textureByteCount)
        pendingQueue.offer(entry)
        WorldWind.requestRedraw()
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval succeeded \'$key\'")
        }
    }

    override fun retrievalFailed(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?, ex: Throwable?) {
        if (ex is SocketTimeoutException) {
            Logger.log(Logger.ERROR, "Socket timeout retrieving image \'$key\'")
        } else if (ex != null) {
            Logger.log(Logger.ERROR, "Image retrieval failed with exception \'$key\'")
        } else {
            Logger.log(Logger.ERROR, "Image retrieval failed \'$key\'")
        }
    }

    override fun retrievalRejected(retriever: Retriever<ImageSource, Bitmap>?, key: ImageSource?) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval rejected \'$key\'")
        }
    }
}