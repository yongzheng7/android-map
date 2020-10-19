package com.atom.map.ogc.gpkg

class GpkgTileUserData : GpkgEntry() {
     var id = 0

     var zoomLevel = 0

     var tileColumn = 0

     var tileRow = 0

     var tileData: ByteArray ?= null

}