package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import com.atom.wyz.worldwind.util.AbstractRetriever
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.Retriever
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class ImageRetriever() : AbstractRetriever<ImageSource, Bitmap>(MAX_SIMULTANEOUS_RETRIEVALS) {
    companion object {
        val MAX_SIMULTANEOUS_RETRIEVALS = 8
    }

    var resources: Resources? = null

    override fun retrieveAsync(key: ImageSource, callback: Retriever.Callback<ImageSource, Bitmap>?) {
        try {
            val bitmap = this.decodeImage(key)
            if (bitmap != null) {
                callback?.retrievalSucceeded(this, key, bitmap)
            } else {
                callback?.retrievalFailed(this, key, null) // failed but no exception
            }
        } catch (logged: Throwable) {
            callback?.retrievalFailed(this, key, logged) // failed with exception
        }
    }

    @Throws(IOException::class)
    protected fun decodeImage(imageSource: ImageSource): Bitmap? {
        if (imageSource.isBitmap()) {
            return imageSource.asBitmap()
        }
        if (imageSource.isResource()) {
            return this.decodeResource(imageSource.asResource())
        }
        if (imageSource.isFilePath()) {
            return this.decodeFilePath(imageSource.asFilePath())
        }
        return if (imageSource.isUrl()) {
            this.decodeUrl(imageSource.asUrl())
        } else this.decodeUnrecognized(imageSource)
    }

    protected fun defaultBitmapFactoryOptions(): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inScaled = false // suppress default image scaling; load the image in its native dimensions
        return options
    }

    protected fun decodeResource(id: Int): Bitmap? {
        val options: BitmapFactory.Options = this.defaultBitmapFactoryOptions()
        return if (resources != null) BitmapFactory.decodeResource(resources, id, options) else null
    }

    protected fun decodeFilePath(pathName: String?): Bitmap? {
        val options = defaultBitmapFactoryOptions()
        return BitmapFactory.decodeFile(pathName, options)
    }

    @Throws(IOException::class)
    protected fun decodeUrl(urlString: String?): Bitmap? {
        var stream: InputStream? = null
        return try {
            val conn = URL(urlString).openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000
            stream = BufferedInputStream(conn.getInputStream())
            val options = defaultBitmapFactoryOptions()
            BitmapFactory.decodeStream(stream, null, options)
        } finally {
            WWUtil.closeSilently(stream)
        }
    }

    protected fun decodeUnrecognized(imageSource: ImageSource): Bitmap? {
        Logger.log(Logger.WARN, "Unrecognized image source \'$imageSource\'")
        return null
    }
}