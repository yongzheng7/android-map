package com.atom.map.ogc.ows

import com.atom.map.geom.Sector
import com.atom.map.util.Logger

open class OwsWgs84BoundingBox : OwsBoundingBox() {
    open fun getSector(): Sector? {
        return try {
            val lowerValues = lowerCorner!!.split("\\s+")
            val upperValues = upperCorner!!.split("\\s+")
            val minLon = lowerValues[0].toDouble()
            val minLat = lowerValues[1].toDouble()
            val maxLon = upperValues[0].toDouble()
            val maxLat = upperValues[1].toDouble()
            Sector(minLat, minLon, maxLat - minLat, maxLon - minLon)
        } catch (ex: Exception) {
            Logger.logMessage(
                Logger.ERROR,
                "OwsWgs84BoundingBox",
                "getSector",
                "Error parsing bounding box corners, " +
                        "LowerCorner=" + lowerCorner + " UpperCorner=" + upperCorner,
                ex
            )
            null
        }
    }
}