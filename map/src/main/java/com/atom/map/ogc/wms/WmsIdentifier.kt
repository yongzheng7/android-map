package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsIdentifier : XmlModel() {

    open var authority: String? = null

    open var identifier: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "authority" -> {
                authority = value as String
            }
        }
    }

    override fun parseText(text: String) {
        identifier = text
    }
}