package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsAllowedValues : XmlModel() {

    open var allowedValues: MutableList<String> = ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Value" -> {
                allowedValues.add((value as String))
            }
        }
    }
}