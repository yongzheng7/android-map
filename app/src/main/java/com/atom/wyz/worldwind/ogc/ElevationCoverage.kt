package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import java.util.*

interface ElevationCoverage {

    var displayName: String

    var enabled: Boolean

    var timestamp : Long

    var userProperties: HashMap<Any, Any>?

    fun getUserProperty(key: Any): Any?

    fun putUserProperty(key: Any, value: Any): Any?

    fun removeUserProperty(key: Any): Any?

    fun hasUserProperty(key: Any): Boolean

    fun getHeight(latitude: Double, longitude: Double ,  result : FloatArray )

    fun getHeightGrid(gridSector: Sector, gridWidth: Int, gridHeight: Int,  result: FloatArray)

    fun getHeightLimits(sector: Sector, result: FloatArray)

}