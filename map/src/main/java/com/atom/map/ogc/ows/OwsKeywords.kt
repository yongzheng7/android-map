package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel
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