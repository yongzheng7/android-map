package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.xml.DoubleModel
import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsLayerCapabilities : XmlModel {
    lateinit var layerAbstract: QName

    lateinit var attribution: QName

    lateinit var authorityUrl: QName

    lateinit var boundingBox: QName

    lateinit var crs: QName

    lateinit var dataUrl: QName

    lateinit var dimension: QName

    lateinit var extent: QName

//    lateinit QName extremeElevations;

    lateinit var featureListUrl: QName

    lateinit var geographicBoundingBox: QName

    lateinit var identifier: QName

    lateinit var keywordList: QName

    lateinit var keyword: QName

    lateinit var lastUpdate: QName

    lateinit var latLonBoundingBox // 1.1.1
            : QName

    lateinit var layer: QName

    lateinit var maxScaleDenominator: QName

    lateinit var metadataUrl: QName

    lateinit var minScaleDenominator: QName

    lateinit var name: QName

    lateinit var scaleHint: QName

    lateinit var srs: QName

    lateinit var style: QName

    lateinit var title: QName

    lateinit var queryable: QName

    lateinit var opaque: QName

    lateinit var noSubsets: QName

    lateinit var fixedWidth: QName

    lateinit var fixedHeight: QName

    lateinit var cascaded: QName

    constructor(namespaceURI: String?) : super(namespaceURI) {
        initialize()
    }

    private fun initialize() {
        layerAbstract = QName(this.namespaceUri, "Abstract")
        attribution = QName(this.namespaceUri, "Attribution")
        authorityUrl = QName(this.namespaceUri, "AuthorityURL")
        boundingBox = QName(this.namespaceUri, "BoundingBox")
        crs = QName(this.namespaceUri, "CRS")
        dataUrl = QName(this.namespaceUri, "DataURL")
        dimension = QName(this.namespaceUri, "Dimension")
        extent = QName(this.namespaceUri, "Extent")
        //        this.extremeElevations = new QName(this.namespaceUri, "ExtremeElevations");
        featureListUrl = QName(this.namespaceUri, "FeatureListURL")
        geographicBoundingBox =
            QName(this.namespaceUri, "EX_GeographicBoundingBox")
        identifier = QName(this.namespaceUri, "Identifier")
        keywordList = QName(this.namespaceUri, "KeywordList")
        keyword = QName(this.namespaceUri, "Keyword")
        lastUpdate = QName(this.namespaceUri, "LastUpdate")
        latLonBoundingBox =
            QName(this.namespaceUri, "LatLonBoundingBox")
        layer = QName(this.namespaceUri, "Layer")
        maxScaleDenominator =
            QName(this.namespaceUri, "MaxScaleDenominator")
        metadataUrl = QName(this.namespaceUri, "MetadataURL")
        minScaleDenominator =
            QName(this.namespaceUri, "MinScaleDenominator")
        name = QName(this.namespaceUri, "Name")
        scaleHint = QName(this.namespaceUri, "ScaleHint")
        srs = QName(this.namespaceUri, "SRS")
        style = QName(this.namespaceUri, "Style")
        title = QName(this.namespaceUri, "Title")
        queryable = QName("", "queryable")
        noSubsets = QName("", "noSubsets")
        fixedWidth = QName("", "fixedWidth")
        fixedHeight = QName("", "fixedHeight")
        cascaded = QName("", "cascaded")
        opaque = QName("", "opaque")
    }

    fun getNamedLayers(): List<WmsLayerCapabilities>? {
        val namedLayers: MutableList<WmsLayerCapabilities> =
            ArrayList()
        if (getName() != null) namedLayers.add(this)
        for (layer in getLayers()) {
            namedLayers.addAll(layer.getNamedLayers()!!)
        }
        return namedLayers
    }

    fun getLayerByName(name: String?): WmsLayerCapabilities? {
        if (name == null || name.isEmpty()) {
            return null
        }
        if (getName() != null && getName() == name) return this
        for (lc in getLayers()) {
            if (lc.getName() != null && lc.getName() == name) return lc
        }
        return null
    }

    fun getStyleByName(name: String?): WmsLayerStyle? {
        if (name == null || name.isEmpty()) {
            return null
        }
        for (style in getStyles()) {
            if (style.getName().equals(name)) return style
        }
        return null
    }

    fun getLastUpdate(): String? {
        return getChildCharacterValue(lastUpdate)
    }

    fun getMinScaleHint(): Double? {
        val o: Any? = getInheritedField(scaleHint)
        if (o != null && o is XmlModel) {
            return o.getDoubleAttributeValue(QName("", "min") , true)
        }
        return null
    }

    fun getMaxScaleHint(): Double? {
        val o: Any? = getInheritedField(scaleHint)
        if (o != null && o is XmlModel) {
            return o.getDoubleAttributeValue(QName("", "max"), true)
        }
        return null
    }

    fun getDimensions(): Set<WmsLayerDimension?>? {
        return getInheritedField(dimension) as Set<WmsLayerDimension>?
    }

    fun getExtents(): Set<WmsLayerExtent>? {
        val extents =
            this.getField(extent) as Set<WmsLayerExtent>?
        return extents ?: emptySet()
    }

    fun getCascaded(): Boolean? {
        return getBooleanAttributeValue(cascaded, true)
    }

    fun getFixedHeight(): Int? {
        return getIntegerAttributeValue(fixedHeight, true)
    }

    fun getFixedWidth(): Int? {
        return getIntegerAttributeValue(fixedWidth, true)
    }

    fun isNoSubsets(): Boolean? {
        return getBooleanAttributeValue(noSubsets, true)
    }

    fun isOpaque(): Boolean? {
        return getBooleanAttributeValue(opaque, true)
    }

    fun isQueryable(): Boolean? {
        return getBooleanAttributeValue(queryable, true)
    }

    fun getAttributions(): Set<WmsLayerAttribution>? {
        val attributions =
            this.getInheritedField(attribution) as Set<WmsLayerAttribution>?
        return attributions ?: emptySet()
    }

    fun getAuthorityUrls(): Set<WmsAuthorityUrl>? {
        val authorityUrls: MutableSet<WmsAuthorityUrl> = mutableSetOf()
        getAdditiveInheritedField(authorityUrl, authorityUrls)
        return authorityUrls
    }

    fun getIdentifiers(): Set<WmsLayerIdentifier>? {
        val identifiers =
            this.getInheritedField(identifier) as Set<WmsLayerIdentifier>?
        return identifiers ?: emptySet()
    }

    fun getMetadataUrls(): Set<WmsLayerInfoUrl>? {
        val metadataUrls =
            this.getField(metadataUrl) as Set<WmsLayerInfoUrl>?
        return metadataUrls ?: metadataUrls
    }

    fun getFeatureListUrls(): Set<WmsLayerInfoUrl>? {
        val featureUrls =
            this.getField(featureListUrl) as Set<WmsLayerInfoUrl>?
        return featureUrls ?: emptySet()
    }

    fun getDataUrls(): Set<WmsLayerInfoUrl>? {
        val dataUrls =
            this.getField(dataUrl) as Set<WmsLayerInfoUrl>?
        return dataUrls ?: emptySet()
    }

    fun getLayers(): List<WmsLayerCapabilities> {
        val layers =
            this.getField(layer) as List<WmsLayerCapabilities>?
        return layers ?: emptyList()
    }

    fun getStyles(): Set<WmsLayerStyle> {
        val styles: MutableSet<WmsLayerStyle> = mutableSetOf()
        getAdditiveInheritedField(style, styles)
        return styles
    }

    fun getBoundingBoxes(): Set<WmsBoundingBox>? {
        val boundingBoxes =
            this.getInheritedField(boundingBox) as Set<WmsBoundingBox>?
        return boundingBoxes ?: emptySet()
    }

    fun getGeographicBoundingBox(): Sector? {
        var boundingBox: WmsGeographicBoundingBox? =
            this.getInheritedField(geographicBoundingBox) as WmsGeographicBoundingBox?

        if (boundingBox == null) {
            // try the 1.1.1 style
            boundingBox =
                getInheritedField(latLonBoundingBox) as WmsGeographicBoundingBox?
        }

        if (boundingBox != null) {
            val minLon = boundingBox.getWestBound()
            val maxLon = boundingBox.getEastBound()
            val minLat = boundingBox.getSouthBound()
            val maxLat = boundingBox.getNorthBound()
            return if (minLon == null || maxLon == null || minLat == null || maxLat == null) {
                null
            } else Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon)
        }
        return null
    }
    fun getNumberOfLevels(imageWidth: Int): Int {
        var value = getMinScaleDenominator()
        if (value == null) {
            // try the 1.1.1 version
            value = getMinScaleHint()
            if (value != null) {
                value /= WorldWind.WGS84_SEMI_MAJOR_AXIS
            } else {
                // this is a fallback value used when no minimum scale hint or denominator is provided
                return 12
            }
        } else {
            // this conversion is based on the WMS 1.3.0 spec page 28
            // the hard coded value 0.00028 is detailed in the spec
            value = value * imageWidth * 0.00028 / WorldWind.WGS84_SEMI_MAJOR_AXIS
        }
        val levelSetConfig = LevelSetConfig()
        return levelSetConfig.numLevelsForResolution(value)
    }
    fun getKeywords(): Set<String>? {
        val keywords = this.getField(keywordList) as WmsKeywords?
        return if (keywords != null) {
            keywords.getKeywords()
        } else {
            emptySet()
        }
    }

    fun getReferenceSystem(): Set<String>? {
        var rs: Set<String>? = getCRS()
        if (rs!!.isEmpty()) {
            rs = getSRS()
        }
        return rs
    }
    fun getLayerAbstract(): String? {
        return getChildCharacterValue(layerAbstract)
    }

    fun getMaxScaleDenominator(): Double? {
        return (getInheritedField(maxScaleDenominator) as DoubleModel?)?.getValue()
    }

    fun getMinScaleDenominator(): Double? {
        return (this.getInheritedField(minScaleDenominator) as DoubleModel?)?.getValue()

    }

    fun getName(): String? {
        return getChildCharacterValue(name)
    }

    protected fun setName(name: String?) {
        setChildCharacterValue(this.name, name)
    }

    fun getTitle(): String? {
        return getChildCharacterValue(title)
    }

    protected fun setTitle(title: String?) {
        setChildCharacterValue(this.title, title)
    }

    fun getSRS(): Set<String>? {
        val srs: MutableSet<String> = mutableSetOf()
        getAdditiveInheritedField(this.srs, srs)
        return srs
    }

    fun getCRS(): Set<String>? {
        val crs: MutableSet<String> = mutableSetOf()
        getAdditiveInheritedField(this.crs, crs)
        return crs
    }

    fun hasCoordinateSystem(coordSys: String?): Boolean {
        if (coordSys == null) return false
        val crs = getCRS()
        if (crs != null && crs.contains(coordSys)) {
            return true
        }
        val srs = getSRS()
        return srs != null && srs.contains(coordSys)
    }

    override fun setField(keyName: QName, value: Any?) {
        if (keyName == crs) {
            var crss =
                this.getField(crs) as MutableSet<String>?
            if (crss == null) {
                crss = HashSet()
                super.setField(crs, crss)
            }
            (value as XmlModel?)?.getCharactersContent()?.let { it1 -> crss.add(it1) }
        } else if (keyName == srs) {
            var srss =
                this.getField(srs) as MutableSet<String>?
            if (srss == null) {
                srss = HashSet()
                super.setField(srs, srss)
            }
            (value as XmlModel?)?.getCharactersContent()?.let { srss.add(it) }
        } else if (keyName == boundingBox) {
            var boundingBoxes =
                this.getField(boundingBox) as MutableSet<WmsBoundingBox>?
            if (boundingBoxes == null) {
                boundingBoxes = HashSet()
                super.setField(boundingBox, boundingBoxes)
            }
            if (value is WmsBoundingBox) {
                boundingBoxes.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == layer) {
            var layers =
                this.getField(layer) as MutableList<WmsLayerCapabilities>?
            if (layers == null) {
                layers = ArrayList()
                super.setField(layer, layers)
            }
            if (value is WmsLayerCapabilities) {
                layers.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == dataUrl) {
            var dataUrls =
                this.getField(dataUrl) as MutableSet<WmsLayerInfoUrl>?
            if (dataUrls == null) {
                dataUrls = HashSet()
                super.setField(dataUrl, dataUrls)
            }
            if (value is WmsLayerInfoUrl) {
                dataUrls.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == featureListUrl) {
            var featureUrls =
                this.getField(featureListUrl) as MutableSet<WmsLayerInfoUrl>?
            if (featureUrls == null) {
                featureUrls = HashSet()
                super.setField(dataUrl, featureUrls)
            }
            if (value is WmsLayerInfoUrl) {
                featureUrls.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == metadataUrl) {
            var metadataUrls =
                this.getField(metadataUrl) as MutableSet<WmsLayerInfoUrl>?
            if (metadataUrls == null) {
                metadataUrls = HashSet()
                super.setField(metadataUrl, metadataUrls)
            }
            if (value is WmsLayerInfoUrl) {
                metadataUrls.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == identifier) {
            var layerIdentifiers =
                this.getField(identifier) as MutableSet<WmsLayerIdentifier>?
            if (layerIdentifiers == null) {
                layerIdentifiers = HashSet()
                super.setField(identifier, layerIdentifiers)
            }
            if (value is WmsLayerIdentifier) {
                layerIdentifiers.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == authorityUrl) {
            var authorityUrls =
                this.getField(authorityUrl) as MutableSet<WmsAuthorityUrl>?
            if (authorityUrls == null) {
                authorityUrls = HashSet()
                super.setField(authorityUrl, authorityUrls)
            }
            if (value is WmsLayerIdentifier) {
                authorityUrls.add(value as WmsAuthorityUrl)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == attribution) {
            var attributions =
                this.getField(attribution) as MutableSet<WmsLayerAttribution>?
            if (attributions == null) {
                attributions = HashSet()
                super.setField(attribution, attributions)
            }
            if (value is WmsLayerAttribution) {
                attributions.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == extent) {
            var extents =
                this.getField(extent) as MutableSet<WmsLayerExtent>?
            if (extents == null) {
                extents = HashSet()
                super.setField(extent, extents)
            }
            if (value is WmsLayerExtent) {
                extents.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == dimension) {
            var dimensions =
                this.getField(dimension) as MutableSet<WmsLayerDimension>?
            if (dimensions == null) {
                dimensions = HashSet()
                super.setField(dimension, dimensions)
            }
            if (value is WmsLayerDimension) {
                dimensions.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == style) {
            var styles =
                this.getField(style) as MutableSet<WmsLayerStyle>?
            if (styles == null) {
                styles = HashSet()
                super.setField(style, styles)
            }
            if (value is WmsLayerStyle) {
                styles.add(value)
            } else {
                super.setField(keyName, value)
            }
        } else {
            super.setField(keyName, value)
        }
    }

    override fun toString(): String // TODO: Complete this method
    {
        val sb = StringBuilder("LAYER ")
        if (getName() != null) {
            sb.append(getName()).append(": ")
        }
        sb.append("queryable = ").append(isQueryable())
        return sb.toString()
    }
}