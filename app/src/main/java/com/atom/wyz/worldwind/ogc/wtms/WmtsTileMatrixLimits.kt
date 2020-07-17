package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmtsTileMatrixLimits : XmlModel() {

    open var tileMatrixIdentifier: String? = null

    open var minTileRow = 0

    open var maxTileRow = 0

    open var minTileCol = 0

    open var maxTileCol = 0

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "TileMatrix" -> {
                tileMatrixIdentifier = (value as WmtsTileMatrix).limitIdentifier
            }
            "MinTileRow" -> {
                minTileRow = (value as String).toInt()
            }
            "MaxTileRow" -> {
                maxTileRow = (value as String).toInt()
            }
            "MinTileCol" -> {
                minTileCol = (value as String).toInt()
            }
            "MaxTileCol" -> {
                maxTileCol = (value as String).toInt()
            }
        }
    }
}