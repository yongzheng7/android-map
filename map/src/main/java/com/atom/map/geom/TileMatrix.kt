package com.atom.map.geom

class TileMatrix {
    var sector = Sector()

    var ordinal = 0

    var matrixWidth = 0

    var matrixHeight = 0

    var tileWidth = 0

    var tileHeight = 0

    fun TileMatrix() {}

    fun degreesPerPixel(): Double {
        return sector.deltaLatitude() / (matrixHeight * tileHeight)
    }

    fun tileSector(row: Int, column: Int): Sector {
        val deltaLat = sector.deltaLatitude() / matrixHeight
        val deltaLon = sector.deltaLongitude() / matrixWidth
        val minLat: Double = sector.maxLatitude - deltaLat * (row + 1)
        val minLon: Double = sector.minLongitude + deltaLon * column
        return Sector(minLat, minLon, deltaLat, deltaLon)
    }
}