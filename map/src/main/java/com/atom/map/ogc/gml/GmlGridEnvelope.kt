package com.atom.map.ogc.gml

import com.atom.map.util.xml.XmlModel

open class GmlGridEnvelope : XmlModel() {

    open var low: GmlIntegerList = GmlIntegerList()

    open var high: GmlIntegerList = GmlIntegerList()

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "low" -> low = value as GmlIntegerList
            "high" -> high = value as GmlIntegerList
        }
    }
}