package com.atom.wyz.worldwind.formats.tiff

import com.atom.wyz.worldwind.util.Logger
import java.nio.ByteBuffer
import java.util.*

open class Subfile {
    companion object{
        private fun floorDiv(x: Int, y: Int): Int {
            var r = x / y
            // if the signs are different and modulo not zero, round down
            if (x xor y < 0 && r * y != x) {
                r--
            }
            return r
        }
    }

    /**
     * The parent Tiff which contains this Subfile.
     */
    lateinit var tiff: Tiff

    /**
     * The Tiff absolute file offset position of this Subfile.
     */
     var offset = 0

    /**
     * The [Field] associated with this Subfile and provided in the Image File Directory (IFD).
     */
     var fields: MutableMap<Int, Field> = HashMap()

    // Minimum Required Tags to support Bi-level and Gray-scale Tiffs

    // Minimum Required Tags to support Bi-level and Gray-scale Tiffs
    // 254 - note this is a bit flag not a signed integer type
     var newSubfileType = 0

    // 256
     var imageWidth = 0

    // 257
     var imageLength = 0

    // 258
    var bitsPerSample = intArrayOf(1)

    // 259
     var compression = 1

    // 262
     var photometricInterpretation = 0

    // 277
     var samplesPerPixel = 1

    // 282
     var xResolution = 0.0

    // 283
     var yResolution = 0.0

    // 284
     var planarConfiguration = 1

    // 296
     var resolutionUnit = 2

    // Strip & Tile Image Data
    // 273
     var stripOffsets: IntArray ?= null

    // 279
     var stripByteCounts: IntArray ?= null

    // 278
     var rowsPerStrip = -0x1

    // 317
     var compressionPredictor = 1

    // 324
     var tileOffsets: IntArray?= null

    // 325
     var tileByteCounts: IntArray?= null

    // 322
     var tileWidth = 0

    // 323
     var tileLength = 0

    // 339
    var sampleFormat = intArrayOf(Tiff.UNSIGNED_INT)

    constructor() {}

    constructor(tiff: Tiff, offset: Int) {
        this.tiff = tiff
        this.offset = offset
        val entries = Tiff.readWord(tiff.buffer)
        for (i in 0 until entries) {
            val field = Field()
            field.subfile = this
            field.offset = tiff.buffer.position()
            field.tag = Tiff.readWord(tiff.buffer)
            field.type = Type.decode(Tiff.readWord(tiff.buffer))
            field.count = Tiff.readLimitedDWord(tiff.buffer)

            // Check if the data is available in the last four bytes of the field entry or if we need to read the pointer
            val size = field.count * field.type!!.getSizeInBytes()
            if (size > 4) {
                field.dataOffset = Tiff.readLimitedDWord(tiff.buffer)
            } else {
                field.dataOffset = tiff.buffer.position()
            }
            tiff.buffer.position(field.dataOffset)
            field.sliceBuffer(tiff.buffer)
            tiff.buffer.position(field.offset + 12) // move the buffer position to the end of the field
            fields[field.tag] = field
        }
        this.populateDefinedFields()
    }

