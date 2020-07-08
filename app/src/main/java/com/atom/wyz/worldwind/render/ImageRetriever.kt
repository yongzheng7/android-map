package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.Retriever
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class ImageRetriever(maxSimultaneousRetrievals : Int = 8 ) : Retriever<ImageSource, ImageOptions ,Bitmap>(maxSimultaneousRetrievals) {

    var resources: Resources? = null

    override fun retrieveAsync(key: ImageSource, options: ImageOptions?, callback: Callback<ImageSource,ImageOptions , Bitmap>) {
        Log.e("addTile" , "AsyncTask  run() -> retrieveAsync 1")
        try {
            this.decodeImage(key , options) ?.let{
                Log.e("addTile" , "AsyncTask  run() -> Succeeded")

                callback.retrievalSucceeded(this, key, options ,it)
            } ?:let{
                Log.e("addTile" , "AsyncTask  run() -> Failed")

                callback.retrievalFailed(this, key, null) // failed but no exception
            }
        } catch (logged: Throwable) {
            Log.e("addTile" , "AsyncTask  run() ->Throwable > Failed")

            callback.retrievalFailed(this, key, logged) // failed with exception
        }
    }

    @Throws(Exception::class)
    protected fun decodeImage(imageSource: ImageSource , imageOptions: ImageOptions?): Bitmap? {
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
        return if (imageSource.isUrl()) {
            this.decodeUrl(imageSource.asUrl(), imageOptions)
        } else this.decodeUnrecognized(imageSource)
    }

    protected fun decodeResource(id: Int, imageOptions: ImageOptions?): Bitmap? {
        val options: BitmapFactory.Options = this.bitmapFactoryOptions(imageOptions)
        return if (resources != null) BitmapFactory.decodeResource(resources, id, options) else null
    }

    protected fun decodeFilePath(pathName: String?, imageOptions: ImageOptions?): Bitmap? {
        val options = bitmapFactoryOptions(imageOptions)
        return BitmapFactory.decodeFile(pathName, options)
    }

    @Throws(IOException::class)
    protected fun decodeUrl(urlString: String?, imageOptions: ImageOptions?): Bitmap? {
        var stream: InputStream? = null
        try {
            Log.e("addTile" , "decodeUrl start 1  ${urlString}") ;
            val conn = URL(urlString).openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000
            stream = BufferedInputStream(conn.getInputStream())
            Log.e("addTile" , "decodeUrl start 2")
            val options = bitmapFactoryOptions(imageOptions)
            Log.e("addTile" , "decodeUrl start 21")
            val decodeStream = BitmapFactory.decodeStream(stream, null, options)
            Log.e("addTile" , "decodeUrl start 3")
            return decodeStream
        } catch (e : Throwable){
            Log.e("addTile" , "decodeUrl ------------Throwable") ;
            WWUtil.closeSilently(stream)
        } finally {
            Log.e("addTile" , "decodeUrl -----------------------finally") ;
            WWUtil.closeSilently(stream)
        }
        return null
    }

    protected fun decodeUnrecognized(imageSource: ImageSource): Bitmap? {
        Logger.log(Logger.WARN, "Unrecognized image source \'$imageSource\'")
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