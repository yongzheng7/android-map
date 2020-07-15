package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsLayerAttribution(namespaceURI: String?) : XmlModel(namespaceURI) {

    lateinit var title: QName

    lateinit var onlineResource: QName

    lateinit var logoUrl: QName

    init {
        this.initialize()
    }

    private fun initialize() {
        title = QName(this.namespaceUri, "Title")
        onlineResource = QName(this.namespaceUri, "OnlineResource")
        logoUrl = QName(this.namespaceUri, "LogoURL")
    }

    fun getTitle(): String? {
        return getChildCharacterValue(title)
    }

    fun getOnlineResource(): WmsOnlineResource? {
        val o = this.getField(onlineResource)
        return if (o is WmsOnlineResource) {
            o
        } else {
            null
        }
    }

    fun getLogoURL(): WmsLogoUrl? {
        val o = this.getField(logoUrl)
        return if (o is WmsLogoUrl) {
            o
        } else {
            null
        }
    }
}