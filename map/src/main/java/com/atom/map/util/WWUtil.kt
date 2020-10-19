package com.atom.map.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class WWUtil {
    companion object {

        /**
         * Closes a specified Closeable, suppressing any checked exceptions. This has no effect if the closeable is null.
         *
         * @param closeable the object to close, may be null in which case this does nothing
         */
        fun closeSilently(closeable: Closeable?) {
            if (closeable == null) return
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) { // silently ignore checked exceptions
            }
        }

        /**
         * Determines whether or not the specified string represents a URL. This returns false if the string is null.
         *
         * @param string the string in question
         *
         * @return true if the string represents a URL, otherwise false
         */
        fun isUrlString(string: String?): Boolean {
            return if (string == null) {
                false
            } else try {
                URL(string)
                true // no exception; the string is probably a valid URL
            } catch (ignored: MalformedURLException) {
                false // silently ignore the exception
            }
        }


        fun readResourceAsText(resource: Resources?, @RawRes id: Int): String {
            if (resource == null) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "WWUtil", "readResourceAsBitmap", "missingResource"))
            }
            var reader: BufferedReader? = null
            return try {
                val `in` = resource.openRawResource(id)
                reader = BufferedReader(InputStreamReader(`in`))
                val sb = StringBuilder()
                var line = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                sb.toString()
            } finally {
                closeSilently(reader)
            }
        }

        fun readResourceAsBitmap(resource: Resources?, @DrawableRes id: Int): Bitmap? {
            if (resource == null) {
                throw IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "WWUtil", "readResourceAsBitmap", "missingResource"))
            }
            val options = BitmapFactory.Options()
            options.inScaled = false // load the bitmap in its native dimensions
            return BitmapFactory.decodeResource(resource, id, options)
        }
    }
}