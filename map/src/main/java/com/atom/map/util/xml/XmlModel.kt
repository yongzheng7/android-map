package com.atom.map.util.xml

abstract class XmlModel() {

    var parent: XmlModel? = null

    open fun parseField(keyName: String, value: Any) {}

    open fun parseText(text: String) {}
}