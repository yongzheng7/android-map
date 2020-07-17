package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsAddress : XmlModel() {

    open var deliveryPoint: MutableList<String> = ArrayList()

    open var city: String? = null

    open var administrativeArea: String? = null

    open var postalCode: MutableList<String> = ArrayList()

    open var country: MutableList<String> = ArrayList()

    open var email: MutableList<String> = ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "DeliveryPoint" -> {
                deliveryPoint.add(value as String)
            }
            "City" -> {
                city = value as String
            }
            "AdministrativeArea" -> {
                administrativeArea = value as String
            }
            "PostalCode" -> {
                postalCode.add(value as String)
            }
            "Country" -> {
                country.add(value as String)
            }
            "ElectronicMailAddress" -> {
                email.add(value as String)
            }
        }
    }
}