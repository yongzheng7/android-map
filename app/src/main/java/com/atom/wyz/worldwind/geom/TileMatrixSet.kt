package com.atom.wyz.worldwind.geom

import java.util.*

class TileMatrixSet {
    companion object{
        fun fromTilePyramid(
            sector: Sector,
            finalmatrixWidth: Int,
            finalmatrixHeight: Int,
            tileWidth: Int,
            tileHeight: Int,
            numLevels: Int
        ): TileMatrixSet {
            var matrixWidth = finalmatrixWidth
            var matrixHeight = finalmatrixHeight
            val tileMatrices = ArrayList<TileMatrix>()
            for (idx in 0 until numLevels) {
                val matrix = TileMatrix()
                matrix.sector.set(sector)
                matrix.ordinal = idx
                matrix.matrixWidth = matrixWidth
                matrix.matrixHeight = matrixHeight
                matrix.tileWidth = tileWidth
                matrix.tileHeight = tileHeight
                tileMatrices.add(matrix)
                matrixWidth *= 2
                matrixHeight *= 2
            }
            return TileMatrixSet(sector, tileMatrices)
        }

    }
    var sector = Sector()

    var entries = arrayOfNulls<TileMatrix>(0)

    constructor() {}

    constructor(
        sector: Sector,
        tileMatrixList: List<TileMatrix>
    ) {
        this.sector.set(sector)
        entries = tileMatrixList.toTypedArray()
    }

    fun count(): Int = entries.size

    fun matrix(index: Int): TileMatrix? {
        return if (index < 0 || index >= entries.size) {
            null
        } else {
            entries[index]
        }
    }

    fun indexOfMatrixNearest(degreesPerPixel: Double): Int {
        var nearestIdx = -1
        var nearestDelta2 = Double.POSITIVE_INFINITY
        var idx = 0
        val len = entries.size
        while (idx < len) {
            val delta = entries[idx]!!.degreesPerPixel() - degreesPerPixel
            val delta2 = delta * delta
            if (nearestDelta2 > delta2) {
                nearestDelta2 = delta2
                nearestIdx = idx
            }
            idx++
        }
        return nearestIdx
    }
}