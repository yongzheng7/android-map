package com.atom.wyz.worldwind.formats.tiff

import androidx.annotation.IntDef
import com.atom.wyz.worldwind.util.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class Tiff {
    companion object{
        const val NEW_SUBFILE_TYPE_TAG = 254

        const val IMAGE_WIDTH_TAG = 256

        const val IMAGE_LENGTH_TAG = 257

        const val BITS_PER_SAMPLE_TAG = 258

        const val COMPRESSION_TAG = 259

        const val PHOTOMETRIC_INTERPRETATION_TAG = 262

        const val SAMPLES_PER_PIXEL_TAG = 277

        const val X_RESOLUTION_TAG = 282

        const val Y_RESOLUTION_TAG = 283

        const val PLANAR_CONFIGURATION_TAG = 284

        const val RESOLUTION_UNIT_TAG = 296

        const val STRIP_OFFSETS_TAG = 273

        const val STRIP_BYTE_COUNTS_TAG = 279

        const val ROWS_PER_STRIP_TAG = 278

        const val COMPRESSION_PREDICTOR_TAG = 317

        const val TILE_OFFSETS_TAG = 324

        const val TILE_BYTE_COUNTS_TAG = 325

        const val TILE_WIDTH_TAG = 322

        const val TILE_LENGTH_TAG = 323

        const val SAMPLE_FORMAT_TAG = 339

        ///////////////////////////////////////////////////////////////////////////////////////////

        const val UNSIGNED_INT = 1

        const val TWOS_COMP_SIGNED_INT = 2

        const val FLOATING_POINT = 3

        const val UNDEFINED = 4


        fun readWord(buffer: ByteBuffer): Int {
            return buffer.short.toInt() and 0xFFFF
        }

        fun readDWord(buffer: ByteBuffer): Long {
            return buffer.int.toLong() and 0xFFFFFFFFL
        }

        fun readLimitedDWord(buffer: ByteBuffer): Int {
            val `val` = readDWord(buffer)
            return if (`val` > Int.MAX_VALUE) {
                throw java.lang.RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Tiff",
                        "readLimitedDWord",
                        "value exceeds signed integer range"
                    )
                )
            } else {
                `val`.toInt()
            }
        }
    }

    /**
     * Tiff tags are the integer definitions of individual Image File Directories (IFDs) and set by the Tiff 6.0
     * specification. The tags defined here are a minimal set and not inclusive of the complete 6.0 specification.
     */
    @IntDef(NEW_SUBFILE_TYPE_TAG,
        IMAGE_WIDTH_TAG,
        IMAGE_LENGTH_TAG,
        BITS_PER_SAMPLE_TAG,
        COMPRESSION_TAG,
        PHOTOMETRIC_INTERPRETATION_TAG,
        SAMPLES_PER_PIXEL_TAG,
        X_RESOLUTION_TAG,
        Y_RESOLUTION_TAG,
        PLANAR_CONFIGURATION_TAG,
        RESOLUTION_UNIT_TAG,
        STRIP_OFFSETS_TAG,
        STRIP_BYTE_COUNTS_TAG,
        ROWS_PER_STRIP_TAG,
        COMPRESSION_PREDICTOR_TAG,
        TILE_OFFSETS_TAG,
        TILE_BYTE_COUNTS_TAG,
        TILE_WIDTH_TAG,
        TILE_LENGTH_TAG,
        SAMPLE_FORMAT_TAG)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class TIFF_TAGS

    @IntDef(UNSIGNED_INT, TWOS_COMP_SIGNED_INT, FLOATING_POINT, UNDEFINED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class SAMPLE_FORMAT


    /**
     * The [Subfile] contained within this Tiff.
     */
    private var subfiles: MutableList<Subfile> = ArrayList()
    /**
     * [ByteBuffer] facilitating the view of the underlying Tiff data buffer.
     */
    var buffer: ByteBuffer

    constructor(buffer: ByteBuffer) {
        this.buffer = buffer
        checkAndSetByteOrder()
    }


    protected fun checkAndSetByteOrder() {
        // check byte order
        buffer.clear()
        val posOne = buffer.get().toChar()
        val posTwo = buffer.get().toChar()
        if (posOne == 'I' && posTwo == 'I') {
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        } else if (posOne == 'M' && posTwo == 'M') {
            buffer.order(ByteOrder.BIG_ENDIAN)
        } else {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Tiff",
                    "checkAndSetByteOrder",
                    "Tiff byte order incompatible"
                )
            )
        }

        // check the version
        val version: Int = Tiff.readWord(buffer)
        if (version != 42) {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Tiff",
                    "checkAndSetByteOrder",
                    "Tiff version incompatible"
                )
            )
        }
    }
    fun getSubfiles(): List<Subfile> {
        if (subfiles.isEmpty()) {
            buffer.position(4)
            val ifdOffset = readLimitedDWord(buffer)
            parseSubfiles(ifdOffset)
        }
        return subfiles
    }

    protected fun parseSubfiles(offset: Int) {
        buffer.position(offset)
        val ifd = Subfile(this, offset)
        subfiles.add(ifd)
        // check if there are more IFDs
        val nextIfdOffset: Int = Tiff.readLimitedDWord(buffer)
        if (nextIfdOffset != 0) {
            buffer.position(nextIfdOffset)
            parseSubfiles(nextIfdOffset)
        }
    }
}