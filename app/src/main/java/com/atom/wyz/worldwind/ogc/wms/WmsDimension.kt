package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmsDimension : XmlModel() {

    open var name: String? = null

    open var units: String? = null

    open var unitSymbol: String? = null

    open var defaultValue: String? = null

    open var multipleValues: Boolean? = null

    open var nearestValue: Boolean? = null

    open var current: Boolean? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "name" -> {
                name = value as String
            }
            "units" -> {
                units = value as String
            }
            "unitSymbol" -> {
                unitSymbol = value as String
            }
            "default" -> {
                defaultValue = value as String
            }
            "multipleValues" -> {
                multipleValues = (value as String).toBoolean()
            }
            "nearestValue" -> {
                nearestValue = (value as String).toBoolean()
            }
            "current" -> {
                current = (value as String).toBoolean()
            }
        }
    }
}