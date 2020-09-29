package com.atom.wyz.worldwind.layer.render

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.util.*

class ImageSource {

    @IntDef(
        TYPE_BITMAP,
        TYPE_RESOURCE,
        TYPE_FILE_PATH,
        TYPE_URL,
        TYPE_UNRECOGNIZED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    public annotation class ImageType

    interface BitmapFactory {
        fun createBitmap(): Bitmap?
    }

    companion object {
        protected const val TYPE_UNRECOGNIZED = 0

        protected const val TYPE_BITMAP_FACTORY = 1

        protected const val TYPE_BITMAP = 2

        protected const val TYPE_RESOURCE = 3

        protected const val TYPE_FILE_PATH = 4

        protected const val TYPE_URL = 5

        protected val lineStippleFactories: HashMap<Any, BitmapFactory> = HashMap()

        fun fromBitmap(bitmap: Bitmap?): ImageSource {
            if (bitmap == null || bitmap.isRecycled) {
                throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ImageSource", "fromBitmap", "invalidBitmap")
                )
            }
            val imageSource: ImageSource =
                ImageSource()
            imageSource.source = bitmap
            imageSource.type =
                TYPE_BITMAP
            return imageSource
        }

        fun fromResource(@DrawableRes id: Int): ImageSource {
            val imageSource: ImageSource =
                ImageSource()
            imageSource.source = id
            imageSource.type =
                TYPE_RESOURCE
            return imageSource
        }

        fun fromFilePath(pathName: String?): ImageSource {
            if (pathName == null) {
                throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ImageSource", "fromFilePath", "missingPathName")
                )
            }
            val imageSource: ImageSource =
                ImageSource()
            imageSource.source = pathName
            imageSource.type =
                TYPE_FILE_PATH
            return imageSource
        }

        fun fromUrl(urlString: String?): ImageSource {
            if (urlString == null) {
                throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ImageSource", "fromUrl", "missingUrl")
                )
            }
            val imageSource: ImageSource =
                ImageSource()
            imageSource.source = urlString
            imageSource.type =
                TYPE_URL
            return imageSource
        }

        fun fromLineStipple(factor: Int, pattern: Short): ImageSource {
            val lfactor = (factor.toLong() and 0xFFFFFFFFL)
            val lpattern = (pattern.toLong() and 0xFFFFL)
            val key = lfactor shl 32 or lpattern
            var factory: BitmapFactory? = lineStippleFactories[key]

            if (factory == null) {
                factory =
                    LineStippleBitmapFactory(
                        factor,
                        pattern
                    )
                lineStippleFactories[key] = factory
            }
            val imageSource = ImageSource()
            imageSource.type =
                TYPE_BITMAP_FACTORY
            imageSource.source = factory
            return imageSource
        }

        fun fromObject(source: Any?): ImageSource {
            if (source == null) {
                throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ImageSource", "fromObject", "missingSource")
                )
            }
            return if (source is Bitmap) {
                fromBitmap(
                    source
                )
            } else if (source is BitmapFactory) {
                fromBitmapFactory(
                    source
                )
            } else if (source is Int) { // Android resource identifier, as generated by the aapt tool
                fromResource(
                    source
                )
            } else if (source is String && WWUtil.isUrlString(source)) {
                fromUrl(
                    source
                )
            } else if (source is String) {
                fromFilePath(
                    source as String?
                )
            } else {
                val imageSource =
                    ImageSource()
                imageSource.source = source
                imageSource.type =
                    TYPE_UNRECOGNIZED
                imageSource
            }
        }


        fun fromBitmapFactory(factory: BitmapFactory): ImageSource {
            val imageSource = ImageSource()
            imageSource.type = TYPE_BITMAP_FACTORY
            imageSource.source = factory
            return imageSource
        }

    }

    protected var source: Any? = null

    @ImageType
    protected var type =
        TYPE_UNRECOGNIZED

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: ImageSource = other as ImageSource
        return type == that.type && source?.equals(that.source) ?: false
    }

    override fun hashCode(): Int {
        return this.type + 31 * this.source.hashCode();
    }

    fun isBitmap(): Boolean {
        return type == TYPE_BITMAP
    }

    fun isBitmapFactory(): Boolean {
        return type == TYPE_BITMAP_FACTORY
    }

    fun isResource(): Boolean {
        return type == TYPE_RESOURCE
    }

    fun isFilePath(): Boolean {
        return type == TYPE_FILE_PATH
    }

    fun isUrl(): Boolean {
        return type == TYPE_URL
    }

    fun asBitmap(): Bitmap? {
        return if (type == TYPE_BITMAP) source as Bitmap? else null
    }

    @DrawableRes
    fun asResource(): Int {
        return if (type == TYPE_RESOURCE) source as Int else 0
    }

    fun asFilePath(): String? {
        return if (type == TYPE_FILE_PATH) source as String else null
    }

    fun asUrl(): String? {
        return if (type == TYPE_URL) source as String else null
    }

    fun asBitmapFactory(): BitmapFactory? {
        return if (type == TYPE_BITMAP_FACTORY) source as BitmapFactory? else null
    }

    fun asObject(): Any? {
        return source
    }

    override fun toString(): String {
        return "\n source=$source "
    }

    class LineStippleBitmapFactory(protected var factor: Int, protected var pattern: Short) :
        BitmapFactory {

         override fun createBitmap(): Bitmap {
            val transparent = Color.argb(0, 0, 0, 0)
            val white = Color.argb(255, 255, 255, 255)
            return if (factor <= 0) {
                val width = 16
                val pixels = IntArray(width)
                Arrays.fill(pixels, white)
                val bitmap = Bitmap.createBitmap(width, 1 /*height*/, Bitmap.Config.ARGB_4444)
                bitmap.setPixels(pixels, 0 /*offset*/, width /*stride*/, 0 /*x*/, 0 /*y*/, width, 1 /*height*/)
                bitmap
            } else {
                val width = factor * 16
                val pixels = IntArray(width)
                var pixel = 0
                for (bi in 0..15) {
                    val bit: Int = pattern.toInt() and (1 shl bi)
                    val color = if (bit == 0) transparent else white
                    for (fi in 0 until factor) {
                        pixels[pixel++] = color
                    }
                }
                val bitmap = Bitmap.createBitmap(width, 1 /*height*/, Bitmap.Config.ARGB_4444)
                bitmap.setPixels(pixels, 0 /*offset*/, width /*stride*/, 0 /*x*/, 0 /*y*/, width, 1 /*height*/)
                bitmap
            }
        }

        override fun toString(): String {
            return "LineStippleBitmapFactory factor=" + factor + ", pattern=" + Integer.toHexString(pattern.toInt() and 0xFFFF)
        }

    }


}