package com.atom.wyz.worldwind.util

import android.util.Log

class Logger {
    companion object {
        const val ERROR = Log.ERROR

        const val WARN = Log.WARN

        const val INFO = Log.INFO

        const val DEBUG = Log.DEBUG

        protected var TAG = "wyz.worldwind"

        fun isLoggable(priority: Int): Boolean {
            return Log.isLoggable(TAG, priority)
        }

        protected var messageTable = mutableMapOf<String, String>(
            "missingLocation" to "The specified location is null",
            "missingFactory" to "The factory is null",
            "missingOffset" to "The offset name is null",
            "missingCache" to  "The cache is null",
            "missingViewport" to  "The viewport is null",
            "missingCallback" to "The callback is null",
            "missingKey" to "The key is null",
            "missingLine" to "The line is null",
            "missingRunnable" to "The runnable is null",
            "missingResources" to "The resources argument is null",
            "missingContext" to "The context is null",
            "missingName" to "The name is null",
            "missingUrl" to "The url is null",
            "invalidResolution" to "The resolution is invalid",
            "missingConfig" to "The configuration is null",
            "missingCoordinateSystem" to "The coordinate system is null",
            "missingFormat" to "The format is null",
            "missingLayerNames" to "The layer names are null",
            "missingLevelSet" to "The level set is null",
            "missingServiceAddress" to "The service address is null",
            "missingVersion" to "The version is null",
            "missingPoint" to "The point is null",
            "missingWorldWindow" to "The world window is null",
            "invalidNumLevels" to "The number of levels is invalid",
            "invalidTileDelta" to "The tile delta is invalid",
            "invalidWidthOrHeight" to "The width or the height is invalid",
            "missingCamera" to "The camera is null",
            "missingImageFormat" to "The image format is null",
            "missingLookAt" to "The look-at is null",
            "missingTileUrlFactory" to "The tile url factory is null",
            "invalidCache" to "The cache is null",
            "missingPlane" to "The plane is null",
            "missingTileFactory" to "The tile factory is null",
            "missingRecognizer" to "The recognizer is null",
            "missingListener" to "The listener is null",
            "invalidBitmap" to "The bitmap is null or recycled",
            "missingPosition" to "The specified position is null",
            "missingResult" to "The specified result argument is null",
            "missingVector" to "The specified vector is null",
            "missingMatrix" to "The specified matrix is null",
            "missingList" to "The specified list is null or empty",
            "missingBuffer" to "The specified buffer is null or empty",
            "invalidWidth" to "The specified width is invalid",
            "invalidStride" to "The specified stride is invalid",
            "invalidHeight" to "The specified height is invalid",
            "invalidClass" to "The class is null or cannot be found",
            "invalidResource" to "The resource is invalid",
            "missingSource" to "The source is null",
            "missingLevel" to "The level is null",
            "missingTessellator" to "The tessellator is null",
            "missingTile" to "The tile is null",
            "errorReadingProgramSource" to "Error reading program sources",
            "missingPathName" to "The path name is null",
            "missingFrameStatistics" to "The frame metrics argument is null",
            "invalidCapacity" to "The capacity is less than 1",
            "missingColor" to "The color is null",
            "missingTypeface" to "The typeface is null",
            "missingGlobe" to "The globe is null"
        )

        fun log(level: Int, message: String) {
            if (Log.isLoggable(TAG, level)) {
                Log.println(level, TAG, message)
            }
        }

        fun log(priority: Int, message: String, tr: Throwable?) {
            if (Log.isLoggable(TAG, priority)) {
                Log.println(priority, TAG, message + '\n' + Log.getStackTraceString(tr))
            }
        }

        fun logMessage(level: Int, className: String, methodName: String, message: String): String {
            val msg = makeMessage(className, methodName, message)
            log(level, msg)
            return msg
        }

        fun logMessage(level: Int, className: String, methodName: String, message: String, tr: Throwable?): String {
            val msg: String = makeMessage(className, methodName, message)
            log(level, msg, tr)
            return msg
        }

        fun makeMessage(className: String, methodName: String, message: String): String {
            val sb = StringBuilder()
            val msg = messageTable[message]
            sb.append(className).append(".").append(methodName)
            sb.append(": ").append(msg ?: message)
            return sb.toString()
        }
    }
}