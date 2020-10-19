package com.atom.map.ogc.gpkg

class GpkgTileMatrixSet() : GpkgEntry() {
    lateinit var tableName: String

    var srsId = 0

    var minX = 0.0

    var minY = 0.0

    var maxX = 0.0

    var maxY = 0.0
}