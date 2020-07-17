package com.atom.wyz.worldwind.ogc.wtms

import java.util.*

open class WmtsStyle : OwsDescription() {

    protected var identifier: String? = null

    protected var isDefault = false

    protected var legendUrls: MutableList<WmtsElementLink> =
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