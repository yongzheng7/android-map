package com.atom.map.ogc.wtms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmtsContents : XmlModel() {

    open var layers: MutableList<WmtsLayer> = ArrayList<WmtsLayer>()

    open var tileMatrixSets: MutableList<WmtsTileMatrixSet> = ArrayList<WmtsTileMatrixSet>()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Layer" -> {
                layers.add(value as WmtsLayer)
            }
            "TileMatrixSet" -> {
                tileMatrixSets.add(value as WmtsTileMatrixSet)
            }
        }
    }
}