package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.xml.XmlModel

open class GmlDoubleList : XmlModel() {

    open var values = DoubleArray(0)
    override fun parseText(text: String) {
        val tokens: Array<String> = text.split(" ".toRegex()).toTypedArray()
        values = DoubleArray(tokens.size)
        var idx = 0
        val len = tokens.size
        while (idx < len) {
            try {
                values[idx] = tokens[idx].toDouble()
            } catch (e: NumberFormatException) {
                Logger.logMessage(
                    Logger.ERROR,
                    "GmlDoubleList",
                    "parseText",
                    "exceptionParsingText",
                    e
                )
            }
            idx++
        }
    }
}