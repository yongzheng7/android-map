package com.atom.map.ogc.wtms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmtsThemes : XmlModel() {

    open var themes: MutableList<WmtsTheme> = ArrayList()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Theme" -> {
                themes.add((value as WmtsTheme))
            }
        }
    }
}