package com.atom.wyz.worldwind.util.xml

class IntegerModel : XmlModel {
    constructor(namespaceUri: String?) : super(namespaceUri)
    fun getValue(): Int? {
        val textValue = this.getField(CHARACTERS_CONTENT) ?.toString()
        if (textValue == null) {
            return null
        } else {
            try {
                return textValue.toInt()
            } catch (ignore: NumberFormatException) {
            }
        }
        return null
    }

}