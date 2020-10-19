package com.atom.map.ogc.ows

import com.atom.map.ogc.wtms.WmtsElementLink
import com.atom.map.util.xml.XmlModel

open class OwsServiceProvider : XmlModel() {
    open var providerName: String? = null

    open var providerSiteUrl: String? = null

    open var serviceContact: OwsServiceContact? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "ProviderName" -> {
                providerName = value as String
            }
            "ProviderSite" -> {
                providerSiteUrl = (value as WmtsElementLink).url
            }
            "ServiceContact" -> {
                serviceContact = value as OwsServiceContact
            }
        }
    }
}