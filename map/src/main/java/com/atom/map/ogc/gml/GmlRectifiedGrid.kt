package com.atom.map.ogc.gml

import java.util.*

open class GmlRectifiedGrid : GmlGrid() {

    open var origin: GmlPointProperty? = null

    open var offsetVector: MutableList<GmlVector> = ArrayList<GmlVector>()
    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "origin" -> origin = value as GmlPointProperty
            "offsetVector" -> offsetVector.add((value as GmlVector))
        }
    }
}