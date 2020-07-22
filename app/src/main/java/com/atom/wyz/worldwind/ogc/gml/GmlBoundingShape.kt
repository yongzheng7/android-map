package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModel

open class GmlBoundingShape : XmlModel() {
    open var envelope: GmlEnvelope? = null

    open var nilReason: String? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Envelope" -> envelope = value as GmlEnvelope
            "nilReason" -> nilReason = value as String
        }
    }
}