package com.atom.wyz.worldwind.ogc.wtms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsDescription : XmlModel() {

    open var titles: MutableList<OwsLanguageString> =
        ArrayList<OwsLanguageString>()

    open var abstracts: MutableList<OwsLanguageString> =
        ArrayList<OwsLanguageString>()

    open var keywords: MutableList<OwsLanguageString> =
        ArrayList<OwsLanguageString>()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Title" -> {
                titles.add(value as OwsLanguageString)
            }
            "Abstract" -> {
                abstracts.add(value as OwsLanguageString)
            }
            "Keywords" -> {
                keywords.addAll((value as OwsKeywords).keywords)
            }
        }
    }
}