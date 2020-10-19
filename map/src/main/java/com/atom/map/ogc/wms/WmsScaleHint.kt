package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsScaleHint : XmlModel() {

    open var min: Double? = null

    open var max: Double? = null

    override fun parseField(keyName: String, value: Any) {
        if (keyName == "min") {
            min = (value as String).toDouble()
        } else if (keyName == "max") {
            max = (value as String).toDouble()
        }
    }
}