package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmsException : XmlModel() {
    open var formats: MutableList<String> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Format" -> {
                formats.add(value as String)
            }
        }
    }

}