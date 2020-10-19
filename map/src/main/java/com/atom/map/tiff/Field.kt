package com.atom.map.tiff

import java.nio.ByteBuffer

class Field {
    /**
     * The [Subfile] which contains this entry.
     */
    var subfile: Subfile? = null

    /**
     * The byte offset from the beginning of the original file.
     */
    var offset = 0

    /**
     * The Tiff specification field tag.
     */
    var tag = 0

    /**
     * The Tiff specification field type.
     */
    var type: Type? = null

    /**
     * The Tiff specification length of the field in the units specified of [Field.type].
     */
    var count = 0

    /**
     * Data offset of the field. The data starts on a word boundary, thus the dword should be even. The data for the
     * field may be anywhere in the file, even after the image data. If the data size is less or equal to 4 bytes
     * (determined by the field type and length), then this offset is not a offset but instead the data itself, to save
     * space. If the data size is less than 4 bytes, the data is stored left-justified within the 4 bytes of the offset
     * field.
     */
    var dataOffset = 0

    /**
     * The data associated with this field. This ByteBuffers 0 position should correspond to the offset position in the
     * complete buffer of the Tiff and the limit should correspond to the end of the data associated with this field.
     * Use the [Field.sliceBuffer] method to populate once all other field properites have been
     * populated.
     */
    protected var data: ByteBuffer? = null

    /**
     * Slices the provided ByteBuffer, sets the byte order to the original, and sets the limit to the current position
     * plus the amount needed to view the data indicated by this field.
     *
     * @param original
     */
    fun sliceBuffer(original: ByteBuffer) {
        val originalLimit = original.limit()
        val sizeInBytes = type?.getSizeInBytes() ?:0
        original.limit(dataOffset + sizeInBytes * count)
        data = original.slice().order(original.order())
        original.limit(originalLimit)
    }

    fun getDataBuffer(): ByteBuffer? {
        data!!.rewind()
        return data
    }
}