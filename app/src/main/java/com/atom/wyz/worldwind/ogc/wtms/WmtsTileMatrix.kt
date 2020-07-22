package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.ogc.ows.OwsDescription

open class WmtsTileMatrix : OwsDescription() {
    open var identifier: String? = null

    open var limitIdentifier: String? = null

    open var scaleDenominator = 0.0

    open var topLeftCorner: String? = null

    open var tileWidth = 0

    open var tileHeight = 0

    open var matrixWidth = 0

    open var matrixHeight = 0


    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Identifier" -> {
                identifier = value as String
            }
            "ScaleDenominator" -> {
                scaleDenominator = (value as String).toDouble()
            }
            "TopLeftCorner" -> {
                topLeftCorner = value as String
            }
            "TileWidth" -> {
                tileWidth = (value as String).toInt()
            }
            "TileHeight" -> {
                tileHeight = (value as String).toInt()
            }
            "MatrixWidth" -> {
                matrixWidth = (value as String).toInt()
            }
            "MatrixHeight" -> {
                matrixHeight = (value as String).toInt()
            }
        }
    }

    override fun parseText(text: String) {
        limitIdentifier = text
    }
}