package com.atom.map.renderable

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.atom.map.WorldWind
import com.atom.map.util.Retriever
import com.atom.map.util.WWUtil
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.PriorityBlockingQueue

open class PriorityRetriever(maxSimultaneousRetrievals: Int = 8) :
    Retriever<ImageSource, ImageOptions, Bitmap>(maxSimultaneousRetrievals) {

    var resources: Resources? = null
    private val idComparator = Comparator<Priority> { p0, p1 -> p0.id() - p1.id() }
    private val queue = PriorityBlockingQueue(maxSimultaneousRetrievals, idComparator)

    @Throws(Exception::class)
    override fun retrieveAsync(
        key: ImageSource,
        options: ImageOptions?,
        callback: Callback<ImageSource, ImageOptions, Bitmap>
    ) {
        this.decodeImage(key, options)?.let {
            callback.retrievalSucceeded(this, key, options, it)
        } ?: let {
            callback.retrievalFailed(this, key, null) // failed but no exception
        }
    }


    override fun retrieve(
        key: ImageSource,
        options: ImageOptions?,
        callback: Callback<ImageSource, ImageOptions, Bitmap>,
        id: Int
    ) {
        obtainAsyncTask(key, options, callback)?.let {
            if(queue.offer(Priority(id, it))){
                try {
                    WorldWind.taskService.execute(Runnable {
                        queue.poll()?.task()?.run()
                    })
                } catch (ignored: Exception) {
                    recycleAsyncTask(it)
                    callback.retrievalRejected(
                        this,
                        key,
                        ignored.localizedMessage ?: ignored.toString()
                    )
                }
            }else{
                callback.retrievalRejected(
                    this,
                    key,
                    "queue is full = ${queue.size}"
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

    @Throws(Exception::class)
    private fun decodeImage(imageSource: ImageSource, imageOptions: ImageOptions?): Bitmap? {
        if (imageSource.isBitmap()) {
            return imageSource.asBitmap()
        }
        if (imageSource.isBitmapFactory()) {
            return imageSource.asBitmapFactory()?.createBitmap()
        }
        if (imageSource.isResource()) {
            return this.decodeResource(imageSource.asResource(), imageOptions)
        }
        if (imageSource.isFilePath()) {
            return this.decodeFilePath(imageSource.asFilePath(), imageOptions)
        }
        if (imageSource.isUrl()) {
            return decodeUrl(imageSource.asUrl(), imageOptions)
        }
        return decodeUnrecognized(imageSource)
    }

    private fun decodeResource(id: Int, imageOptions: ImageOptions?): Bitmap? {
        val options: BitmapFactory.Options = this.bitmapFactoryOptions(imageOptions)
        return if (resources != null) BitmapFactory.decodeResource(resources, id, options) else null
    }

    private fun decodeFilePath(pathName: String?, imageOptions: ImageOptions?): Bitmap? {
        val options = bitmapFactoryOptions(imageOptions)
        return BitmapFactory.decodeFile(pathName, options)
    }

    @Throws(Exception::class)
    private fun decodeUrl(urlString: String?, imageOptions: ImageOptions?): Bitmap? {
        var stream: InputStream? = null
        try {
            urlString ?: return null
            val url = URL(urlString)
            val conn = url.openConnection() as (HttpURLConnection)
            conn.connectTimeout = 3000
            conn.requestMethod = "GET"
            conn.readTimeout = 30000
            // 模拟浏览器
            conn.setRequestProperty("Accept-Language", "zh-CN")
            conn.setRequestProperty("Charset", "UTF-8")
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Trident/4.0; " +
                        ".NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; " +
                        ".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"
            )
            stream = BufferedInputStream(conn.inputStream)
            val options = bitmapFactoryOptions(imageOptions)
            return BitmapFactory.decodeStream(stream, null, options)
        } finally {
            WWUtil.closeSilently(stream)
        }
    }

    private fun decodeUnrecognized(imageSource: ImageSource): Bitmap? {
        return null
    }

    private fun bitmapFactoryOptions(imageOptions: ImageOptions?): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inScaled =
            false // suppress default image scaling; load the image in its native dimensions
        imageOptions?.let {
            when (imageOptions.imageFormat) {
                WorldWind.RGBA_8888 -> options.inPreferredConfig = Bitmap.Config.ARGB_8888
                WorldWind.RGB_565 -> options.inPreferredConfig = Bitmap.Config.RGB_565
            }
        }
        return options
    }
}