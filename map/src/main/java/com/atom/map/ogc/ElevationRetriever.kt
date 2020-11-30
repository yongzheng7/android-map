package com.atom.map.ogc

import com.atom.map.tiff.Subfile
import com.atom.map.tiff.Tiff
import com.atom.map.renderable.ImageSource
import com.atom.map.util.Logger
import com.atom.map.util.Retriever
import com.atom.map.util.WWUtil
import com.atom.map.util.pool.SynchronizedPool
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class ElevationRetriever(maxSimultaneousRetrievals: Int) :
    Retriever<ImageSource, Void, ShortBuffer>(maxSimultaneousRetrievals) {

    companion object{
        val PAGE_SIZE = 1024 * 16

        val BUFFER_SIZE = 1024 * 132
    }
     var pagePool: SynchronizedPool<ByteArray> = SynchronizedPool()

     var bufferPool: SynchronizedPool<ByteBuffer> = SynchronizedPool()



    override fun retrieveAsync(
        key: ImageSource,
        options: Void?,
        callback: Callback<ImageSource, Void, ShortBuffer>
    ) {
        try {
            val buffer = this.decodeCoverage(key)
            if (buffer != null) {
                callback.retrievalSucceeded(this, key, options, buffer)
            } else {
                callback.retrievalFailed(this, key, null) // failed but no exception
            }
        } catch (logged: Throwable) {
            callback.retrievalFailed(this, key, logged) // failed with exception
        }
    }

    @Throws(IOException::class)
    protected fun decodeCoverage(imageSource: ImageSource): ShortBuffer? {
        return if (imageSource.isUrl()) {
            this.decodeUrl(imageSource.asUrl())
        } else this.decodeUnrecognized(imageSource)
    }

    @Throws(IOException::class)
    protected fun decodeUrl(urlString: String?): ShortBuffer? {
        var stream: InputStream? = null
        return try {
            val conn = URL(urlString).openConnection()
            conn.connectTimeout = 3000
            conn.readTimeout = 30000
            stream = BufferedInputStream(conn.getInputStream())
            val contentType = conn.contentType
            return if (contentType.equals("application/bil16", ignoreCase = true)) {
                this.readInt16Data(stream)
            } else if (contentType.equals("image/tiff", ignoreCase = true)) {
                this.readTiffData(stream)
            } else {
                throw RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "ElevationRetriever",
                        "decodeUrl",
                        "Format not supported"
                    )
                )
            }
        } finally {
            WWUtil.closeSilently(stream)
        }
    }

    protected fun decodeUnrecognized(imageSource: ImageSource): ShortBuffer? {
        Logger.log(Logger.WARN, "Unrecognized image source \'$imageSource\'")
        return null
    }

    @Throws(IOException::class)
    protected fun readTiffData(stream: InputStream): ShortBuffer? {
        var tiffBuffer = bufferPool.acquire()
        if (tiffBuffer == null) {
            tiffBuffer = ByteBuffer.allocate(BUFFER_SIZE)
        }
        tiffBuffer!!.clear()
        val buffer = bufferStream(stream, tiffBuffer)
        val tiff = Tiff(buffer)
        val subfile: Subfile = tiff.getSubfiles().get(0)
        // check that the format of the subfile matches our supported data types
        return if (this.isTiffFormatSupported(subfile)) {
            val dataSize: Int = subfile.getDataSize()
            val result: ByteBuffer = subfile.getData(ByteBuffer.allocate(dataSize))
            result.clear()
            bufferPool.release(tiffBuffer)
            result.asShortBuffer()
        } else {
            throw java.lang.RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "ElevationRetriever",
                    "readTiffData",
                    "Tiff file format not supported"
                )
            )
        }
    }

    protected fun isTiffFormatSupported(subfile: Subfile): Boolean {
        return subfile.sampleFormat.get(0) == Tiff.TWOS_COMP_SIGNED_INT &&
                subfile.bitsPerSample.get(0) == 16 &&
                subfile.samplesPerPixel == 1 &&
                subfile.compression == 1
    }

    @Throws(IOException::class)
    protected fun readInt16Data(stream: InputStream): ShortBuffer {
        return bufferStream(stream, ByteBuffer.allocate(BUFFER_SIZE)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
    }

    @Throws(IOException::class)
    protected fun bufferStream(
        stream: InputStream,
        finalbuffer: ByteBuffer
    ): ByteBuffer {
        var buffer = finalbuffer
        var page = pagePool.acquire()
        if (page == null) {
            page = ByteArray(PAGE_SIZE)
        }
        var readCount: Int
        while (stream.read(page, 0, page.size).also { readCount = it } != -1) {
            if (readCount > buffer.remaining()) {
                val newBuffer =
                    ByteBuffer.allocate(buffer.capacity() + page.size)
                newBuffer.put(buffer.flip() as ByteBuffer)
                buffer = newBuffer
            }
            buffer.put(page, 0, readCount)
        }
        buffer.flip()
        pagePool.release(page)
        return buffer
    }

}