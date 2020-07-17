package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmsAddress() : XmlModel() {

    open var addressType: String? = null

    open var address: String? = null

    open var city: String? = null

    open var stateOrProvince: String? = null

    open var postCode: String? = null

    open var country: String? = null


    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Address" -> {
                address = value as String
            }
            "AddressType" -> {
                addressType = value as String
            }
            "City" -> {
                city = value as String
            }
            "StateOrProvince" -> {
                stateOrProvince = value as String
            }
            "PostCode" -> {
                postCode = value as String
            }
            "Country" -> {
                country = value as String
            }
        }
    }

}