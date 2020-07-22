package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel

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