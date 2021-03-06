package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModelParser

class WmsXmlParser : XmlModelParser() {
    init {
        this.registerNamespace("") // WMS 1.1.1 namespace
        this.registerNamespace("http://www.opengis.net/wms") // WMS 1.3.0 namespace
    }

    protected fun registerNamespace(namespace: String?) {
        registerTxtModel(namespace, "Abstract")
        registerTxtModel(namespace, "AccessConstraints")
        registerTxtModel(namespace, "Address")
        registerTxtModel(namespace, "AddressType")
        registerXmlModel(namespace, "Attribution", WmsAttribution::class.java)
        registerXmlModel(namespace, "AuthorityURL", WmsAuthorityUrl::class.java)
        registerXmlModel(namespace, "BoundingBox", WmsBoundingBox::class.java)
        registerXmlModel(namespace, "Capability", WmsCapability::class.java)
        registerTxtModel(namespace, "City")
        registerXmlModel(namespace, "ContactAddress", WmsAddress::class.java)
        registerTxtModel(namespace, "ContactElectronicMailAddress")
        registerXmlModel(namespace, "ContactInformation", WmsContactInformation::class.java)
        registerTxtModel(namespace, "ContactOrganization")
        registerTxtModel(namespace, "ContactPerson")
        registerXmlModel(namespace, "ContactPersonPrimary", WmsContactPersonPrimary::class.java)
        registerTxtModel(namespace, "ContactPosition")
        registerTxtModel(namespace, "ContactVoiceTelephone")
        registerTxtModel(namespace, "Country")
        registerTxtModel(namespace, "CRS")
        registerXmlModel(namespace, "DataURL", WmsInfoUrl::class.java)
        registerXmlModel(namespace, "DCPType", WmsDcpType::class.java)
        registerXmlModel(namespace, "Dimension", WmsDimension::class.java)
        registerXmlModel(namespace, "Extent", WmsDimension::class.java)
        registerXmlModel(namespace, "EX_GeographicBoundingBox", WmsGeographicBoundingBox::class.java)
        registerTxtModel(namespace, "westBoundLongitude")
        registerTxtModel(namespace, "eastBoundLongitude")
        registerTxtModel(namespace, "northBoundLatitude")
        registerTxtModel(namespace, "southBoundLatitude")
        registerXmlModel(namespace, "Exception", WmsException::class.java)
        registerXmlModel(namespace, "FeatureListURL", WmsInfoUrl::class.java)
        registerTxtModel(namespace, "Fees")
        registerTxtModel(namespace, "Format")
        registerXmlModel(namespace, "Get", WmsDcpType.WmsDcpHttpProtocol::class.java)
        registerXmlModel(namespace, "GetCapabilities", WmsRequestOperation::class.java)
        registerXmlModel(namespace, "GetMap", WmsRequestOperation::class.java)
        registerXmlModel(namespace, "GetFeatureInfo", WmsRequestOperation::class.java)
        registerXmlModel(namespace, "HTTP", WmsDcpType.WmsDcpHttp::class.java)
        registerXmlModel(namespace, "Identifier", WmsIdentifier::class.java)
        registerTxtModel(namespace, "Keyword")
        registerXmlModel(namespace, "KeywordList", WmsKeywords::class.java)
        registerXmlModel(namespace, "LatLonBoundingBox", WmsGeographicBoundingBox::class.java)
        registerXmlModel(namespace, "Layer", WmsLayer::class.java)
        registerXmlModel(namespace, "LayerInfo", WmsInfoUrl::class.java)
        registerTxtModel(namespace, "LayerLimit")
        registerXmlModel(namespace, "LegendURL", WmsLogoUrl::class.java)
        registerXmlModel(namespace, "LogoURL", WmsLogoUrl::class.java)
        registerTxtModel(namespace, "MaxHeight")
        registerTxtModel(namespace, "MaxScaleDenominator")
        registerTxtModel(namespace, "MaxWidth")
        registerXmlModel(namespace, "MetadataURL", WmsInfoUrl::class.java)
        registerTxtModel(namespace, "MinScaleDenominator")
        registerTxtModel(namespace, "Name")
        registerXmlModel(namespace, "OnlineResource", WmsOnlineResource::class.java)
        registerXmlModel(namespace, "Post", WmsDcpType.WmsDcpHttpProtocol::class.java)
        registerTxtModel(namespace, "PostCode")
        registerXmlModel(namespace, "Request", WmsRequest::class.java)
        registerXmlModel(namespace, "ScaleHint", WmsScaleHint::class.java)
        registerXmlModel(namespace, "Service", WmsService::class.java)
        registerTxtModel(namespace, "SRS")
        registerTxtModel(namespace, "StateOrProvince")
        registerXmlModel(namespace, "Style", WmsStyle::class.java)
        registerXmlModel(namespace, "StyleSheetURL", WmsInfoUrl::class.java)
        registerXmlModel(namespace, "StyleURL", WmsInfoUrl::class.java)
        registerTxtModel(namespace, "Title")
        registerXmlModel(namespace, "WMS_Capabilities", WmsCapabilities::class.java)
        registerXmlModel(namespace, "WMT_MS_Capabilities", WmsCapabilities::class.java)
    }
}