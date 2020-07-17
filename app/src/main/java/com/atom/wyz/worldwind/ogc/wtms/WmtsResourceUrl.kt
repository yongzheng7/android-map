package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmtsResourceUrl : XmlModel() {
    open var format: String? = null

    open var resourceType: String? = null

    open var template: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "format" -> {
                format = value as String
            }
            "resourceType" -> {
                resourceType = value as String
            }
            "template" -> {
                template = value as String
            }
        }
    }
}