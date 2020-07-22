package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.util.Logger

class Sector() {
    companion object {
        /**
         * 来自角度
         */
        fun fromDegrees(
            latitudeDegrees: Double, longitudeDegrees: Double,
            deltaLatitudeDegrees: Double, deltaLongitudeDegrees: Double
        ): Sector {
            return Sector(
                latitudeDegrees, longitudeDegrees,
                deltaLatitudeDegrees, deltaLongitudeDegrees
            )
        }

        /**
         * 来自弧度
         */
        fun fromRadians(
            latitudeRadians: Double, longitudeRadians: Double,
            deltaLatitudeRadians: Double, deltaLongitudeRadians: Double
        ): Sector {
            return Sector(
                Math.toDegrees(latitudeRadians), Math.toDegrees(longitudeRadians),
                Math.toDegrees(deltaLatitudeRadians), Math.toDegrees(deltaLongitudeRadians)
            )
        }
    }

    var minLatitude: Double = Double.NaN
    var maxLatitude: Double = Double.NaN
    var minLongitude: Double = Double.NaN
    var maxLongitude: Double = Double.NaN

    constructor(sector: Sector) : this() {
        set(sector)
    }

    constructor(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ) : this() {
        set(minLatitude, minLongitude, deltaLatitude, deltaLongitude)
    }

    operator fun set(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ): Sector {
        this.minLatitude = minLatitude
        this.minLongitude = minLongitude
        this.maxLatitude =
            Location.clampLatitude(minLatitude + if (deltaLatitude > 0) deltaLatitude else Double.NaN)
        this.maxLongitude =
            Location.clampLongitude(minLongitude + if (deltaLongitude > 0) deltaLongitude else Double.NaN)
        return this
    }

    fun set(sector: Sector): Sector {
        minLatitude = sector.minLatitude
        maxLatitude = sector.maxLatitude
        minLongitude = sector.minLongitude
        maxLongitude = sector.maxLongitude
        return this
    }

    fun setEmpty(): Sector {
        maxLatitude = Double.NaN
        minLatitude = Double.NaN
        maxLongitude = Double.NaN
        minLongitude = Double.NaN
        return this
    }

    fun setFullSphere(): Sector {
        minLatitude = -90.0
        maxLatitude = 90.0
        minLongitude = -180.0
        maxLongitude = 180.0
        return this
    }

    /**
     * 是否相交
     */
    fun intersects(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ): Boolean {
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        val maxLatitude: Double =
            Location.clampLatitude(minLatitude + if (deltaLatitude > 0) deltaLatitude else Double.NaN)
        val maxLongitude: Double =
            Location.clampLongitude(minLongitude + if (deltaLongitude > 0) deltaLongitude else Double.NaN)
        return _intersects(minLatitude, minLongitude, maxLatitude, maxLongitude)
    }

    private fun _intersects(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): Boolean {
        return this.minLatitude < maxLatitude && this.maxLatitude > minLatitude && this.minLongitude < maxLongitude && this.maxLongitude > minLongitude
    }

    /**
     * 是否相交
     */
    fun intersects(sector: Sector): Boolean {
        // Assumes normalized angles: [-90, +90], [-180, +180]
        return _intersects(
            sector.minLatitude,
            sector.minLongitude,
            sector.maxLatitude,
            sector.maxLongitude
        )
    }
    fun intersectsOrNextTo(sector: Sector): Boolean {
        // Assumes normalized angles: [-90, +90], [-180, +180]
        // Note: comparisons with NaN are always false
        return minLatitude <= sector.maxLatitude && maxLatitude >= sector.minLatitude && minLongitude <= sector.maxLongitude && maxLongitude >= sector.minLongitude
    }
    /**
     * 判断相交后 合并
     */
    fun intersect(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ): Boolean { // Assumes normalized angles: [-90, +90], [-180, +180]
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        val maxLatitude: Double =
            Location.clampLatitude(minLatitude + if (deltaLatitude > 0) deltaLatitude else Double.NaN)
        val maxLongitude: Double =
            Location.clampLongitude(minLongitude + if (deltaLongitude > 0) deltaLongitude else Double.NaN)
        val intersects = _intersects(minLatitude, minLongitude, maxLatitude, maxLongitude)
        if (intersects) {
            if (this.minLatitude < minLatitude) this.minLatitude = minLatitude
            if (this.maxLatitude > maxLatitude) this.maxLatitude = maxLatitude
            if (this.minLongitude < minLongitude) this.minLongitude = minLongitude
            if (this.maxLongitude > maxLongitude) this.maxLongitude = maxLongitude
        }

        return intersects

    }
    fun isFullSphere(): Boolean {
        return minLatitude == -90.0 && maxLatitude == 90.0 && minLongitude == -180.0 && maxLongitude == 180.0
    }
    /**
     * 判断相交后 合并
     */
    fun intersect(sector: Sector): Boolean {
        // Assumes normalized angles: [-90, +90], [-180, +180] and comparisons with NaN are always false
        if (intersects(sector)) { // longitudes intersect
            if (minLatitude < sector.minLatitude) minLatitude = sector.minLatitude
            if (maxLatitude > sector.maxLatitude) maxLatitude = sector.maxLatitude
            if (minLongitude < sector.minLongitude) minLongitude = sector.minLongitude
            if (maxLongitude > sector.maxLongitude) maxLongitude = sector.maxLongitude
            return true
        }
        return false // the two sectors do not intersect

    }

