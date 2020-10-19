package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel

open class OwsLanguageString : XmlModel() {

    open var lang: String? = null

    open var value: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "lang" -> {
                lang = value as String
            }
        }
    }

    override fun parseText(text: String) {
        value = text
    }
}