package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel

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