    /**
     * 判断是否包含该经纬度
     */
    fun contains(latitude: Double, longitude: Double): Boolean {
        // Assumes normalized angles: [-90, +90], [-180, +180] and comparisons with NaN are always false
        return latitude in minLatitude..maxLatitude && longitude in minLongitude..maxLongitude
    }

    /**
     * 判断是否包含该区域
     */
    fun contains(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ): Boolean {
        val maxLatitude: Double =
            Location.clampLatitude(minLatitude + if (deltaLatitude > 0) deltaLatitude else Double.NaN)
        val maxLongitude: Double =
            Location.clampLongitude(minLongitude + if (deltaLongitude > 0) deltaLongitude else Double.NaN)
        return _contains(minLatitude , minLongitude , maxLatitude , maxLongitude)
    }
    private fun _contains(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double) : Boolean {
        return this.minLatitude <= minLatitude && this.maxLatitude >= maxLatitude && this.minLongitude <= minLongitude && this.maxLongitude >= maxLongitude
    }

    /**
     * 判断是否包含该区域
     */
    operator fun contains(sector: Sector): Boolean {
        // Assumes normalized angles: [-90, +90], [-180, +180], and comparisons with NaN are always false
        return _contains(sector.minLatitude , sector.minLongitude , sector.maxLatitude , sector.maxLongitude)
    }

    /**
     * 判断两个区域是否存在
     * 接着进行获取两个区域想融合的大小
     */
    fun union(
        minLatitude: Double,
        minLongitude: Double,
        deltaLatitude: Double,
        deltaLongitude: Double
    ): Sector { // Assumes normalized angles: [-90, +90], [-180, +180]
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        val maxLatitude: Double =
            Location.clampLatitude(minLatitude + if (deltaLatitude > 0) deltaLatitude else Double.NaN)
        val maxLongitude: Double =
            Location.clampLongitude(minLongitude + if (deltaLongitude > 0) deltaLongitude else Double.NaN)
        if (minLatitude < maxLatitude && minLongitude < maxLongitude) { // specified sector not empty
            if (!isEmpty()) { // this sector not empty
                if (this.minLatitude > minLatitude) this.minLatitude = minLatitude
                if (this.maxLatitude < maxLatitude) this.maxLatitude = maxLatitude
                if (this.minLongitude > minLongitude) this.minLongitude = minLongitude
                if (this.maxLongitude < maxLongitude) this.maxLongitude = maxLongitude
            } else { // this sector is empty, set to the specified sector
                this.minLatitude = minLatitude
                this.maxLatitude = maxLatitude
                this.minLongitude = minLongitude
                this.maxLongitude = maxLongitude
            }
        }

        return this
    }

    /**
     * 判断两个区域是否存在
     * 接着进行获取两个区域想融合的大小
     */
    fun union(sector: Sector): Sector {
        // Assumes normalized angles: [-90, +90], [-180, +180]
        if (!sector.isEmpty()) { // specified sector not empty
            if (!isEmpty()) { // this sector not empty
                if (minLatitude > sector.minLatitude) minLatitude = sector.minLatitude
                if (maxLatitude < sector.maxLatitude) maxLatitude = sector.maxLatitude
                if (minLongitude > sector.minLongitude) minLongitude = sector.minLongitude
                if (maxLongitude < sector.maxLongitude) maxLongitude = sector.maxLongitude
            } else { // this sector is empty, set to the specified sector
                minLatitude = sector.minLatitude
                maxLatitude = sector.maxLatitude
                minLongitude = sector.minLongitude
                maxLongitude = sector.maxLongitude
            }
        }
        return this
    }

