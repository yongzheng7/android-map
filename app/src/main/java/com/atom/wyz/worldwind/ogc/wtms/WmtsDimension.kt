package com.atom.wyz.worldwind.ogc.wtms

import java.util.*

open class WmtsDimension: OwsDescription() {
    open var identifier: String? = null

    open var unitOfMeasure: String? = null

    open var unitSymbol: String? = null

    open var valueDefault: String? = null

    open var current: Boolean? = null

    open var values: MutableList<String> =
        ArrayList()

    override  fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Identifier" -> {
                identifier = value as String
            }
            "UOM" -> {
                unitOfMeasure = value as String
            }
            "UnitSymbol" -> {
                unitSymbol = value as String
            }
            "Default" -> {
                valueDefault = value as String
            }
            "Current" -> {
                current = java.lang.Boolean.parseBoolean(value as String)
            }
            "Value" -> {
                values.add((value as String))
            }
        }
    }
}