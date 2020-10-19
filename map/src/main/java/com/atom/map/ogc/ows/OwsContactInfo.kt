package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel

open class OwsContactInfo : XmlModel() {

    protected var phone: OwsPhone? = null

    protected var address: OwsAddress? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Phone" -> {
                phone = value as OwsPhone
            }
            "Address" -> {
                address = value as OwsAddress
            }
        }
    }
}