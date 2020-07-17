package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmsLayer : XmlModel() {

    // Properties of the Layer element
    open var layers: MutableList<WmsLayer> = ArrayList()

    open var name: String? = null

    open var title: String? = null

    open var description: String? = null

    open var keywordList: MutableList<String> = ArrayList()

    open var styles: MutableList<WmsStyle> = ArrayList<WmsStyle>()
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    field.addAll(parent.styles)
                    break
                }
                parent = parent.parent
            }
            return field
        }

    // The 1.3.0 Reference System
    open var crses: MutableList<String> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    field.addAll(parent.crses)
                }
                parent = parent.parent
            }
            return field
        }

    // The 1.1.1 Reference System
    open var srses: MutableList<String> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    field.addAll(parent.srses)
                }
                parent = parent.parent
            }
            return field
        }


    open var geographicBoundingBox: WmsGeographicBoundingBox? = null

    open var boundingBoxes: MutableList<WmsBoundingBox> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            val boundingBoxMap: MutableMap<String, WmsBoundingBox> =
                HashMap()
            while (parent != null) {
                if (parent is WmsLayer) {
                    for (boundingBox in parent.boundingBoxes) {
                        if (boundingBox.crs != null && !boundingBoxMap.containsKey(boundingBox.crs!!)) {
                            boundingBoxMap[boundingBox.crs!!] = boundingBox
                        }
                    }
                }
                parent = parent.parent
            }
            return ArrayList(boundingBoxMap.values)
        }

    // The 1.3.0 Dimension Property
    open var dimensions: MutableList<WmsDimension> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            val dimensionMap: MutableMap<String, WmsDimension> = HashMap()
            while (parent != null) {
                if (parent is WmsLayer) {
                    for (dimension in parent.dimensions) {
                        if (dimension.name != null && !dimensionMap.containsKey(dimension.name!!)) {
                            dimensionMap[dimension.name!!] = dimension
                        }
                    }
                }
                parent = parent.parent
            }
            return ArrayList(dimensionMap.values)
        }

    // The 1.1.1 Dimension Property
    open var extents: MutableList<WmsDimension> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    field.addAll(parent.extents)
                }
                parent = parent.parent
            }
            return field
        }

    open var attribution: WmsAttribution? = null
        get() {
            var actualAttribution = field
            var parent: XmlModel? = this.parent
            while (actualAttribution == null && parent != null) {
                if (parent is WmsLayer) {
                    actualAttribution = parent.attribution
                }
                parent = parent.parent
            }
            return actualAttribution
        }

    open var authorityUrls: MutableList<WmsAuthorityUrl> = ArrayList()
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    field.addAll(parent.authorityUrls)
                    break
                }
                parent = parent.parent
            }
            return field
        }

    open var identifiers: MutableList<WmsIdentifier> =
        ArrayList()

    open var metadataUrls: MutableList<WmsInfoUrl> =
        ArrayList()

    open var dataUrls: MutableList<WmsInfoUrl> = ArrayList()

    open var featureListUrls: MutableList<WmsInfoUrl> =
        ArrayList()

    // The 1.3.0 Scale Property
    open var maxScaleDenominator: Double? = null
        get() {
            var actualMaxScaleDenominator = field
            var parent = this.parent
            while (actualMaxScaleDenominator == null && parent != null) {
                if (parent is WmsLayer) {
                    actualMaxScaleDenominator = parent.maxScaleDenominator
                }
                parent = parent.parent
            }
            return actualMaxScaleDenominator
        }

    // The 1.3.0 Scale Property
    open var minScaleDenominator: Double? = null
        get() {
            var actualMinScaleDenominator = field
            var parent: XmlModel? = this.parent
            while (actualMinScaleDenominator == null && parent != null) {
                if (parent is WmsLayer) {
                    actualMinScaleDenominator = parent.minScaleDenominator
                }
                parent = parent.parent
            }
            return actualMinScaleDenominator
        }

    // The 1.1.1 Scale Property
    open var scaleHint: WmsScaleHint? = null
        get() {
            var parent: XmlModel? = this.parent
            while (parent != null) {
                if (parent is WmsLayer) {
                    val wmsLayer = parent
                    if (wmsLayer.scaleHint != null) {
                        return wmsLayer.scaleHint
                    }
                }
                parent = parent.parent
            }
            return WmsScaleHint() // to prevent NPE on chained calls
        }


    // Properties of the Layer attributes
    open var queryable = false

    open var cascaded: Int? = null
        get() {
            var actualCascade = field
            var parent: XmlModel? = this.parent
            while (actualCascade == null && parent != null) {
                if (parent is WmsLayer) {
                    actualCascade = parent.cascaded
                }
                parent = parent.parent
            }
            return actualCascade
        }

    open var opaque: Boolean? = null
        get() {
            var actualOpaque = field

            var parent: XmlModel? = this.parent

            while (actualOpaque == null && parent != null) {
                if (parent is WmsLayer) {
                    actualOpaque = parent.opaque
                }
                parent = parent.parent
            }

            return actualOpaque
        }

    open var noSubsets: Boolean? = null
        get() {
            var actualNoSubsets = field
            var parent: XmlModel? = this.parent
            while (actualNoSubsets == null && parent != null) {
                if (parent is WmsLayer) {
                    actualNoSubsets = parent.noSubsets
                }
                parent = parent.parent
            }
            return actualNoSubsets
        }

    open var fixedWidth: Int? = null
        get() {
            var actualFixedWidth = field
            var parent: XmlModel? = this.parent
            while (actualFixedWidth == null && parent != null) {
                if (parent is WmsLayer) {
                    actualFixedWidth = parent.fixedWidth
                }
                parent = parent.parent
            }
            return actualFixedWidth
        }

    open var fixedHeight: Int? = null
        get() {
            var actualFixedHeight = field
            var parent: XmlModel? = this.parent
            while (actualFixedHeight == null && parent != null) {
                if (parent is WmsLayer) {
                    actualFixedHeight = parent.fixedHeight
                }
                parent = parent.parent
            }
            return actualFixedHeight
        }

    open fun getNamedLayers(): List<WmsLayer> {
        val namedLayers: MutableList<WmsLayer> = ArrayList()
        if (this.name != null) namedLayers.add(this)
        for (layer in this.layers) {
            namedLayers.addAll(layer.getNamedLayers())
        }
        return namedLayers
    }

    open fun getStyle(name: String?): WmsStyle? {
        if (name == null || name.isEmpty()) {
            return null
        }
        for (style in this.styles) {
            if (style.name.equals(name)) return style
        }
        return null
    }

    open fun getGeographicBoundingBox(): Sector? {
        var actualGeographicBoundingBox = geographicBoundingBox
        var parent: XmlModel? = this.parent
        while (actualGeographicBoundingBox == null && parent != null) {
            if (parent is WmsLayer) {
                actualGeographicBoundingBox = parent.geographicBoundingBox
            }
            parent = parent.parent
        }
        return actualGeographicBoundingBox?.getGeographicBoundingBox()
    }

    open fun getReferenceSystems(): List<String>? {
        var rs = crses
        if (rs.isEmpty()) {
            rs = srses
        }
        return rs
    }

    open fun getCapability(): WmsCapability? {
        var model: XmlModel? = this
        while (model != null) {
            model = model.parent
            if (model is WmsCapability) {
                return model
            }
        }
        return null
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Layer" -> {
                layers.add((value as WmsLayer))
            }
            "Name" -> {
                name = value as String
            }
            "Title" -> {
                title = value as String
            }
            "Abstract" -> {
                description = value as String
            }
            "KeywordList" -> {
                keywordList.addAll((value as WmsKeywords).keywords)
            }
            "Style" -> {
                styles.add((value as WmsStyle))
            }
            "CRS" -> {
                crses.add((value as String))
            }
            "SRS" -> {
                srses.add((value as String))
            }
            "EX_GeographicBoundingBox" -> {
                geographicBoundingBox = value as WmsGeographicBoundingBox
            }
            "LatLonBoundingBox" -> {
                geographicBoundingBox = value as WmsGeographicBoundingBox
            }
            "BoundingBox" -> {
                boundingBoxes.add((value as WmsBoundingBox))
            }
            "Dimension" -> {
                dimensions.add((value as WmsDimension))
            }
            "Extent" -> {
                extents.add((value as WmsDimension))
            }
            "Attribution" -> {
                attribution = value as WmsAttribution
            }
            "AuthorityURL" -> {
                authorityUrls.add((value as WmsAuthorityUrl))
            }
            "Identifier" -> {
                identifiers.add((value as WmsIdentifier))
            }
            "MetadataURL" -> {
                metadataUrls.add((value as WmsInfoUrl))
            }
            "DataURL" -> {
                dataUrls.add((value as WmsInfoUrl))
            }
            "FeatureListURL" -> {
                featureListUrls.add((value as WmsInfoUrl))
            }
            "MinScaleDenominator" -> {
                minScaleDenominator = (value as String).toDouble()
            }
            "MaxScaleDenominator" -> {
                maxScaleDenominator = (value as String).toDouble()
            }
            "ScaleHint" -> {
                scaleHint = value as WmsScaleHint
            }
            "queryable" -> {
                queryable = java.lang.Boolean.parseBoolean(value as String)
            }
            "cascaded" -> {
                cascaded = (value as String).toInt()
            }
            "opaque" -> {
                opaque = java.lang.Boolean.parseBoolean(value as String)
            }
            "noSubsets" -> {
                noSubsets = java.lang.Boolean.parseBoolean(value as String)
            }
            "fixedWidth" -> {
                fixedWidth = (value as String).toInt()
            }
            "fixedHeight" -> {
                fixedHeight = (value as String).toInt()
            }
        }
    }
}