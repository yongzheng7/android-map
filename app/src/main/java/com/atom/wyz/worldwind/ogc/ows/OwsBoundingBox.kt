package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel

open class OwsBoundingBox : XmlModel() {

    open var crs: String? = null

    open var lowerCorner: String? = null

    open var upperCorner: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "crs" -> {
                crs = value as String
            }
            "LowerCorner" -> {
                lowerCorner = value as String
            }
            "UpperCorner" -> {
                upperCorner = value as String
            }
        }
    }
}