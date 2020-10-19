package com.atom.map.ogc.wtms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmtsTileMatrixSetLimits : XmlModel() {
    open var tileMatrixLimits: MutableList<WmtsTileMatrixLimits> =
        ArrayList<WmtsTileMatrixLimits>()


    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "TileMatrixLimits" -> {
                tileMatrixLimits.add(value as WmtsTileMatrixLimits)
            }
        }
    }
}