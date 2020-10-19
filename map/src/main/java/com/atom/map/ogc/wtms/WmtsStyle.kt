package com.atom.map.ogc.wtms

import com.atom.map.ogc.ows.OwsDescription
import java.util.*

open class WmtsStyle : OwsDescription() {

    open var identifier: String? = null

    open var isDefault = false

    open var legendUrls: MutableList<WmtsElementLink> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Identifier" -> {
                identifier = value as String
            }
            "isDefault" -> {
                isDefault = (value as String).toBoolean()
            }
            "LegendURL" -> {
                legendUrls.add((value as WmtsElementLink))
            }
        }
    }
}