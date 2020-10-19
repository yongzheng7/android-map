package com.atom.map.ogc.wms

import com.atom.map.geom.Sector
import com.atom.map.util.xml.XmlModel

open class WmsGeographicBoundingBox : XmlModel() {

    open var north = Double.NaN

    open var east = Double.NaN

    open var south = Double.NaN

    open var west = Double.NaN

    open fun getGeographicBoundingBox(): Sector {
        val deltaLongitude = east - west
        val deltaLatitude = north - south
        return Sector(south, west, deltaLatitude, deltaLongitude)
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "minx" -> {
                west = (value as String).toDouble()
            }
            "miny" -> {
                south = (value as String).toDouble()
            }
            "maxx" -> {
                east = (value as String).toDouble()
            }
            "maxy" -> {
                north = (value as String).toDouble()
            }
            "westBoundLongitude" -> {
                west = (value as String).toDouble()
            }
            "southBoundLatitude" -> {
                south = (value as String).toDouble()
            }
            "eastBoundLongitude" -> {
                east = (value as String).toDouble()
            }
            "northBoundLatitude" -> {
                north = (value as String).toDouble()
            }
        }
    }

}