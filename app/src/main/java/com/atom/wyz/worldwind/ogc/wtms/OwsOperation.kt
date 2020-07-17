package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
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