package com.atom.wyz.worldwind.util.xml

class DoubleModel : XmlModel {
    constructor(namespaceUri: String?) : super(namespaceUri)

    fun getValue(): Double? {
        val textValue = this.getField(CHARACTERS_CONTENT)?.toString()
        if (textValue == null) {
            return null
        } else {
            try {
                return textValue.toDouble()
            } catch (ignore: NumberFormatException) {
            }
        }
        return null
    }
}