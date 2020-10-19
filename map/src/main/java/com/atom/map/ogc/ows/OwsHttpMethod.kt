package com.atom.map.ogc.ows

import com.atom.map.ogc.ows.OwsConstraint
import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsHttpMethod : XmlModel() {

    open var url: String? = null

    open var constraints: MutableList<OwsConstraint> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "href" -> {
                url = value as String
            }
            "Constraint" -> {
                constraints.add((value as OwsConstraint))
            }
        }
    }
}