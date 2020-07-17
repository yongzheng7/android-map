package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModelParser

open class WmtsXmlParser : XmlModelParser() {
    protected var owsNamespace = "http://www.opengis.net/ows/1.1"

    protected var wmtsNamespace = "http://www.opengis.net/wmts/1.0"

    init {
        registerParsers()
    }

    protected fun registerParsers() {
        this.registerWmtsXmlModels()
        this.registerWmtsTextModels()
    }
    protected open fun registerWmtsXmlModels() {
        registerXmlModel(owsNamespace, "Abstract", OwsLanguageString::class.java)
        registerXmlModel(owsNamespace, "Address", OwsAddress::class.java)
        registerXmlModel(owsNamespace, "AllowedValues", OwsAllowedValues::class.java)
        registerXmlModel(owsNamespace, "BoundingBox", OwsBoundingBox::class.java)
        registerXmlModel(wmtsNamespace, "Capabilities", WmtsCapabilities::class.java)
        registerXmlModel(owsNamespace, "Constraint", OwsConstraint::class.java)
        registerXmlModel(owsNamespace, "ContactInfo", OwsContactInfo::class.java)
        registerXmlModel(wmtsNamespace, "Contents", WmtsContents::class.java)
        registerXmlModel(owsNamespace, "DCP", OwsDcp::class.java)
        registerXmlModel(wmtsNamespace, "Dimension", WmtsDimension::class.java)
        registerXmlModel(owsNamespace, "Get", OwsHttpMethod::class.java)
        registerXmlModel(owsNamespace, "HTTP", OwsHttp::class.java)
        registerXmlModel(owsNamespace, "Keyword", OwsLanguageString::class.java)
        registerXmlModel(owsNamespace, "Keywords", OwsKeywords::class.java)
        registerXmlModel(wmtsNamespace, "Layer", WmtsLayer::class.java)
        registerXmlModel(wmtsNamespace, "LegendURL", WmtsElementLink::class.java)
        registerXmlModel(owsNamespace, "Metadata", WmtsElementLink::class.java)
        registerXmlModel(owsNamespace, "Operation", OwsOperation::class.java)
        registerXmlModel(owsNamespace, "OperationsMetadata", OwsOperationsMetadata::class.java)
        registerXmlModel(owsNamespace, "Phone", OwsPhone::class.java)
        registerXmlModel(owsNamespace, "Post", OwsHttpMethod::class.java)
        registerXmlModel(owsNamespace, "ProviderSite", WmtsElementLink::class.java)
        registerXmlModel(wmtsNamespace, "ResourceURL", WmtsResourceUrl::class.java)
        registerXmlModel(owsNamespace, "ServiceContact", OwsServiceContact::class.java)
        registerXmlModel(
            owsNamespace,
            "ServiceIdentification",
            OwsServiceIdentification::class.java
        )
        registerXmlModel(wmtsNamespace, "ServiceMetadataURL", WmtsElementLink::class.java)
        registerXmlModel(owsNamespace, "ServiceProvider", OwsServiceProvider::class.java)
        registerXmlModel(wmtsNamespace, "Style", WmtsStyle::class.java)
        registerXmlModel(wmtsNamespace, "Theme", WmtsTheme::class.java)
        registerXmlModel(wmtsNamespace, "Themes", WmtsThemes::class.java)
        registerXmlModel(wmtsNamespace, "TileMatrix", WmtsTileMatrix::class.java)
        registerXmlModel(wmtsNamespace, "TileMatrixLimits", WmtsTileMatrixLimits::class.java)
        registerXmlModel(wmtsNamespace, "TileMatrixSet", WmtsTileMatrixSet::class.java)
        registerXmlModel(
            wmtsNamespace,
            "TileMatrixSetLimits",
            WmtsTileMatrixSetLimits::class.java
        )
        registerXmlModel(wmtsNamespace, "TileMatrixSetLink", WmtsTileMatrixSetLink::class.java)
        registerXmlModel(owsNamespace, "Title", OwsLanguageString::class.java)
        registerXmlModel(owsNamespace, "WGS84BoundingBox", OwsWgs84BoundingBox::class.java)
    }

    protected open fun registerWmtsTextModels() {
        registerTxtModel(owsNamespace, "AccessConstraints")
        registerTxtModel(owsNamespace, "AdministrativeArea")
        registerTxtModel(owsNamespace, "City")
        registerTxtModel(owsNamespace, "Country")
        registerTxtModel(wmtsNamespace, "Current")
        registerTxtModel(wmtsNamespace, "Default")
        registerTxtModel(owsNamespace, "DeliveryPoint")
        registerTxtModel(owsNamespace, "ElectronicMailAddress")
        registerTxtModel(owsNamespace, "Facsimile")
        registerTxtModel(owsNamespace, "Fees")
        registerTxtModel(wmtsNamespace, "Format")
        registerTxtModel(owsNamespace, "Identifier")
        registerTxtModel(owsNamespace, "IndividualName")
        registerTxtModel(wmtsNamespace, "InfoFormat")
        registerTxtModel(wmtsNamespace, "LayerRef")
        registerTxtModel(owsNamespace, "LowerCorner")
        registerTxtModel(wmtsNamespace, "MatrixHeight")
        registerTxtModel(wmtsNamespace, "MatrixWidth")
        registerTxtModel(wmtsNamespace, "MaxTileCol")
        registerTxtModel(wmtsNamespace, "MaxTileRow")
        registerTxtModel(wmtsNamespace, "MinTileCol")
        registerTxtModel(wmtsNamespace, "MinTileRow")
        registerTxtModel(owsNamespace, "PositionName")
        registerTxtModel(owsNamespace, "PostalCode")
        registerTxtModel(wmtsNamespace, "Profile")
        registerTxtModel(owsNamespace, "ProviderName")
        registerTxtModel(wmtsNamespace, "ScaleDenominator")
        registerTxtModel(owsNamespace, "ServiceType")
        registerTxtModel(owsNamespace, "ServiceTypeVersion")
        registerTxtModel(owsNamespace, "SupportedCRS")
        registerTxtModel(wmtsNamespace, "TileHeight")
        registerTxtModel(wmtsNamespace, "TileWidth")
        registerTxtModel(wmtsNamespace, "TopLeftCorner")
        registerTxtModel(wmtsNamespace, "UnitSymbol")
        registerTxtModel(owsNamespace, "UOM")
        registerTxtModel(wmtsNamespace, "UOM")
        registerTxtModel(owsNamespace, "UpperCorner")
        registerTxtModel(owsNamespace, "Value")
        registerTxtModel(wmtsNamespace, "Value")
        registerTxtModel(owsNamespace, "Voice")
        registerTxtModel(wmtsNamespace, "WellKnownScaleSet")
    }
}