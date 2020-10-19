package com.atom.map.ogc.ows

import com.atom.map.ogc.ows.OwsAllowedValues
import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsConstraint : XmlModel() {
    open var name: String? = null

    open var allowedValues: MutableList<String> = ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "name" -> {
                name = value as String
            }
            "AllowedValues" -> {
                allowedValues.addAll((value as OwsAllowedValues).allowedValues)
            }
            "AnyValue" -> {
                allowedValues.add("AnyValue")
            }
        }
    }
}