package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.TileMatrixSet
import com.atom.wyz.worldwind.util.Logger

class Wcs201ElevationCoverage : TiledElevationCoverage {

    constructor(
        sector: Sector,
        numLevels: Int,
        serviceAddress: String,
        coverage: String
    ) {
        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "invalidNumLevels")
            )
        }
        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage(
                    "Wcs201ElevationCoverage",
                    "constructor",
                    "The number of levels must be greater than 0"
                )
            )
        }
        val matrixWidth = if (sector.isFullSphere()) 2 else 1
        val matrixHeight = 1
        val tileWidth = 256
        val tileHeight = 256
        this.tileMatrixSet = (
            TileMatrixSet.fromTilePyramid(
                sector,
                matrixWidth,
                matrixHeight,
                tileWidth,
                tileHeight,
                numLevels
            )
        )
        this.tileFactory = (Wcs201TileFactory(serviceAddress, coverage))
    }
}