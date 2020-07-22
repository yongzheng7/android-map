package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModel

open class GmlDomainSet : XmlModel() {

    open var geometry: GmlAbstractGeometry? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        if (value is GmlAbstractGeometry) { // we know the element type at parse time, but not it's name
            geometry = value
        }
    }
}