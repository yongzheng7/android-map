package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmsContactInformation() : XmlModel() {

    open var contactPosition: String? = null

    open var contactVoiceTelephone: String? = null

    open var contactFacsimileTelephone: String? = null

    open var contactElectronicMailAddress: String? = null

    open var contactAddress: WmsAddress? = null

    open var contactPersonPrimary: WmsContactPersonPrimary? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "ContactPosition" -> {
                contactPosition = value as String
            }
            "ContactVoiceTelephone" -> {
                contactVoiceTelephone = value as String
            }
            "ContactFacsimileNumber" -> {
                contactFacsimileTelephone = value as String
            }
            "ContactElectronicMailAddress" -> {
                contactElectronicMailAddress = value as String
            }
            "ContactPersonPrimary" -> {
                contactPersonPrimary = value as WmsContactPersonPrimary
            }
            "ContactAddress" -> {
                contactAddress = value as WmsAddress
            }
        }
    }

}