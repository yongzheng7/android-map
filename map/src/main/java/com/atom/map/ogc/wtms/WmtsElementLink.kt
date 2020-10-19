package com.atom.map.ogc.wtms

import com.atom.map.util.xml.XmlModel

open class WmtsElementLink : XmlModel() {
    open var url: String? = null

    open var format: String? = null
    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "href" -> {
                url = value as String
            }
            "format" -> {
                format = value as String
            }
        }
    }
}