    fun union(latitude: Double, longitude: Double): Sector {
        // Assumes normalized angles: [-90, +90], [-180, +180], and comparisons with NaN are always false
        if (!isEmpty()) {
            maxLatitude = Math.max(maxLatitude, latitude)
            minLatitude = Math.min(minLatitude, latitude)
            maxLongitude = Math.max(maxLongitude, longitude)
            minLongitude = Math.min(minLongitude, longitude)
        } else if (!java.lang.Double.isNaN(minLatitude) && !java.lang.Double.isNaN(minLongitude)) { // Note, order is important, set the max members first
            maxLatitude = Math.max(minLatitude, latitude)
            maxLongitude = Math.max(minLongitude, longitude)
            minLatitude = Math.min(minLatitude, latitude)
            minLongitude = Math.min(minLongitude, longitude)
        } else {
            minLatitude = latitude
            minLongitude = longitude
            maxLatitude = Double.NaN
            maxLongitude = Double.NaN
        }
        return this
    }


    /**
     * 判断多个是否存在
     * 接着进行获取两个区域想融合的大小
     */
    fun union(array: FloatArray, count: Int, stride: Int): Sector {
        require(array.size >= stride) {
            Logger.logMessage(
                Logger.ERROR,
                "Sector",
                "union",
                "missingArray"
            )
        }
        require(count >= 0) { Logger.logMessage(Logger.ERROR, "Sector", "union", "invalidCount") }
        require(stride >= 2) { Logger.logMessage(Logger.ERROR, "Sector", "union", "invalidStride") }

        var minLat = if (java.lang.Double.isNaN(minLatitude)) Double.MAX_VALUE else minLatitude
        var maxLat = if (java.lang.Double.isNaN(maxLatitude)) -Double.MAX_VALUE else maxLatitude
        var minLon = if (java.lang.Double.isNaN(minLongitude)) Double.MAX_VALUE else minLongitude
        var maxLon = if (java.lang.Double.isNaN(maxLongitude)) -Double.MAX_VALUE else maxLongitude

        var idx = 0
        while (idx < count) {
            val lon = array[idx]
            val lat = array[idx + 1]
            if (maxLat < lat) {
                maxLat = lat.toDouble()
            }
            if (minLat > lat) {
                minLat = lat.toDouble()
            }
            if (maxLon < lon) {
                maxLon = lon.toDouble()
            }
            if (minLon > lon) {
                minLon = lon.toDouble()
            }
            idx += stride
        }

        if (minLat < Double.MAX_VALUE) {
            minLatitude = minLat
        }
        if (maxLat > -Double.MAX_VALUE) {
            maxLatitude = maxLat
        }
        if (minLon < Double.MAX_VALUE) {
            minLongitude = minLon
        }
        if (maxLon > -Double.MAX_VALUE) {
            maxLongitude = maxLon
        }
        return this // TODO
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that: Sector = other as Sector
        return minLatitude == that.minLatitude && maxLatitude == that.maxLatitude && minLongitude == that.minLongitude && maxLongitude == that.maxLongitude
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(minLatitude)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(maxLatitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(minLongitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(maxLongitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /**
     * 判断是非为空区域
     */
    fun isEmpty(): Boolean {
        // Note: Comparison with a NaN is always false.
        return !(minLatitude < maxLatitude && minLongitude < maxLongitude)
    }

    /**
     * 获取该区域的纬度跨度
     */
    fun deltaLatitude(): Double {
        return maxLatitude - minLatitude
    }

    /**
     * 获取该区域的经度跨度
     */
    fun deltaLongitude(): Double {
        return maxLongitude - minLongitude
    }

    /**
     * 获取该区域纬度中心
     */
    fun centroidLatitude(): Double {
        return 0.5 * (minLatitude + maxLatitude)
    }

    /**
     * 获取该区域经度中心
     */
    fun centroidLongitude(): Double {
        return 0.5 * (minLongitude + maxLongitude)
    }

    /**
     * 获取该区域中心点经纬度
     */
    fun centroid(result: Location): Location {
        result.latitude = centroidLatitude()
        result.longitude = centroidLongitude()
        return result
    }

    fun translate(deltaLatitude: Double, deltaLongitude: Double): Sector {
        minLatitude += deltaLatitude
        maxLatitude += deltaLatitude
        minLongitude += deltaLongitude
        maxLongitude += deltaLongitude
        return this
    }
    override fun toString(): String {
        return "minLatitude=$minLatitude, maxLatitude=$maxLatitude, minLongitude=$minLongitude, maxLongitude=$maxLongitude"
    }


}