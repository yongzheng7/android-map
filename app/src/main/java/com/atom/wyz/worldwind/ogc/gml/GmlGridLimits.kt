package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModel

open class GmlGridLimits : XmlModel() {

    open var gridEnvelope: GmlGridEnvelope? = null
    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "GridEnvelope" -> gridEnvelope = value as GmlGridEnvelope
        }
    }
}