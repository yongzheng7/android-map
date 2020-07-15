package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsLayerIdentifier(namespaceUri: String?) : XmlModel(namespaceUri) {

    lateinit var authority: QName

    init {
        initialize()
    }

    protected fun initialize() {
        authority = QName("", "authority")
    }

    fun getIdentifier(): String? {
        return getCharactersContent()
    }

    fun getAuthority(): String? {
        val o = this.getField(authority)
        return if (o is String) {
            o
        } else {
            null
        }
    }
}