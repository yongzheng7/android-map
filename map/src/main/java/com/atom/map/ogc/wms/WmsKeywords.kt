package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmsKeywords() : XmlModel() {

    open var keywords: MutableList<String> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Keyword" -> {
                keywords.add(value as String)
            }
        }
    }
}