package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.ogc.ows.OwsException
import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsExceptionReport : XmlModel() {
    open var exceptions: MutableList<OwsException> = ArrayList()

    open var version: String? = null

    open var lang: String? = null

    override fun toString(): String {
        return "OwsExceptionReport{" +
                "exceptions=" + exceptions +
                ", version='" + version + '\'' +
                ", lang='" + lang + '\'' +
                '}'
    }

    open fun toPrettyString(): String? {
        return if (exceptions.size == 0) {
            null
        } else if (exceptions.size == 1) {
            exceptions[0].toPrettyString()
        } else {
            val sb = StringBuilder()
            var ordinal = 1
            for (exception in exceptions) {
                if (sb.length > 0) {
                    sb.append("\n")
                }
                sb.append(ordinal++).append(": ")
                sb.append(exception.toPrettyString())
            }
            sb.toString()
        }
    }

    override open fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Exception" -> exceptions.add((value as OwsException))
            "version" -> version = value as String
            "lang" -> lang = value as String
        }
    }
}