package com.atom.wyz.worldwind.layer.render

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.Retriever
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ImageRetriever(maxSimultaneousRetrievals : Int = 8 ) : Retriever<ImageSource, ImageOptions,Bitmap>(maxSimultaneousRetrievals) {

    var resources: Resources? = null

    @Throws(Exception::class)
    override fun retrieveAsync(key: ImageSource, options: ImageOptions?, callback: Callback<ImageSource, ImageOptions, Bitmap>) {
        this.decodeImage(key , options) ?.let{
            Logger.log(Logger.ERROR, "retrieveAsync Succeeded ----A-----")
            callback.retrievalSucceeded(this, key, options ,it)
            Logger.log(Logger.ERROR, "retrieveAsync Succeeded ----A1----")
        } ?:let{
            Logger.log(Logger.ERROR, "retrieveAsync    Failed ----B-----")
            callback.retrievalFailed(this, key, null) // failed but no exception
            Logger.log(Logger.ERROR, "retrieveAsync    Failed ----B1----")
        }
    }

    @Throws(Exception::class)
    protected fun decodeImage(imageSource: ImageSource, imageOptions: ImageOptions?): Bitmap? {
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

    protected fun decodeResource(id: Int, imageOptions: ImageOptions?): Bitmap? {
        val options: BitmapFactory.Options = this.bitmapFactoryOptions(imageOptions)
        return if (resources != null) BitmapFactory.decodeResource(resources, id, options) else null
    }

    protected fun decodeFilePath(pathName: String?, imageOptions: ImageOptions?): Bitmap? {
        val options = bitmapFactoryOptions(imageOptions)
        return BitmapFactory.decodeFile(pathName, options)
    }

    @Throws(Exception::class)
    protected fun decodeUrl(urlString: String?, imageOptions: ImageOptions?): Bitmap? {
        var stream: InputStream? = null
        try {
            val url = URL(urlString)
            val conn = url.openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000
            stream = BufferedInputStream(conn.getInputStream())
            val options = bitmapFactoryOptions(imageOptions)
            val decodeStream = BitmapFactory.decodeStream(stream, null, options)
            return decodeStream
        } finally {
            WWUtil.closeSilently(stream)
        }
    }

    protected fun decodeUnrecognized(imageSource: ImageSource): Bitmap? {
        return null
    }


    protected fun bitmapFactoryOptions(imageOptions: ImageOptions?): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inScaled = false // suppress default image scaling; load the image in its native dimensions
        imageOptions ?.let{
            when (imageOptions.imageFormat) {
                WorldWind.RGBA_8888 -> options.inPreferredConfig = Bitmap.Config.ARGB_8888
                WorldWind.RGB_565 -> options.inPreferredConfig = Bitmap.Config.RGB_565
            }
        }
        return options
    }
}