package com.atom.map.ogc.wtms

import com.atom.map.util.xml.XmlModel

open class WmtsTileMatrixSetLink : XmlModel() {

    open var identifier: String? = null

    open var tileMatrixSetLimits: WmtsTileMatrixSetLimits? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "TileMatrixSet" -> {
                identifier = (value as WmtsTileMatrixSet).linkIdentifier
            }
            "TileMatrixSetLimits" -> {
                tileMatrixSetLimits = value as WmtsTileMatrixSetLimits
            }
        }
    }
}