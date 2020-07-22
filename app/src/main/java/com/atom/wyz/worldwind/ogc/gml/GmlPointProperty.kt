package com.atom.wyz.worldwind.ogc.gml

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class GmlPointProperty : XmlModel() {

    open var points: MutableList<GmlPoint> = ArrayList()

    open var nilReason: String? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "Point" -> points.add((value as GmlPoint))
            "nilReason" -> nilReason = value as String
        }
    }
}