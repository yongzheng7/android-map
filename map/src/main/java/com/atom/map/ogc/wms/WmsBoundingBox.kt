package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmsBoundingBox() : XmlModel() {

    open var crs: String? = null

    open var minx = 0.0

    open var maxx = 0.0

    open var miny = 0.0

    open var maxy = 0.0

    open var resx = 0.0

    open var resy = 0.0

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "CRS" -> {
                // Convention is to be in upper case
                crs = (value as String).toUpperCase(Locale.getDefault())
            }
            "SRS" -> {
                crs = (value as String).toUpperCase(Locale.getDefault())
            }
            "minx" -> {
                minx = (value as String).toDouble()
            }
            "miny" -> {
                miny = (value as String).toDouble()
            }
            "maxx" -> {
                maxx = (value as String).toDouble()
            }
            "maxy" -> {
                maxy = (value as String).toDouble()
            }
            "resx" -> {
                resx = (value as String).toDouble()
            }
            "resy" -> {
                resy = (value as String).toDouble()
            }
        }
    }
}