    protected open fun populateDefinedFields() {
        var field = fields[Tiff.NEW_SUBFILE_TYPE_TAG]
        if (field != null) {
            newSubfileType = Tiff.readLimitedDWord(field.getDataBuffer()!!)
        }
        field = fields[Tiff.IMAGE_WIDTH_TAG]
        if (field != null) {
            if (field.type === Type.USHORT) {
                imageWidth = Tiff.readWord(field.getDataBuffer()!!)
            } else if (field.type === Type.ULONG) {
                imageWidth = Tiff.readLimitedDWord(field.getDataBuffer()!!)
            } else {
                throw RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateDefinedFields",
                        "invalid image width type"
                    )
                )
            }
        } else {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateDefinedFields",
                    "invalid tiff format - image width missing"
                )
            )
        }
        field = fields[Tiff.IMAGE_LENGTH_TAG]
        if (field != null) {
            if (field.type === Type.USHORT) {
                imageLength = Tiff.readWord(field.getDataBuffer()!!)
            } else if (field.type === Type.ULONG) {
                imageLength = Tiff.readLimitedDWord(field.getDataBuffer()!!)
            } else {
                throw RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateDefinedFields",
                        "invalid image length type"
                    )
                )
            }
        } else {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateDefinedFields",
                    "invalid tiff format - image length missing"
                )
            )
        }
        field = fields[Tiff.BITS_PER_SAMPLE_TAG]
        if (field != null) {
            bitsPerSample = IntArray(field.count)
            for (i in 0 until field.count) {
                bitsPerSample[i] = Tiff.readWord(field.getDataBuffer()!!)
            }
        }
        field = fields[Tiff.COMPRESSION_TAG]
        if (field != null) {
            compression = Tiff.readWord(field.getDataBuffer()!!)
            if (compression != 1) {
                throw UnsupportedOperationException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateDefineFields",
                        "compressed images are not supported"
                    )
                )
            }
        }
        field = fields[Tiff.PHOTOMETRIC_INTERPRETATION_TAG]
        if (field != null) {
            photometricInterpretation = Tiff.readWord(field.getDataBuffer()!!)
        } else {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populatedDefinedFields",
                    "photometricinterpretation missing"
                )
            )
        }
        field = fields[Tiff.SAMPLES_PER_PIXEL_TAG]
        if (field != null) {
            samplesPerPixel = Tiff.readWord(field.getDataBuffer()!!)
        }
        field = fields[Tiff.X_RESOLUTION_TAG]
        if (field != null) {
            xResolution = this.calculateRational(field.getDataBuffer())
        }
        field = fields[Tiff.Y_RESOLUTION_TAG]
        if (field != null) {
            yResolution = this.calculateRational(field.getDataBuffer())
        }
        field = fields[Tiff.PLANAR_CONFIGURATION_TAG]
        if (field != null) {
            planarConfiguration = Tiff.readWord(field.getDataBuffer()!!)
            if (planarConfiguration != 1) {
                throw UnsupportedOperationException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateDefinedFields",
                        "planar configurations other than 1 are not supported"
                    )
                )
            }
        }
        field = fields[Tiff.RESOLUTION_UNIT_TAG]
        if (field != null) {
            resolutionUnit = Tiff.readWord(field.getDataBuffer()!!)
        }
        if (fields.containsKey(Tiff.STRIP_OFFSETS_TAG)) {
            this.populateStripFields()
        } else if (fields.containsKey(Tiff.TILE_OFFSETS_TAG)) {
            this.populateTileFields()
        } else {
            throw RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateDefinedFields",
                    "no image offsets provided"
                )
            )
        }
        field = fields[Tiff.COMPRESSION_PREDICTOR_TAG]
        if (field != null) {
            compressionPredictor = Tiff.readWord(field.getDataBuffer()!!)
        }
        field = fields[Tiff.SAMPLE_FORMAT_TAG]
        if (field != null) {
            sampleFormat = IntArray(field.count)
            for (i in 0 until field.count) {
                sampleFormat[i] = Tiff.readWord(field.getDataBuffer()!!)
            }
        }
    }

    protected open fun populateStripFields() {
        var field = fields[Tiff.STRIP_OFFSETS_TAG]
        if (field != null) {
            stripOffsets = IntArray(field.count)
            val data = field.getDataBuffer()
            for (i in stripOffsets!!.indices) {
                if (field.type === Type.USHORT) {
                    stripOffsets!![i] = Tiff.readWord(data!!)
                } else if (field.type === Type.ULONG) {
                    stripOffsets!![i] = Tiff.readLimitedDWord(data!!)
                } else {
                    throw java.lang.RuntimeException(
                        Logger.logMessage(
                            Logger.ERROR,
                            "Strip",
                            "populateStripFields",
                            "invalid offset type"
                        )
                    )
                }
            }
        } else {
            throw java.lang.RuntimeException("invalid tiff format - stripOffsets missing")
        }
        field = fields[Tiff.ROWS_PER_STRIP_TAG]
        if (field != null) {
            if (field.type === Type.USHORT) {
                rowsPerStrip = Tiff.readWord(field.getDataBuffer()!!)
            } else if (field.type === Type.ULONG) {
                rowsPerStrip = Tiff.readLimitedDWord(field.getDataBuffer()!!)
            } else {
                throw java.lang.RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Strip",
                        "populateStripFields",
                        "invalid rowsperstrip type"
                    )
                )
            }
        }
        field = fields[Tiff.STRIP_BYTE_COUNTS_TAG]
        if (field != null) {
            stripByteCounts = IntArray(field.count)
            val data = field.getDataBuffer()
            for (i in stripByteCounts!!.indices) {
                if (field.type === Type.USHORT) {
                    stripByteCounts!![i] = Tiff.readWord(data!!)
                } else if (field.type === Type.ULONG) {
                    stripByteCounts!![i] = Tiff.readLimitedDWord(data!!)
                } else {
                    throw java.lang.RuntimeException(
                        Logger.logMessage(
                            Logger.ERROR,
                            "Strip",
                            "populateStripFields",
                            "invalid stripByteCounts type"
                        )
                    )
                }
            }
        } else {
            throw java.lang.RuntimeException("invalid tiff format - stripByteCounts missing")
        }
    }

    protected open fun populateTileFields() {
        var field = fields[Tiff.TILE_OFFSETS_TAG]
        if (field != null) {
            tileOffsets = IntArray(field.count)
            val data = field.getDataBuffer()
            for (i in tileOffsets!!.indices) {
                if (field.type === Type.USHORT) {
                    tileOffsets!![i] = Tiff.readWord(data!!)
                } else if (field.type === Type.ULONG) {
                    tileOffsets!![i] = Tiff.readLimitedDWord(data!!)
                } else {
                    throw java.lang.RuntimeException(
                        Logger.logMessage(
                            Logger.ERROR,
                            "Subfile",
                            "populateTileFields",
                            "invalid offset type"
                        )
                    )
                }
            }
        } else {
            throw java.lang.RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "missing offset")
            )
        }
        field = fields[Tiff.TILE_BYTE_COUNTS_TAG]
        if (field != null) {
            tileByteCounts = IntArray(field.count)
            val data = field.getDataBuffer()
            for (i in tileByteCounts!!.indices) {
                if (field.type === Type.USHORT) {
                    tileByteCounts!![i] = Tiff.readWord(data!!)
                } else if (field.type === Type.ULONG) {
                    tileByteCounts!![i] = Tiff.readLimitedDWord(data!!)
                } else {
                    throw java.lang.RuntimeException(
                        Logger.logMessage(
                            Logger.ERROR,
                            "Subfile",
                            "populateTileFields",
                            "invalid tileByteCounts type"
                        )
                    )
                }
            }
        } else {
            throw java.lang.RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateTileFields",
                    "invalid tiff format - tileByteCounts missing"
                )
            )
        }
        field = fields[Tiff.TILE_WIDTH_TAG]
        if (field != null) {
            if (field.type === Type.USHORT) {
                tileWidth = Tiff.readWord(field.getDataBuffer()!!)
            } else if (field.type === Type.ULONG) {
                tileWidth = Tiff.readLimitedDWord(field.getDataBuffer()!!)
            } else {
                throw java.lang.RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateTileFields",
                        "invalid tileWidth type"
                    )
                )
            }
        } else {
            throw java.lang.RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateTileFields",
                    "missing tilewidth field"
                )
            )
        }
        field = fields[Tiff.TILE_LENGTH_TAG]
        if (field != null) {
            if (field.type === Type.USHORT) {
                tileLength = Tiff.readWord(field.getDataBuffer()!!)
            } else if (field.type === Type.ULONG) {
                tileLength = Tiff.readLimitedDWord(field.getDataBuffer()!!)
            } else {
                throw java.lang.RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "Subfile",
                        "populateTileFields",
                        "invalid tileLength type"
                    )
                )
            }
        } else {
            throw java.lang.RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "populateTileFields",
                    "missing tileLength field"
                )
            )
        }
    }

    protected open fun getTotalBytesPerPixel(): Int {
        var totalBytesPerSample = 0
        for (i in bitsPerSample.indices) {
            totalBytesPerSample += bitsPerSample[i]
        }
        return totalBytesPerSample / 8
    }

    protected open fun calculateRational(buffer: ByteBuffer?): Double {
        val numerator = Tiff.readDWord(buffer!!)
        val denominator = Tiff.readDWord(buffer)
        return (numerator / denominator).toDouble()
    }

    /**
     * Calculates the uncompressed data size. Should be used when preparing the ByteBuffer for write Tiff data in the
     * [Subfile.getData] method.
     *
     * @return the size in bytes of the uncompressed data
     */
    open fun getDataSize(): Int {
        var bytes = 0
        for (i in 0 until this.samplesPerPixel) {
            bytes += this.imageLength * this.imageWidth * this.bitsPerSample.get(i) / 8
        }
        return bytes
    }

    /**
     * Writes the uncompressed data from the Tiff data associated with the Subfile to the provided
     * ByteBuffer. The data copied to the provided buffer will use the original datas byte order and may override the
     * byte order specified by the provided buffer.
     *
     * @param result a ByteBuffer ready for the uncompressed Tiff data, should have a capacity of at least the return
     * value of [Subfile.getDataSize]
     *
     * @return the populated provided ByteBuffer
     */
    open fun getData(result: ByteBuffer): ByteBuffer {
        if (result.remaining() < getDataSize()) {
            throw java.lang.RuntimeException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Subfile",
                    "getUncompressedImage",
                    "inadequate buffer size"
                )
            )
        }
        // set the result ByteBuffer to our datas byte order
        result.order(tiff.buffer.order())

        // TODO handle compression
        if (fields.containsKey(Tiff.STRIP_OFFSETS_TAG)) {
            combineStrips(result)
        } else {
            combineTiles(result)
        }
        return result
    }

    protected open fun combineStrips(result: ByteBuffer) {
        // this works when the data is not compressed and may work when it is compressed as well
        for (i in stripOffsets!!.indices) {
            tiff.buffer.limit(stripOffsets!![i] + stripByteCounts!![i])
            tiff.buffer.position(stripOffsets!![i])
            result.put(tiff.buffer)
        }
        tiff.buffer.clear()
    }

    protected open fun combineTiles(result: ByteBuffer) {
        // this works when the data is not compressed, but it will cause problems if it is compressed and needs to be
        // decompressed as this detiles the tiles, each tile should be decompressed prior to this operation
        val tilesAcross = (imageWidth + tileWidth - 1) / tileWidth
        // int tilesDown = (this.imageLength + this.tileLength - 1) / this.tileLength;
        var currentTileRow = 0
        var currentTileCol = 0
        var tileIndex = 0
        var tilePixelRow = 0
        var tilePixelCol = 0
        var tilePixelIndex = 0
        val totalBytesPerSample = getTotalBytesPerPixel()
        var offsetIndex = 0
        for (pixelRow in 0 until imageLength) {
            currentTileRow = floorDiv(pixelRow, tileLength)
            tilePixelRow = pixelRow - currentTileRow * tileLength
            for (pixelCol in 0 until imageWidth) {
                currentTileCol = floorDiv(pixelCol, tileWidth)
                tileIndex = currentTileRow * tilesAcross + currentTileCol

                // offset byte row/column
                tilePixelCol = pixelCol - currentTileCol * tileWidth
                tilePixelIndex =
                    (tilePixelRow * tileWidth + tilePixelCol) * totalBytesPerSample
                offsetIndex = tileOffsets!![tileIndex] + tilePixelIndex
                tiff.buffer.limit(offsetIndex + totalBytesPerSample)
                tiff.buffer.position(offsetIndex)
                result.put(tiff.buffer)
            }
        }
        tiff.buffer.clear()
    }

}