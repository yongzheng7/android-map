package com.atom.map.ogc.ows

import com.atom.map.ogc.ows.OwsDcp
import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsOperation : XmlModel() {
    open var name: String? = null

    open var dcps: MutableList<OwsDcp> = ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "name" -> {
                name = value as String
            }
            "DCP" -> {
                dcps.add((value as OwsDcp))
            }
        }
    }
}