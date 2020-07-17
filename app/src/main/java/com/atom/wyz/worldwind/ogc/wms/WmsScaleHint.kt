package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel

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