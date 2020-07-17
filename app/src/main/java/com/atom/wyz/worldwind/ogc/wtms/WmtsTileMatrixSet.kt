package com.atom.wyz.worldwind.ogc.wtms

import java.util.*

open class WmtsTileMatrixSet : OwsDescription() {

    open var identifier: String? = null

    open var linkIdentifier: String? = null

    open var supportedCrs: String? = null

    open var wellKnownScaleSet: String? = null

    open var boundingBox: OwsBoundingBox? = null

    open var tileMatrices: MutableList<WmtsTileMatrix> =
        ArrayList<WmtsTileMatrix>()

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Identifier" -> {
                identifier = value as String
            }
            "SupportedCRS" -> {
                supportedCrs = value as String
            }
            "WellKnownScaleSet" -> {
                wellKnownScaleSet = value as String
            }
            "BoundingBox" -> {
                boundingBox = value as OwsBoundingBox
            }
            "TileMatrix" -> {
                tileMatrices.add(value as WmtsTileMatrix)
            }
        }
    }

    override fun parseText(text: String) {
        linkIdentifier = text
    }
}