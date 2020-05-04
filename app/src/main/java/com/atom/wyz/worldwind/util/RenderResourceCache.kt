package com.atom.wyz.worldwind.util

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.util.Log
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.render.*
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class RenderResourceCache
    : LruMemoryCache<Any, RenderResource>,
    Retriever.Callback<ImageSource,ImageOptions, Bitmap>,
    Handler.Callback {
    companion object {
        protected const val STALE_RETRIEVAL_AGE = 3000
        protected const val TRIM_STALE_RETRIEVALS = 1
        protected const val TRIM_STALE_RETRIEVALS_DELAY = 6000
        fun recommendedCapacity(context: Context?): Int {
            val am =
                if (context != null) context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager else null
            return if (am != null) {
                val mi = ActivityManager.MemoryInfo()
                am.getMemoryInfo(mi)
                if (mi.totalMem >= 1024 * 1024 * 2048L) { // use 384 MB on machines with 1536 MB or more
                    1024 * 1024 * 384
                } else if (mi.totalMem >= 1024 * 1024 * 1536) { // use 256 MB on machines with 1536 MB or more
                    1024 * 1024 * 256
                } else if (mi.totalMem >= 1024 * 1024 * 1024) { // use 192 MB on machines with 1024 MB or more
                    1024 * 1024 * 192
                } else if (mi.totalMem >= 1024 * 1024 * 512) { // use 96 MB on machines with 512 MB or more
                    1024 * 1024 * 96
                } else { // use 64 MB on machines with less than 512 MB
                    1024 * 1024 * 64
                }
            } else { // use 64 MB by default
                1024 * 1024 * 64
            }
        }
    }

    var resources: Resources? = null
        set(value) {
            field = value
            (imageRetriever as ImageRetriever).resources = value
        }
    // 回收队列
    protected var evictionQueue: Queue<RenderResource>

    protected var imageRetriever: Retriever<ImageSource, ImageOptions,Bitmap>

    protected var urlImageRetriever: Retriever<ImageSource,ImageOptions, Bitmap>

    protected var imageRetrieverCache: LruMemoryCache<ImageSource, Bitmap>

    protected var handler: Handler

    constructor(capacity: Int, lowWater: Int) : super(capacity, lowWater)
    constructor(capacity: Int) : super(capacity)

    init {
        handler = Handler(this)
        evictionQueue = ConcurrentLinkedQueue()
        imageRetriever = ImageRetriever(2)
        urlImageRetriever = ImageRetriever(8)
        imageRetrieverCache = SynchronizedMemoryCache(this.capacity / 8)
        Logger.log(
            Logger.INFO, String.format(
                Locale.US, "RenderResourceCache initialized  %,.0f KB  (%,.0f KB retrieval cache)",
                this.capacity / 1024.0, imageRetrieverCache.capacity / 1024.0
            )
        )
    }

    override fun clear() {
        handler.removeMessages(TRIM_STALE_RETRIEVALS)
        entries.clear()
        evictionQueue.clear()
        imageRetrieverCache.clear()
        usedCapacity = 0
    }
    override fun entryRemoved(key: Any, oldValue: RenderResource, newValue: RenderResource?, evicted: Boolean) {
        evictionQueue.offer(oldValue)
    }
    fun releaseEvictedResources(dc: DrawContext) {
        var evicted: RenderResource?
        while (evictionQueue.poll().also { evicted = it } != null) {
            try {
                evicted?.release(dc)
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Released render resource \'$evicted\'")
                }
            } catch (ignored: Exception) {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Exception releasing render resource \'$evicted\'", ignored)
                }
            }
        }
    }

    fun retrieveTexture(imageSource: ImageSource?, imageOptions : ImageOptions? ): GpuTexture? {
        if (imageSource == null) {
            return null
        }
        if (imageSource.isBitmap()) {
            val texture = GpuTexture(imageSource.asBitmap())
            put(imageSource, texture, texture.textureByteCount)
            return texture
        }
        imageRetrieverCache.remove(imageSource)?.let{
            val texture = GpuTexture(it)
            put(imageSource, texture, texture.textureByteCount)
            return texture
        }

        if (imageSource.isUrl()) {
            urlImageRetriever.retrieve(imageSource, imageOptions,this)
        } else {
            imageRetriever.retrieve(imageSource, imageOptions,this)
        }
        return null
    }

    override fun retrievalSucceeded(retriever: Retriever<ImageSource,ImageOptions, Bitmap>, key: ImageSource, options : ImageOptions? , value: Bitmap) {
        imageRetrieverCache.put(key, value, value.byteCount)
        WorldWind.requestRedraw()
        if (!handler.hasMessages(TRIM_STALE_RETRIEVALS)) {
            handler.sendEmptyMessageDelayed(
                TRIM_STALE_RETRIEVALS,
                TRIM_STALE_RETRIEVALS_DELAY.toLong()
            )
        }
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval succeeded \'$key\'")
        }
    }

    override fun retrievalFailed(retriever: Retriever<ImageSource,ImageOptions, Bitmap>, key: ImageSource, ex: Throwable?) {
        if (ex is SocketTimeoutException) {
            Logger.log(Logger.ERROR, "Socket timeout retrieving image \'$key\'")
        } else if (ex != null) {
            Logger.log(Logger.ERROR, "Image retrieval failed with exception \'$key\'")
        } else {
            Logger.log(Logger.ERROR, "Image retrieval failed \'$key\'")
        }
    }

    override fun retrievalRejected(retriever: Retriever<ImageSource,ImageOptions, Bitmap>, key: ImageSource) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval rejected \'$key\'")
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == TRIM_STALE_RETRIEVALS) {
            val now = System.currentTimeMillis()
            val trimmedCapacity = imageRetrieverCache.trimToAge(now - STALE_RETRIEVAL_AGE)
            if (!handler.hasMessages(TRIM_STALE_RETRIEVALS) && imageRetrieverCache.usedCapacity != 0) {
                handler.sendEmptyMessageDelayed(
                    TRIM_STALE_RETRIEVALS,
                    TRIM_STALE_RETRIEVALS_DELAY.toLong()
                )
            }
            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(
                    Logger.DEBUG, String.format(
                        Locale.US, "Trimmed stale image retrievals %,.0f KB",
                        trimmedCapacity / 1024.0
                    )
                )
            }
        }
        return false
    }

}