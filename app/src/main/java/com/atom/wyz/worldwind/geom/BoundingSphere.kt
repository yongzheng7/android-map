package com.atom.wyz.worldwind.geom

import com.atom.wyz.worldwind.geom.observer.Frustum
import com.atom.wyz.worldwind.util.Logger

class BoundingSphere {
    /**
     * The sphere's center point.
     */
    val center = Vec3()

    /**
     * The sphere's radius.
     */
    var radius = 1f

    constructor() {}

    override fun equals(other: Any?): Boolean {
        if (other == null || this.javaClass != other.javaClass) {
            return false
        }
        val that = other as BoundingSphere
        return (radius == that.radius && center.equals(that.center))
    }

    override fun hashCode(): Int {
        var result: Int = center.hashCode()
        val temp: Long = radius.toLong()
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "center=$center, radius=$radius"
    }

    operator fun set(center: Vec3, radius: Float): BoundingSphere {
        if (radius < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingSphere", "set", "invalidRadius")
            )
        }
        this.center.set(center)
        this.radius = radius
        return this
    }

    fun intersectsFrustum(frustum: Frustum): Boolean {
        // See if the extent's bounding sphere is within or intersects the frustum. The dot product of the extent's
        // center point with each plane's vector provides a distance to each plane. If this distance is less than
        // -radius, the extent is completely clipped by that plane and therefore does not intersect the space enclosed
        // by this Frustum.
        val nr = -radius
        if (frustum.near.distanceToPoint(center) <= nr) {
            return false
        }
        if (frustum.far.distanceToPoint(center) <= nr) {
            return false
        }
        if (frustum.left.distanceToPoint(center) <= nr) {
            return false
        }
        if (frustum.right.distanceToPoint(center) <= nr) {
            return false
        }
        if (frustum.top.distanceToPoint(center) <= nr) {
            return false
        }
        if (frustum.bottom.distanceToPoint(center) <= nr) {
            return false
        }
        return true
    }

}