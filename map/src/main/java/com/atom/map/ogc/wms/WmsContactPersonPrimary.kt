package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsContactPersonPrimary : XmlModel(){

    open var contactPerson: String? = null

    open var contactOrganization: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "ContactPerson" -> {
                contactPerson = value as String
            }
            "ContactOrganization" -> {
                contactOrganization = value as String
            }
        }
    }
}