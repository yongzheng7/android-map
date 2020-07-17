package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsKeywords : XmlModel() {

    open var keywords: MutableList<OwsLanguageString> =
        ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Keyword" -> {
                keywords.add((value as OwsLanguageString))
            }
        }
    }
}