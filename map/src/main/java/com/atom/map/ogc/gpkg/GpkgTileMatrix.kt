package com.atom.map.ogc.gpkg

class GpkgTileMatrix : GpkgEntry() {

     lateinit var tableName: String

     var zoomLevel = 0

     var matrixWidth = 0

     var matrixHeight = 0

     var tileWidth = 0

     var tileHeight = 0

     var pixelXSize = 0.0

     var pixelYSize = 0.0
}