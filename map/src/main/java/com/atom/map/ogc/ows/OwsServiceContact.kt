package com.atom.map.ogc.ows

import com.atom.map.ogc.ows.OwsContactInfo
import com.atom.map.util.xml.XmlModel

open class OwsServiceContact : XmlModel() {

    open var individualName: String? = null

    open var positionName: String? = null

    open var contactInfo: OwsContactInfo? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "IndividualName" -> {
                individualName = value as String
            }
            "PositionName" -> {
                positionName = value as String
            }
            "ContactInfo" -> {
                contactInfo = value as OwsContactInfo
            }
        }
    }
}