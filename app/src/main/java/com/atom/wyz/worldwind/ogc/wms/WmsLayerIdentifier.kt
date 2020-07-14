package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsLayerIdentifier : XmlModel {

    lateinit var authority: QName

    constructor(namespaceUri: String?) : super(namespaceUri) {
        initialize()
    }

    protected fun initialize() {
        authority = QName("", "authority")
    }

    fun getIdentifier(): String? {
        return getCharactersContent()
    }

    fun getAuthority(): String? {
        return this.getField(authority).toString()
    }
}