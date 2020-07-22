package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModel

open class GmlAbstractGml : XmlModel() {

    open var id: String? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "id" -> id = value as String
        }
    }
}