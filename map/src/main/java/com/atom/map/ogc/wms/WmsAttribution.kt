package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsAttribution : XmlModel() {
    open var title: String? = null

    open var url: String? = null

    open var logoUrl: WmsLogoUrl? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Title" -> {
                title = value as String
            }
            "OnlineResource" -> {
                url = (value as WmsOnlineResource).url
            }
            "LogoURL" -> {
                logoUrl = value as WmsLogoUrl
            }
        }
    }
}