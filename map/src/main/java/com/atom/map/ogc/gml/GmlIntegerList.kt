package com.atom.map.ogc.gml

import com.atom.map.util.Logger
import com.atom.map.util.xml.XmlModel

open class GmlIntegerList : XmlModel() {

    open var values = IntArray(0)

    override fun parseText(text: String): Unit {
        val tokens = text.split(" ".toRegex()).toTypedArray()
        values = IntArray(tokens.size)
        var idx = 0
        val len = tokens.size
        while (idx < len) {
            try {
                values[idx] = tokens[idx].toInt()
            } catch (e: NumberFormatException) {
                Logger.logMessage(
                    Logger.ERROR,
                    "GmlIntegerList",
                    "parseText",
                    "exceptionParsingText",
                    e
                )
            }
            idx++
        }
    }
}