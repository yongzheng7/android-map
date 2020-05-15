package com.atom.wyz.worldwind.geom

import android.util.SparseIntArray
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger
import java.util.*

open class Location(var latitude: Double, var longitude: Double) {

    companion object {

        protected var timeZoneLatitudes = SparseIntArray()
        protected const val TOLERANCE = 1e-15

        init {
            timeZoneLatitudes.put(-12, -45)// GMT-12
            timeZoneLatitudes.put(-11, -30)// GMT-11
            timeZoneLatitudes.put(-10, 20) // GMT-10
            timeZoneLatitudes.put(-9, 45)  // GMT-9
            timeZoneLatitudes.put(-8, 40)  // GMT-8
            timeZoneLatitudes.put(-7, 35)  // GMT-7
            timeZoneLatitudes.put(-6, 30)  // GMT-6
            timeZoneLatitudes.put(-5, 25)  // GMT-5
            timeZoneLatitudes.put(-4, -15) // GMT-4
            timeZoneLatitudes.put(-3, 0)   // GMT-3
            timeZoneLatitudes.put(-2, 45)  // GMT-2
            timeZoneLatitudes.put(-1, 30)   // GMT-1
            timeZoneLatitudes.put(0, 30)   // GMT+0
            timeZoneLatitudes.put(1, 20)   // GMT+1
            timeZoneLatitudes.put(2, 20)   // GMT+2
            timeZoneLatitudes.put(3, 25)   // GMT+3
            timeZoneLatitudes.put(4, 30)   // GMT+4
            timeZoneLatitudes.put(5, 35)   // GMT+5
            timeZoneLatitudes.put(6, 30)   // GMT+6
            timeZoneLatitudes.put(7, 25)   // GMT+7
            timeZoneLatitudes.put(8, -30)  // GMT+8
            timeZoneLatitudes.put(9, -30)  // GMT+9
            timeZoneLatitudes.put(10, -30) // GMT+10
            timeZoneLatitudes.put(11, -45) // GMT+11
            timeZoneLatitudes.put(12, -45) // GMT+12
        }

        /**
         * Constructs an approximate location for a specified time zone. Used when selecting an initial navigator position
         * based on the device's current time zone.
         * 构造指定时区的大概位置。 根据设备的当前时区选择初始导航器位置时使用。
         */
        fun fromTimeZone(timeZone: TimeZone?): Location {
            if (timeZone == null) {
                throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Location", "fromTimeZone", "The time zone is null")
                )
            }
            val millisPerHour = 3.6e6 // 毫秒
            val offsetMillis = timeZone.rawOffset
            val offsetHours = (offsetMillis / millisPerHour).toInt()
            val lat: Double = timeZoneLatitudes.get(offsetHours, 0)
                .toDouble() // use a pre-determined latitude or 0 if none is available
            val lon = 180 * offsetHours / 12.toDouble() // center on the time zone's average longitude
            return Location(lat, lon)
        }

        /**
         * 直接经纬度创建生成一个Location
         */
        fun fromDegrees(latitudeDegrees: Double, longitudeDegrees: Double): Location {
            return Location(latitudeDegrees, longitudeDegrees)
        }

        /**
         * 直接根据弧度创建生成一个Location
         */
        fun fromRadians(latitudeRadians: Double, longitudeRadians: Double): Location {
            return Location(Math.toDegrees(latitudeRadians), Math.toDegrees(longitudeRadians))
        }

        /**
         * 规格化 经度
         */
        fun normalizeLongitude(degrees: Double): Double {
            val angle = degrees % 360
            return if (angle > 180) angle - 360 else if (angle < -180) 360 + angle else angle
        }

        /**
         * 规格化 纬度
         */
        fun normalizeLatitude(degreesLatitude: Double): Double {
            val lat = degreesLatitude % 180
            val normalizedLat = if (lat > 90) 180 - lat else if (lat < -90) -180 - lat else lat
            // Determine whether whether the latitude is in the north or south hemisphere
            val numEquatorCrosses = (degreesLatitude / 180).toInt()
            return if (numEquatorCrosses % 2 == 0) normalizedLat else -normalizedLat

        }

