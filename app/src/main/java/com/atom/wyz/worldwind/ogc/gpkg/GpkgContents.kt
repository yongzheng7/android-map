package com.atom.wyz.worldwind.ogc.gpkg

class GpkgContents() : GpkgEntry() {
    var tableName: String? = null

    var dataType: String? = null

    var identifier: String? = null

    var description: String? = null

    var lastChange: String? = null

    var minX = 0.0

    var minY = 0.0

    var maxX = 0.0

    var maxY = 0.0

    var srsId = 0

}