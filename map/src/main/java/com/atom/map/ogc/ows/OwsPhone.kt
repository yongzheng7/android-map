package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel

open class OwsPhone : XmlModel() {
    open var voice: String? = null

    open var fax: String? = null
    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Voice" -> {
                voice = value as String
            }
            "Facsimile" -> {
                fax = value as String
            }
        }
    }
}