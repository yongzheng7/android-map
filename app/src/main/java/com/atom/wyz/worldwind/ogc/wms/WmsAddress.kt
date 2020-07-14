package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

class WmsAddress : XmlModel {

    lateinit var addressType: QName

    lateinit  var address: QName

    lateinit  var city: QName

    lateinit  var stateOrProvince: QName

    lateinit  var postCode: QName

    lateinit  var country: QName

    constructor(namespaceURI: String?):super(namespaceURI) {
        this.initialize()
    }

    protected fun initialize() {
        addressType = QName(this.namespaceUri, "AddressType")
        address = QName(this.namespaceUri, "Address")
        city = QName(this.namespaceUri, "City")
        stateOrProvince = QName(this.namespaceUri, "StateOrProvince")
        postCode = QName(this.namespaceUri, "PostCode")
        country = QName(this.namespaceUri, "Country")
    }


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("AddressType: ")
            .append(if (getAddressType() != null) getAddressType() else "none")
            .append(" ")
        sb.append("Address: ").append(if (getAddress() != null) getAddress() else "none")
            .append(" ")
        sb.append("City: ").append(if (getCity() != null) getCity() else "none")
            .append(" ")
        sb.append("StateOrProvince: ")
            .append(if (getStateOrProvince() != null) getStateOrProvince() else "none")
            .append(" ")
        sb.append("PostCode: ")
            .append(if (getPostCode() != null) getPostCode() else "none").append(" ")
        sb.append("Country: ").append(if (getCountry() != null) getCountry() else "none")
        return sb.toString()
    }

    fun getAddressType(): String? {
        return getChildCharacterValue(addressType)
    }

    protected fun setAddressType(addressType: String?) {
        setChildCharacterValue(this.addressType, addressType)
    }

    fun getAddress(): String? {
        return getChildCharacterValue(address)
    }

    protected fun setAddress(address: String?) {
        setChildCharacterValue(this.address, address)
    }

    fun getCity(): String? {
        return getChildCharacterValue(city)
    }

    protected fun setCity(city: String?) {
        setChildCharacterValue(this.city, city)
    }

    fun getStateOrProvince(): String? {
        return getChildCharacterValue(stateOrProvince)
    }

    protected fun setStateOrProvince(stateOrProvince: String?) {
        setChildCharacterValue(this.stateOrProvince, stateOrProvince)
    }

    fun getPostCode(): String? {
        return getChildCharacterValue(postCode)
    }

    protected fun setPostCode(postCode: String?) {
        setChildCharacterValue(this.postCode, postCode)
    }

    fun getCountry(): String? {
        return getChildCharacterValue(country)
    }

    protected fun setCountry(country: String?) {
        setChildCharacterValue(this.country, country)
    }
}