package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.*
import javax.xml.namespace.QName

class WmsPullParserContext(namespaceUri: String?) : XmlPullParserContext(namespaceUri) {

    override fun initializeParsers() {
        super.initializeParsers()

        // Wms Element Registration
        registerParsableModel(
            QName(namespaceUri, "Address"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "AddressType"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Attribution"),
            WmsLayerAttribution(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "AuthorityUrl"),
            WmsAuthorityUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "BoundingBox"),
            WmsBoundingBox(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Capability"),
            WmsCapabilityInformation(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "City"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "ContactAddress"),
            WmsAddress(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "ContactElectronicMailAddress"
            ), XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "ContactInformation"
            ), WmsContactInformation(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "ContactOrganization"
            ), XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "ContactPerson"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "ContactPersonPrimary"
            ), XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "ContactPosition"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "ContactVoiceTelephone"
            ), XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Country"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "DataURL"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "DCPType"),
            WmsDcpType(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Dimension"),
            WmsLayerDimension(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Extent"),
            WmsLayerExtent(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "EX_GeographicBoundingBox"
            ),
            WmsGeographicBoundingBox(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "westBoundLongitude"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "eastBoundLongitude"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "northBoundLatitude"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "southBoundLatitude"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "FeatureListURL"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Format"),
            WmsFormat(namespaceUri)
        )

        registerParsableModel(
            QName(namespaceUri, "Get"),
            NameStringModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "GetCapabilities"),
            WmsRequestDescription(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "GetMap"),
            WmsRequestDescription(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "GetFeatureInfo"),
            WmsRequestDescription(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "HTTP"),
            NameStringModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Identifier"),
            WmsLayerIdentifier(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "KeywordList"),
            WmsKeywords(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "LatLonBoundingBox"
            ),
            WmsGeographicBoundingBox(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Layer"),
            WmsLayerCapabilities(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "LayerInfo"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "LayerLimit"),
            IntegerModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "LegendURL"),
            WmsLogoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "LogoURL"),
            WmsLogoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "MaxHeight"),
            IntegerModel(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "MaxScaleDenominator"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "MaxWidth"),
            IntegerModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "MetadataURL"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "MinScaleDenominator"
            ), DoubleModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "OnlineResource"),
            WmsOnlineResource(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Post"),
            NameStringModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "PostCode"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Service"),
            WmsServiceInformation(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "StateOrProvince"),
            XmlModel(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "Style"),
            WmsLayerStyle(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "StyleSheetURL"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "StyleURL"),
            WmsLayerInfoUrl(namespaceUri)
        )
        registerParsableModel(
            QName(namespaceUri, "WMS_Capabilities"),
            WmsCapabilities(namespaceUri)
        )
        registerParsableModel(
            QName(
                namespaceUri,
                "WMT_MS_Capabilities"
            ), WmsCapabilities(namespaceUri)
        )
    }

}