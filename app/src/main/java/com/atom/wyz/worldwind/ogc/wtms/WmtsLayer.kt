package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmtsLayer : XmlModel() {

    open var identifier: String? = null

    open var boundingBoxes: MutableList<OwsBoundingBox> =
        ArrayList()

    open var wgs84BoundingBox: OwsWgs84BoundingBox? = null

    open var metadata: MutableList<WmtsElementLink> =
        ArrayList()

    open var styles: MutableList<WmtsStyle> = ArrayList<WmtsStyle>()

    open var formats: MutableList<String> =
        ArrayList()

    open var infoFormats: MutableList<String> =
        ArrayList()

    open var tileMatrixSetLinks: MutableList<WmtsTileMatrixSetLink> =
        ArrayList<WmtsTileMatrixSetLink>()

    open var resourceUrls: MutableList<WmtsResourceUrl> =
        ArrayList<WmtsResourceUrl>()

    open var dimensions: MutableList<WmtsDimension> =
        ArrayList<WmtsDimension>()

    open fun getCapabilities(): WmtsCapabilities? {
        var parent: XmlModel? = this.parent
        while (parent != null) {
            if (parent is WmtsCapabilities) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    open fun getLayerSupportedTileMatrixSets(): List<WmtsTileMatrixSet>? {
        val associatedTileMatrixSets: MutableList<WmtsTileMatrixSet> =
            ArrayList<WmtsTileMatrixSet>()
        for (tileMatrixSetLink in this.tileMatrixSetLinks) {
            getCapabilities() ?.getTileMatrixSets() ?.forEach {
                if (it.identifier.equals(tileMatrixSetLink.identifier)) {
                    associatedTileMatrixSets.add(it)
                }
            }
        }
        return associatedTileMatrixSets
    }

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "WGS84BoundingBox" -> {
                wgs84BoundingBox = value as OwsWgs84BoundingBox
            }
            "Identifier" -> {
                identifier = value as String
            }
            "Metadata" -> {
                metadata.add((value as WmtsElementLink))
            }
            "Style" -> {
                styles.add(value as WmtsStyle)
            }
            "Format" -> {
                formats.add((value as String))
            }
            "InfoFormat" -> {
                infoFormats.add((value as String))
            }
            "TileMatrixSetLink" -> {
                tileMatrixSetLinks.add(value as WmtsTileMatrixSetLink)
            }
            "ResourceURL" -> {
                resourceUrls.add(value as WmtsResourceUrl)
            }
            "BoundingBox" -> {
                boundingBoxes.add((value as OwsBoundingBox))
            }
            "Dimension" -> {
                dimensions.add(value as WmtsDimension)
            }
        }
    }
}