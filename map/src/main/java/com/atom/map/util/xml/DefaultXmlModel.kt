package com.atom.map.util.xml

import java.util.*

open class DefaultXmlModel() : XmlModel() {

    open var fields: MutableMap<String, Any> = HashMap()

    private var text: String? = null

    open fun getField(keyName: String?): Any? {
        return fields[keyName]
    }

    open fun getText(): String? {
        return text
    }

    override fun parseField(keyName: String, value: Any) {
        fields[keyName] = value
    }

    override fun parseText(text: String) {
        this.text = text
    }
}