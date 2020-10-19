package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsOnlineResource : XmlModel() {
    open var type: String? = null

    open var url: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "type" -> {
                type = value as String
            }
            "href" -> {
                url = value as String
            }
        }
    }
}