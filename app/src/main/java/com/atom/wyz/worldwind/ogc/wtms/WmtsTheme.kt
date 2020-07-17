package com.atom.wyz.worldwind.ogc.wtms

import java.util.*

open class WmtsTheme: OwsDescription() {
    open var identifier: String? = null

    open var themes: MutableList<WmtsTheme> = ArrayList()

    open var layerRefs: MutableList<String> = ArrayList()

    override  fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Identifier" -> {
                identifier = value as String
            }
            "Theme" -> {
                themes.add((value as WmtsTheme))
            }
            "LayerRef" -> {
                layerRefs.add((value as String))
            }
        }
    }
}