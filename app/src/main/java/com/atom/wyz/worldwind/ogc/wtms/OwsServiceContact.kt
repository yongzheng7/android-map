package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel

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