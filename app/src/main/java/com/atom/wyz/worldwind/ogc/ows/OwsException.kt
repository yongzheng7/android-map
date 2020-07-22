package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsException : XmlModel() {

    open var exceptionText: MutableList<String> =
        ArrayList()

    open var exceptionCode: String? = null

    open var locator: String? = null

    override fun toString(): String {
        return "OwsException{" +
                "exceptionText=" + exceptionText +
                ", exceptionCode='" + exceptionCode + '\'' +
                ", locator='" + locator + '\'' +
                '}'
    }

    open fun toPrettyString(): String? {
        return if (exceptionText.size == 0) {
            null
        } else if (exceptionText.size == 1) {
            exceptionText[0]
        } else {
            val sb = StringBuilder()
            for (text in exceptionText) {
                if (sb.length > 0) {
                    sb.append(", ")
                }
                sb.append(text)
            }
            sb.toString()
        }
    }

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "ExceptionText" -> exceptionText.add((value as String))
            "exceptionCode" -> exceptionCode = value as String
            "locator" -> locator = value as String
        }
    }
}