package com.atom.map.ogc

import com.atom.map.geom.Sector
import com.atom.map.geom.TileMatrixSet
import com.atom.map.util.Logger
import java.lang.IllegalArgumentException

class Wcs100ElevationCoverage :
    TiledElevationCoverage {

    constructor(
        sector: Sector ,
        numLevels : Int ,
        serviceAddress: String,
        coverage: String
    ) {

        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "invalidNumLevels")
            )
        }


        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.makeMessage(
                    "Wcs100ElevationCoverage",
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
        this.tileFactory = (Wcs100TileFactory(serviceAddress, coverage))

    }
}