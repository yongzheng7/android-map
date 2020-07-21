package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.Logger
import java.util.*

open class ElevationModel : Iterable<ElevationCoverage> {

    open var coverages: MutableList<ElevationCoverage> = ArrayList()

    constructor() {}

    constructor(model: ElevationModel) {
        this.addAllCoverages(model)
    }

    constructor(iterable: Iterable<ElevationCoverage?>) {
        for (coverage in iterable) {
            if (coverage == null) {
                throw IllegalArgumentException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "ElevationModel",
                        "constructor",
                        "missingCoverage"
                    )
                )
            }
            this.addCoverage(coverage)
        }
    }

    open fun count(): Int {
        return coverages.size
    }

    open fun getCoverageNamed(name: String?): ElevationCoverage? {
        var idx = 0
        val len = coverages.size
        while (idx < len) {
            val coverage = coverages[idx]
            if (coverage.displayName.equals(name)) {
                return coverage
            }
            idx++
        }
        return null
    }

    open fun getCoverageWithProperty(key: Any, value: Any?): ElevationCoverage? {
        var idx = 0
        val len = coverages.size
        while (idx < len) {
            val coverage = coverages[idx]
            if (coverage.hasUserProperty(key)) {
                val coverageValue = coverage.getUserProperty(key)
                if (if (coverageValue == null) value == null else coverageValue == value) {
                    return coverage
                }
            }
            idx++
        }
        return null
    }

    open fun addCoverage(coverage: ElevationCoverage): Boolean {
        return !coverages.contains(coverage) && coverages.add(coverage)
    }

    open fun addAllCoverages(model: ElevationModel): Boolean {
        val thatList = model.coverages
        var changed = false
        var idx = 0
        val len = thatList.size
        while (idx < len) {
            changed = changed or addCoverage(thatList[idx])
            idx++
        }
        return changed
    }

    open fun removeCoverage(coverage: ElevationCoverage): Boolean {
        return coverages.remove(coverage)
    }

    open fun removeAllCoverages(model: ElevationModel): Boolean {
        return coverages.removeAll(model.coverages)
    }

    open fun clearCoverages() {
        coverages.clear()
    }

    override fun iterator(): Iterator<ElevationCoverage> {
        return coverages.iterator()
    }

    open fun getTimestamp(): Long {
        var maxTimestamp: Long = 0
        var idx = 0
        val len = coverages.size
        while (idx < len) {
            val coverage = coverages[idx]
            val timestamp: Long = coverage.timestamp
            if (maxTimestamp < timestamp) {
                maxTimestamp = timestamp
            }
            idx++
        }
        return maxTimestamp
    }

    open fun getHeight(
        latitude: Double,
        longitude: Double,
        result: FloatArray
    ) {
        for (idx in coverages.indices.reversed()) {
            val coverage = coverages[idx]
            coverage.getHeight(latitude, longitude, result)
        }
    }

    open fun getHeightGrid(
        gridSector: Sector,
        gridWidth: Int,
        gridHeight: Int,
        result: FloatArray
    ) {
        var idx = 0
        val len = coverages.size
        while (idx < len) {
            coverages[idx].getHeightGrid(gridSector, gridWidth, gridHeight, result)
            idx++
        }
    }

    open fun getHeightLimits(
        sector: Sector,
        result: FloatArray
    ) {
        var idx = 0
        val len = coverages.size
        while (idx < len) {
            val coverage = coverages[idx]
            coverage.getHeightLimits(sector, result)
            idx++
        }
    }

}