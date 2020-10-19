package com.atom.map.globe

open class Ellipsoid {
    /**
     * One half of the ellipsoid's major axis length in meters, which runs through the center to opposite points on the
     * equator.
     */
    protected var semiMajorAxis = 1.0

    /**
     * Measure of the ellipsoid's compression. Indicates how much the ellipsoid's semi-minor axis is compressed relative
     * to the semi-major axis. Expressed as `1/f`, where `f = (a - b) / a`, given the semi-major axis `a` and the semi-minor axis `b`.
     */
    protected var inverseFlattening = 1.0

    /**
     * Constructs an ellipsoid with semi-major axis and inverse flattening both 1.0.
     */
    constructor()

    /**
     * Constructs an ellipsoid with a specified semi-major axis and inverse flattening.
     *
     * @param semiMajorAxis     one half of the ellipsoid's major axis length in meters, which runs through the center
     * to opposite points on the equator
     * @param inverseFlattening measure of the ellipsoid's compression, indicating how much the semi-minor axis is
     * compressed relative to the semi-major axis
     */
    constructor(
        semiMajorAxis: Double,
        inverseFlattening: Double
    ) {
        this.semiMajorAxis = semiMajorAxis
        this.inverseFlattening = inverseFlattening
    }

    /**
     * Constructs an ellipsoid with the semi-major axis and inverse flattening of a specified ellipsoid.
     *
     * @param ellipsoid the ellipsoid specifying the values
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    constructor(ellipsoid: Ellipsoid) {
        semiMajorAxis = ellipsoid.semiMajorAxis
        inverseFlattening = ellipsoid.inverseFlattening
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that = other as Ellipsoid
        return (semiMajorAxis == that.semiMajorAxis
                && inverseFlattening == that.inverseFlattening)
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long = semiMajorAxis.toBits()
        result = (temp xor (temp ushr 32)).toInt()
        temp = inverseFlattening.toBits()
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "semiMajorAxis=" + semiMajorAxis + ", inverseFlattening=" + inverseFlattening
    }

    /**
     * Sets this ellipsoid to a specified semi-major axis and inverse flattening.
     *
     * @param semiMajorAxis     the new semi-major axis length in meters, one half of the ellipsoid's major axis, which
     * runs through the center to opposite points on the equator
     * @param inverseFlattening the new inverse flattening, a measure of the ellipsoid's compression, indicating how
     * much the semi-minor axis is compressed relative to the semi-major axis
     *
     * @return this ellipsoid with its semi-major axis and inverse flattening set to the specified values
     */
    operator fun set(
        semiMajorAxis: Double,
        inverseFlattening: Double
    ): Ellipsoid {
        this.semiMajorAxis = semiMajorAxis
        this.inverseFlattening = inverseFlattening
        return this
    }

    /**
     * Sets this ellipsoid to the semi-major axis and inverse flattening of a specified ellipsoid.
     *
     * @param ellipsoid the ellipsoid specifying the new values
     *
     * @return this ellipsoid with its semi-major axis and inverse flattening set to that of the specified ellipsoid
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    fun set(ellipsoid: Ellipsoid): Ellipsoid {
        semiMajorAxis = ellipsoid.semiMajorAxis
        inverseFlattening = ellipsoid.inverseFlattening
        return this
    }

    /**
     * Computes this ellipsoid's semi-major axis length in meters. The semi-major axis is one half of the ellipsoid's
     * major axis, which runs through the center to opposite points on the equator.
     *
     * @return this ellipsoid's semi-major axis length in meters
     */
    fun semiMajorAxis(): Double {
        return semiMajorAxis
    }

    /**
     * Computes this ellipsoid's semi-minor length axis in meters. The semi-minor axis is one half of the ellipsoid's
     * minor axis, which runs through the center to opposite points on the poles.
     *
     * @return this ellipsoid's semi-minor axis length in meters
     */
    fun semiMinorAxis(): Double {
        val f = 1 / inverseFlattening
        return semiMajorAxis * (1 - f)
    }

    /**
     * Computes this ellipsoid's inverse flattening, a measure of an ellipsoid's compression. The returned value is
     * equivalent to `a / (a - b)`, where `a` and `b` indicate this ellipsoid's semi-major axis and
     * semi-minor axis, respectively.
     *
     * @return this ellipsoid's inverse flattening
     */
    fun inverseFlattening(): Double {
        return inverseFlattening
    }

    /**
     * Computes this ellipsoid's eccentricity squared. The returned value is equivalent to `2*f - f*f`,
     * where `f` is this ellipsoid's flattening.
     *
     * @return this ellipsoid's eccentricity squared
     */
    fun eccentricitySquared(): Double {
        val f = 1 / inverseFlattening
        return 2 * f - f * f
    }
}