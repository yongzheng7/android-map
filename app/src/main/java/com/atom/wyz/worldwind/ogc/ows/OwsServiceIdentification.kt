package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsServiceIdentification : XmlModel() {

    protected var serviceType: String? = null

    protected var serviceTypeVersions: MutableList<String> =
        ArrayList()

    protected var profiles: MutableList<String> =
        ArrayList()

    protected var fees: String? = null

    protected var accessConstraints: MutableList<String> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "ServiceType" -> {
                serviceType = value as String
            }
            "ServiceTypeVersion" -> {
                serviceTypeVersions.add((value as String))
            }
            "Fees" -> {
                fees = value as String
            }
            "AccessConstraints" -> {
                accessConstraints.add((value as String))
            }
            "Profile" -> {
                profiles.add((value as String))
            }
        }
    }
}