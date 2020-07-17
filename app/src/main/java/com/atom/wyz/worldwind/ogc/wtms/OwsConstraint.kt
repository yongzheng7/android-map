package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsConstraint : XmlModel() {
    protected var name: String? = null

    protected var allowedValues: MutableList<String> = ArrayList()
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