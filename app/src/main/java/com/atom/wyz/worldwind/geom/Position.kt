package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.Logger

class Position(latitude: Double, longitude: Double) : Location(latitude, longitude) {
    var altitude :Double = 0.0
    companion object{
        fun fromDegrees(latitudeDegrees: Double, longitudeDegrees: Double, altitude: Double): Position {
            return Position(latitudeDegrees, longitudeDegrees, altitude)
        }
        fun fromRadians(latitudeRadians: Double, longitudeRadians: Double, altitude: Double): Position {
            return Position(Math.toDegrees(latitudeRadians), Math.toDegrees(longitudeRadians), altitude)
        }

        fun zero(): Position {
            return Position()
        }
    }


    constructor():this(0.0, 0.0 , 0.0)

    constructor(latitude: Double, longitude: Double , altitude : Double):this(latitude, longitude){
        this.altitude = altitude
    }

    constructor( position : Position ):this(position.latitude, position.longitude , position.altitude)


    override fun hashCode(): Int {
        var result = super.hashCode()
        val temp: Long
        temp = java.lang.Double.doubleToLongBits(altitude)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
    override fun equals(o: Any?): Boolean {
        if (o == null || this.javaClass != o.javaClass) {
            return false
        }
        val that: Position = o as Position
        return latitude == that.latitude && longitude == that.longitude && altitude == that.altitude
    }

    override fun toString(): String {
        return latitude.toString() + "\u00b0, " + longitude + "\u00b0, " + altitude
    }

    operator fun set(latitude: Double, longitude: Double, altitude: Double): Position {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        return this
    }

    override fun set(location: Any?): Position {
        if (location == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Position", "set", "missingPosition"))
        }
        if(location is Position){
            latitude = location.latitude
            longitude = location.longitude
            altitude = location.altitude
        }
        return this
    }

    fun interpolateAlongPath(@WorldWind.PathType pathType : Int, amount: Double, endPosition: Position, result: Position): Position {
        // Interpolate latitude and longitude.
        super.interpolateAlongPath(pathType, amount, endPosition, result)
        // Interpolate altitude.
        result.altitude = (1 - amount) * altitude + amount * endPosition.altitude
        return result
    }
}