        fun zero(): Location {
            return Location(0.0, 0.0)
        }

        /**
         * 比较纬度
         */
        fun clampLatitude(degreesLatitude: Double): Double {
            return if (degreesLatitude > 90.0) 90.0 else if (degreesLatitude < -90.0) -90.0 else degreesLatitude
        }

        fun clampLongitude(degreesLongitude: Double): Double {
            return if (degreesLongitude > 180.0) 180.0 else if (degreesLongitude < -180.0) -180.0 else degreesLongitude
        }

        /**
         * 确定位置列表是否跨越了子午线。
         */
        fun locationsCrossAntimeridian(locations: List<out Location>): Boolean {

            // Check the list's length. A list with fewer than two locations does not cross the antimeridan.
            val len = locations.size
            if (len < 2) {
                return false
            }
            // Compute the longitude attributes associated with the first location.
            var lon1: Double = normalizeLongitude(locations[0].longitude)
            var sig1 = Math.signum(lon1)

            // Iterate over the segments in the list. A segment crosses the antimeridian if its endpoint longitudes have
            // different signs and are more than 180 degrees apart (but not 360, which indicates the longitudes are the same).
            for (idx in 1 until len) {
                val lon2: Double = normalizeLongitude(locations[idx].longitude)
                val sig2 = Math.signum(lon2)
                if (sig1 != sig2) {
                    val delta = Math.abs(lon1 - lon2)
                    if (delta > 180 && delta < 360) {
                        return true
                    }
                }
                lon1 = lon2
                sig1 = sig2
            }
            return false
        }
    }

    constructor() : this(0.0, 0.0) {
    }

    constructor(location: Location) : this(location.latitude, location.longitude) {
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val location: Location = other as Location
        return (latitude == location.latitude && longitude == location.longitude)
    }

    override fun toString(): String {
        return "" + latitude + "\u00b0, " + longitude + "\u00b0"
    }

    open fun set(latitude: Double, longitude: Double): Location {
        this.latitude = latitude
        this.longitude = longitude
        return this
    }

    open fun set(location: Any?): Location {
        if (location == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "set", "missingPosition")
            )
        }
        if (location is Location) {
            latitude = location.latitude
            longitude = location.longitude
        }
        return this
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(latitude)
        result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /**
     * 沿路径在两个指定位置之间的指定距离处计算位置。计算中心点位置经纬度
     */
    open fun interpolateAlongPath(
        @WorldWind.PathType pathType: Int, amount: Double,
        endLocation: Location?,
        result: Location?
    ): Location {
        if (endLocation == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingLocation")
            )
        }
        if (result == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingResult")
            )
        }
        if (this == endLocation) {
            result.set(this)
            return this
        }
        return if (pathType == WorldWind.GREAT_CIRCLE) {
            val azimuthDegrees: Double = this.greatCircleAzimuth(endLocation)
            val distanceRadians: Double = this.greatCircleDistance(endLocation) * amount
            this.greatCircleLocation(azimuthDegrees, distanceRadians, result)
        } else if (pathType == WorldWind.RHUMB_LINE) {
            val azimuthDegrees: Double = this.rhumbAzimuth(endLocation)
            val distanceRadians: Double = this.rhumbDistance(endLocation) * amount
            this.rhumbLocation(azimuthDegrees, distanceRadians, result)
        } else {
            val azimuthDegrees: Double = this.linearAzimuth(endLocation)
            val distanceRadians: Double = this.linearDistance(endLocation) * amount
            this.linearLocation(azimuthDegrees, distanceRadians, result)
        }
    }

    /**
     * 计算从第一个位置指向第二个位置的方位角（从北向顺时针方向）。
     * 该角度可用作大圆弧的起始方位角，该圆弧从第一个位置开始并经过第二个位置。
     * 此函数使用球形模型，而不是椭圆模型。
     * 大圈方位角
     * @param location The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    open fun greatCircleAzimuth(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleAzimuth", "missingLocation")
            )
        }
        val lat1: Double = Math.toRadians(latitude)
        val lat2: Double = Math.toRadians(location.latitude)

        val lon1: Double = Math.toRadians(longitude)
        val lon2: Double = Math.toRadians(location.longitude)
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }
        if (lon1 == lon2) { // 如果两个经纬度 其中经度一样纬度 比较
            return if (lat1 > lat2) 180.0 else 0.0
        }

        val y = Math.cos(lat2) * Math.sin(lon2 - lon1)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
        val azimuthRadians = Math.atan2(y, x)
        return if (java.lang.Double.isNaN(azimuthRadians)) 0.0 else Math.toDegrees(azimuthRadians)
    }

    /**
     * Computes the great circle angular distance between two locations. The return value gives the
     * distance as the angle between the two positions. In radians, this angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe. This function uses a spherical model,
     * not elliptical.
     * 计算两个位置之间的大圆角距离。 返回值给出距离作为两个位置之间的角度。
     * 以弧度表示，此角度是两个位置之间的线段的弧长。
     * 要从该值计算以米为单位的距离，请将返回值乘以地球仪的半径。
     * 此函数使用球形模型，而不是椭圆形。
     */
    open fun greatCircleDistance(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleDistance", "missingLocation")
            )
        }
        val lat1Radians: Double = Math.toRadians(latitude)
        val lat2Radians: Double = Math.toRadians(location.latitude)

        val lon1Radians: Double = Math.toRadians(longitude)
        val lon2Radians: Double = Math.toRadians(location.longitude)

        if (lat1Radians == lat2Radians && lon1Radians == lon2Radians) {
            return 0.0
        }
        val a = Math.sin((lat2Radians - lat1Radians) / 2.0)
        val b = Math.sin((lon2Radians - lon1Radians) / 2.0)
        val c = a * a + Math.cos(lat1Radians) * Math.cos(lat2Radians) * b * b
        val distanceRadians = 2.0 * Math.asin(Math.sqrt(c))
        return if (java.lang.Double.isNaN(distanceRadians)) 0.0 else distanceRadians
    }

    /**
     * Computes the location on a great circle path corresponding to a given starting location,
     * azimuth, and arc distance. This function uses a spherical model, not elliptical.
     * 计算与给定起始位置，方位角和弧距相对应的大圆路径上的位置。 此函数使用球形模型，而不是椭圆模型。
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the end
     * location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     * undefined.
     */
    open fun greatCircleLocation(azimuthDegrees: Double, distanceRadians: Double, result: Location?): Location {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleLocation", "missingResult")
            )
        }
        if (distanceRadians == 0.0) {
            result.latitude = latitude
            result.longitude = longitude
            return result
        }
        val latRadians: Double = Math.toRadians(latitude)
        val lonRadians: Double = Math.toRadians(longitude)
        val azimuthRadians: Double = Math.toRadians(azimuthDegrees)
        // Taken from "Map Projections - A Working Manual", page 31, equation 5-5 and 5-6.
        val endLatRadians = Math.asin(
            Math.sin(latRadians) * Math.cos(distanceRadians) +
                    Math.cos(latRadians) * Math.sin(distanceRadians) * Math.cos(azimuthRadians)
        )
        val endLonRadians = lonRadians + Math.atan2(
            Math.sin(distanceRadians) * Math.sin(azimuthRadians),
            Math.cos(latRadians) * Math.cos(distanceRadians) -
                    Math.sin(latRadians) * Math.sin(distanceRadians) * Math.cos(azimuthRadians)
        )
        if (java.lang.Double.isNaN(endLatRadians) || java.lang.Double.isNaN(endLonRadians)) {
            result.latitude = latitude
            result.longitude = longitude
        } else {
            result.latitude = normalizeLatitude(Math.toDegrees(endLatRadians))
            result.longitude = normalizeLongitude(Math.toDegrees(endLonRadians))
        }
        return result
    }

    /**
     * Computes the azimuth angle (clockwise from North) location points from the first location to the
     * second location. This angle can be used as the azimuth for a rhumb arc location begins at the
     * first location, and passes through the second location. This function uses a spherical model,
     * not elliptical.
     *
     * @param location The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    open fun rhumbAzimuth(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbAzimuth", "missingLocation")
            )
        }
        val lat1: Double = Math.toRadians(latitude)
        val lat2: Double = Math.toRadians(location.latitude)
        val lon1: Double = Math.toRadians(longitude)
        val lon2: Double = Math.toRadians(location.longitude)
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }
        var dLon = lon2 - lon1
        val dPhi = Math.log(
            Math.tan(lat2 / 2.0 + Math.PI / 4) /
                    Math.tan(lat1 / 2.0 + Math.PI / 4)
        )
        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = if (dLon > 0) -(2 * Math.PI - dLon) else 2 * Math.PI + dLon
        }
        val azimuthRadians = Math.atan2(dLon, dPhi)
        return if (java.lang.Double.isNaN(azimuthRadians)) 0.0 else Math.toDegrees(azimuthRadians)
    }

    /**
     * Computes the rhumb angular distance between two locations. The return value gives the
     * distance as the angle between the two positions in radians. This angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe. This function uses a spherical model,
     * not elliptical.
     */
    open fun rhumbDistance(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbDistance", "missingLocation")
            )
        }
        val lat1: Double = Math.toRadians(latitude)
        val lat2: Double = Math.toRadians(location.latitude)
        val lon1: Double = Math.toRadians(longitude)
        val lon2: Double = Math.toRadians(location.longitude)
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }
        val dLat = lat2 - lat1
        var dLon = lon2 - lon1


        val q: Double
        q =
            if (Math.abs(dLat) < TOLERANCE) { // Avoid indeterminates along E/W courses when lat end points are "nearly" identical
                Math.cos(lat1)
            } else {
                val dPhi = Math.log(
                    Math.tan(lat2 / 2.0 + Math.PI / 4) /
                            Math.tan(lat1 / 2.0 + Math.PI / 4)
                )
                dLat / dPhi
            }

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = if (dLon > 0) -(2 * Math.PI - dLon) else 2 * Math.PI + dLon
        }
        val distanceRadians = Math.sqrt(dLat * dLat + q * q * dLon * dLon)
        return if (java.lang.Double.isNaN(distanceRadians)) 0.0 else distanceRadians
    }

    /**
     * Computes the location on a rhumb arc with the given starting location, azimuth, and arc
     * distance. This function uses a spherical model, not elliptical.
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     * undefined.
     */
    open fun rhumbLocation(azimuthDegrees: Double, distanceRadians: Double, result: Location?): Location {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbLocation", "missingResult")
            )
        }
        if (distanceRadians == 0.0) {
            result.latitude = latitude
            result.longitude = longitude
            return result
        }
        val latRadians: Double = Math.toRadians(latitude)
        val lonRadians: Double = Math.toRadians(longitude)
        val azimuthRadians: Double = Math.toRadians(azimuthDegrees)
        var endLatRadians = latRadians + distanceRadians * Math.cos(azimuthRadians)
        val endLonRadians: Double

        val dLat = endLatRadians - latRadians
        val q: Double
        q =
            if (Math.abs(dLat) < TOLERANCE) { // Avoid indeterminates along E/W courses when lat end points are "nearly" identical
                Math.cos(latRadians)
            } else {
                val dPhi = Math.log(
                    Math.tan(endLatRadians / 2 + Math.PI / 4) /
                            Math.tan(latRadians / 2 + Math.PI / 4)
                )
                dLat / dPhi
            }


        val dLon = distanceRadians * Math.sin(azimuthRadians) / q
        // Handle latitude passing over either pole.
        if (Math.abs(endLatRadians) > Math.PI / 2) {
            endLatRadians = if (endLatRadians > 0) Math.PI - endLatRadians else -Math.PI - endLatRadians
        }
        endLonRadians = (lonRadians + dLon + Math.PI) % (2 * Math.PI) - Math.PI
        if (java.lang.Double.isNaN(endLatRadians) || java.lang.Double.isNaN(endLonRadians)) {
            result.latitude = latitude
            result.longitude = longitude
        } else {
            result.latitude = Location.normalizeLatitude(Math.toDegrees(endLatRadians))
            result.longitude = Location.normalizeLongitude(Math.toDegrees(endLonRadians))
        }
        return result
    }

    /**
     * Computes the azimuth angle (clockwise from North) location points from the first location to the
     * second location. This angle can be used as the azimuth for a linear arc location begins at the
     * first location, and passes through the second location.
     *
     * @param location The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    open fun linearAzimuth(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearAzimuth", "missingLocation")
            )
        }
        val lat1: Double = Math.toRadians(latitude)
        val lat2: Double = Math.toRadians(location.latitude)
        val lon1: Double = Math.toRadians(longitude)
        val lon2: Double = Math.toRadians(location.longitude)
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }
        var dLon = lon2 - lon1
        val dPhi = lat2 - lat1
        // If longitude change is over 180 take shorter path across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = if (dLon > 0) -(2 * Math.PI - dLon) else 2 * Math.PI + dLon
        }
        val azimuthRadians = Math.atan2(dLon, dPhi)
        return if (java.lang.Double.isNaN(azimuthRadians)) 0.0 else Math.toDegrees(azimuthRadians)
    }

    /**
     * Computes the linear angular distance between two locations. The return value gives the
     * distance as the angle between the two positions in radians. This angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe.
     *
     * @param location The ending location.
     *
     * @return The computed distance, in radians.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    open fun linearDistance(location: Location?): Double {
        if (location == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearDistance", "missingLocation")
            )
        }
        val lat1: Double = Math.toRadians(latitude)
        val lat2: Double = Math.toRadians(location.latitude)
        val lon1: Double = Math.toRadians(longitude)
        val lon2: Double = Math.toRadians(location.longitude)
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }
        val dLat = lat2 - lat1
        var dLon = lon2 - lon1
        // If lonChange over 180 take shorter path across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = if (dLon > 0) -(2 * Math.PI - dLon) else 2 * Math.PI + dLon
        }
        val distanceRadians = Math.sqrt(dLat * dLat + dLon * dLon)
        return if (java.lang.Double.isNaN(distanceRadians)) 0.0 else distanceRadians
    }

    /**
     * Computes the location on a linear path with the given starting location, azimuth, and arc
     * distance.
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     * undefined.
     */
    open fun linearLocation(azimuthDegrees: Double, distanceRadians: Double, result: Location?): Location {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearLocation", "missingResult")
            )
        }
        if (distanceRadians == 0.0) {
            result.latitude = latitude
            result.longitude = longitude
            return result
        }
        val latRadians: Double = Math.toRadians(latitude)
        val lonRadians: Double = Math.toRadians(longitude)
        val azimuthRadians: Double = Math.toRadians(azimuthDegrees)
        var endLatRadians = latRadians + distanceRadians * Math.cos(azimuthRadians)
        val endLonRadians: Double
        // Handle latitude passing over either pole.
        if (Math.abs(endLatRadians) > Math.PI / 2) {
            endLatRadians = if (endLatRadians > 0) Math.PI - endLatRadians else -Math.PI - endLatRadians
        }
        endLonRadians = (lonRadians + distanceRadians * Math.sin(azimuthRadians) + Math.PI) %
                (2 * Math.PI) - Math.PI
        if (java.lang.Double.isNaN(endLatRadians) || java.lang.Double.isNaN(endLonRadians)) {
            result.latitude = latitude
            result.longitude = longitude
        } else {
            result.latitude = Location.normalizeLatitude(Math.toDegrees(endLatRadians))
            result.longitude = Location.normalizeLongitude(Math.toDegrees(endLonRadians))
        }
        return result
    }
}