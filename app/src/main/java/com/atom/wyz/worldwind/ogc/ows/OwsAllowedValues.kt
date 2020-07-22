package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel
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