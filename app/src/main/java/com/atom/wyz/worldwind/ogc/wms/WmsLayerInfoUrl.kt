package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

open class WmsLayerInfoUrl(namespaceUri: String?) : XmlModel(namespaceUri) {

    lateinit var format: QName
    lateinit var onlineResource: QName

    init {
        initialize()
    }

    private fun initialize() {
        format = QName(this.namespaceUri, "Format")
        onlineResource = QName(this.namespaceUri, "OnlineResource")
    }

    open fun getOnlineResource(): WmsOnlineResource? {
        val o = this.getField(onlineResource)
        return if (o is WmsOnlineResource) {
            o
        } else {
            null
        }
    }

    open fun getFormat(): String? {
        val o = this.getField(format)
        return if (o is XmlModel) {
            o.getCharactersContent()
        } else {
            null
        }
    }

}