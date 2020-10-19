package com.atom.map.ogc

import com.atom.map.geom.Sector
import java.util.*

abstract class AbstractElevationCoverage :
    ElevationCoverage {

    override var displayName: String = ""

    override var enabled: Boolean = true
        set(value) {
            field = value
            this.updateTimestamp()
        }

    override var timestamp: Long = 0

    override var userProperties: HashMap<Any, Any>? = null

    constructor() {
        this.updateTimestamp()
    }

    protected open fun updateTimestamp() {
        timestamp = System.currentTimeMillis()
    }

    override fun getUserProperty(key: Any): Any? {
        return userProperties?.get(key)
    }

    override fun putUserProperty(key: Any, value: Any): Any? {
        if (userProperties == null) {
            userProperties = HashMap()
        }
        return userProperties?.put(key, value)
    }

    override fun removeUserProperty(key: Any): Any? {
        return userProperties?.remove(key)
    }

    override fun hasUserProperty(key: Any): Boolean {
        return userProperties?.containsKey(key) ?: false
    }

    override fun getHeight(latitude: Double, longitude: Double, result: FloatArray){
        if (!this.enabled) {
            return
        }
         doGetHeight(latitude, longitude, result)
    }

    override fun getHeightGrid(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        result: FloatArray
    ) {
        if (!this.enabled) {
            return
        }
        doGetHeightGrid(gridSector, gridWidth, gridHeight,  result)
    }

    override fun getHeightLimits(sector: Sector,  result: FloatArray) {
        if (!this.enabled) {
            return
        }
        doGetHeightLimits(sector,  result)
    }

    abstract fun doGetHeight(
        latitude: Double,
        longitude: Double,
        result: FloatArray
    )

    abstract fun doGetHeightGrid(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        result: FloatArray
    )

    abstract fun doGetHeightLimits(
        sector: Sector,
        result: FloatArray
    )
}