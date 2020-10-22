package com.atom.wyz.worldwind


import com.atom.map.geom.Matrix4
import com.atom.map.geom.Plane
import com.atom.map.geom.Vec3
import com.atom.map.util.Logger
import com.atom.map.util.WWMath
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class)
class PlaneTest {
    @Before
    @Throws(Exception::class)
    fun setUp() { // To accommodate WorldWind exception handling, we must mock all
// the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger::class.java)
    }



    @Test
    fun testConstructor_Default() {
        val plane = Plane()
        Assert.assertNotNull(plane)
        assertEquals("normal x", plane.normal.x, 0.0, 0.0)
        assertEquals("normal y", plane.normal.y, 0.0, 0.0)
        assertEquals("normal z", plane.normal.z, 1.0, 0.0)
        assertEquals("distance", plane.distance, 0.0, 0.0)
    }

    @Test
    fun testConstructor_Values() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 6.0
        val plane = Plane(n.x, n.y, n.z, distance)
        Assert.assertNotNull(plane)
        assertEquals("normal x", plane.normal.x, n.x, 0.0)
        assertEquals("normal y", plane.normal.y, n.y, 0.0)
        assertEquals("normal z", plane.normal.z, n.z, 0.0)
        assertEquals("distance", plane.distance, distance, 0.0)
    }

    @Test
    fun testConstructor_NotNormalizedValues() {
        val n = Vec3(3.0, 4.0, 5.0)
        val nExpected: Vec3 = Vec3(n).normalize()
        val distance = 6.0
        val distanceExpected: Double = distance / n.magnitude()
        val plane = Plane(n.x, n.y, n.z, distance)
        assertEquals("normal x", plane.normal.x, nExpected.x, 0.0)
        assertEquals("normal y", plane.normal.y, nExpected.y, 0.0)
        assertEquals("normal z", plane.normal.z, nExpected.z, 0.0)
        assertEquals("distance", plane.distance, distanceExpected, 0.0)
    }

    @Test
    fun testConstructor_ZeroValues() {
        val plane = Plane(0.0, 0.0, 0.0, 0.0)
        assertEquals("normal x", plane.normal.x, 0.0, 0.0)
        assertEquals("normal y", plane.normal.y, 0.0, 0.0)
        assertEquals("normal z", plane.normal.z, 0.0, 0.0)
        assertEquals("distance", plane.distance, 0.0, 0.0)
    }

    @Test
    fun testConstructor_Copy() {
        val plane = Plane(0.0, 0.0, 1.0, 10.0)
        val copy = Plane(plane)
        Assert.assertNotNull("copy", copy)
        assertEquals("copy equal to original", plane, copy)
    }

    @Test
    @Throws(Exception::class)
    fun testEquals() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 6.0
        val plane1 = Plane(n.x, n.y, n.z, distance)
        val plane2 = Plane(n.x, n.y, n.z, distance)
        assertEquals("normal", plane1.normal, plane2.normal)
        assertEquals("distance", plane1.distance, plane2.distance, 0.0)
        Assert.assertTrue("equals", plane1.equals(plane2))
    }

    @Test
    @Throws(Exception::class)
    fun testEquals_Inequality() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance1 = 6.0
        val distance2 = 7.0
        val plane1 = Plane(n.x, n.y, n.z, distance1)
        val plane2 = Plane(n.x, n.y, n.z, distance2)
        val plane3 = Plane(0.0, 1.0, 0.0, distance1)
        Assert.assertFalse("not equals", plane1.equals(plane2))
        Assert.assertFalse("not equals", plane1.equals(plane3))
    }

    @Test
    @Throws(Exception::class)
    fun testEquals_Null() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 10.0
        val plane1 = Plane(n.x, n.y, n.z, distance)
        val result: Boolean = plane1.equals(null)
        Assert.assertFalse("not equals", result)
    }

    @Test
    @Throws(Exception::class)
    fun testHashCode() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance1 = 6.0
        val distance2 = 7.0
        val plane1 = Plane(n.x, n.y, n.z, distance1)
        val plane2 = Plane(n.x, n.y, n.z, distance1)
        val plane3 = Plane(n.x, n.y, n.z, distance2)
        val hashCode1: Int = plane1.hashCode()
        val hashCode2: Int = plane2.hashCode()
        val hashCode3: Int = plane3.hashCode()
        Assert.assertEquals(hashCode1.toLong(), hashCode2.toLong())
        Assert.assertNotEquals(hashCode1.toLong(), hashCode3.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testToString() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 6.0
        val plane = Plane(n.x, n.y, n.z, distance)
        val string: String = plane.toString()
        Assert.assertTrue("normal x", string.contains(java.lang.Double.toString(plane.normal.x)))
        Assert.assertTrue("normal y", string.contains(java.lang.Double.toString(plane.normal.y)))
        Assert.assertTrue("normal z", string.contains(java.lang.Double.toString(plane.normal.z)))
        Assert.assertTrue("distance", string.contains(java.lang.Double.toString(plane.distance)))
    }

    @Test
    @Throws(Exception::class)
    fun testDistanceToPoint() {
        val normal: Vec3 = Vec3(0.0, 0.0, 1.0).normalize() // arbitrary orientation
        val distance = 10.0 // arbitrary distance
        val plane = Plane(normal.x, normal.y, normal.z, distance)
        // The plane's normal points towards the origin, so use the normal's
// reversed direction to create a point on the plane
        val point: Vec3 = Vec3(normal).negate().multiply(distance)
        val origin = Vec3(0.0, 0.0, 0.0)
        val distanceToOrigin: Double = plane.distanceToPoint(origin)
        val distanceToPoint: Double = plane.distanceToPoint(point)
        System.out.println(point)
        System.out.println(distanceToOrigin)
        System.out.println(distanceToPoint)
        Assert.assertEquals("distance to origin", distance, distanceToOrigin, 0.0)
        Assert.assertEquals("distance to point on plane", 0.0, distanceToPoint, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testSet() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 6.0
        val plane = Plane(0.0, 0.0, 1.0, 10.0)
        plane.set(n.x, n.y, n.z, distance)
        assertEquals("normal", n, plane.normal)
        assertEquals("distance", distance, plane.distance, 0.0)
    }

    @Test
    fun testSet_NotNormalizedValues() {
        val n = Vec3(3.0, 4.0, 5.0)
        val nExpected: Vec3 = Vec3(n).normalize()
        val distance = 6.0
        val distanceExpected: Double = distance / n.magnitude()
        val plane = Plane(0.0, 0.0, 1.0, 10.0)
        plane.set(n.x, n.y, n.z, distance)
        assertEquals("normal x", plane.normal.x, nExpected.x, 0.0)
        assertEquals("normal y", plane.normal.y, nExpected.y, 0.0)
        assertEquals("normal z", plane.normal.z, nExpected.z, 0.0)
        assertEquals("distance", plane.distance, distanceExpected, 0.0)
    }

    @Test
    fun testSet_ZeroValues() {
        val plane = Plane(0.0, 0.0, 1.0, 10.0)
        plane.set(0.0, 0.0, 0.0, 0.0)
        assertEquals("normal x", plane.normal.x, 0.0, 0.0)
        assertEquals("normal y", plane.normal.y, 0.0, 0.0)
        assertEquals("normal z", plane.normal.z, 0.0, 0.0)
        assertEquals("distance", plane.distance, 0.0, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testSet_Plane() {
        val n: Vec3 = Vec3(3.0, 4.0, 5.0).normalize()
        val distance = 6.0
        val plane1 = Plane(0.0, 0.0, 1.0, 10.0)
        val plane2 = Plane(n.x, n.y, n.z, distance)
        plane1.set(plane2)
        assertEquals("normal", n, plane1.normal)
        assertEquals("distance", distance, plane1.distance, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testTransformByMatrix() {
        val p = Plane(0.0, 0.0, -1.0, 10.0)
        // An arbitrary transformation matrix. Note that planes are transformed by the inverse transpose 4x4 matrix.
        val theta = 30.0
        val c = Math.cos(Math.toRadians(theta))
        val s = Math.sin(Math.toRadians(theta))
        val x = 0.0
        val y = 0.0
        val z = 3.0
        val m = Matrix4()
        m.multiplyByRotation(1.0, 0.0, 0.0, theta)
        m.multiplyByTranslation(x, y, z)
        m.invertOrthonormal().transpose()
        p.transformByMatrix(m)
        assertEquals("normal x", p.normal.x, 0.0, 0.0)
        assertEquals("normal y", p.normal.y, s, 0.0)
        assertEquals("normal z", p.normal.z, -c, 0.0)
        assertEquals("distance", p.distance, 13.0, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testDot() {
        val distance = Math.sqrt(3.0)
        System.out.println(distance)
        val n: Vec3 = Vec3(3.0, 3.0, 3.0).normalize()
        val u = Vec3(0.0, 0.0, 3.0)
        val plane = Plane(n.x, n.y, n.z, distance)
        val expected: Double = n.dot(u) - distance
        System.out.println(n.dot(u))
        val result: Double = plane.dot(u)
        System.out.println(expected)
        System.out.println(result)
        Assert.assertEquals("plane dot product", expected, result, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testIntersectsSegment() {
        val p = Plane(0.0, 0.0, -1.0, 0.0)
        var result: Boolean
        // These tests were adapted from WorldWindJava PlaneTest
        result = p.intersectsSegment(Vec3(), Vec3(0.0, 0.0, -1.0))
        Assert.assertTrue("Perpendicular, 0 at origin, should produce intersection at origin", result)
        result = p.intersectsSegment(Vec3(1.0, 0.0, 1.0), Vec3(1.0, 1.0, 0.0))
        Assert.assertTrue(
            "Line segment is in fact a point, located on the plane, should produce intersection at (1, 0, 0)",
            result
        )
        result = p.intersectsSegment(Vec3(0.0, 0.0, -1.0), Vec3(0.0, 0.0, -1.0))
        Assert.assertFalse(
            "Line segment is in fact a point not on the plane, should produce no intersection",
            result
        )
        result = p.intersectsSegment(Vec3(0.0, 0.0, 1.0), Vec3(0.0, 0.0, -1.0))
        Assert.assertTrue(
            "Perpendicular, integer end points off origin, should produce intersection at origin",
            result
        )
        result = p.intersectsSegment(Vec3(0.0, 0.0, 0.5), Vec3(0.0, 0.0, -0.5))
        Assert.assertTrue(
            "Perpendicular, non-integer end points off origin, should produce intersection at origin",
            result
        )
        result = p.intersectsSegment(Vec3(0.5, 0.5, 0.5), Vec3(-0.5, -0.5, -0.5))
        Assert.assertTrue(
            "Not perpendicular, non-integer end points off origin, should produce intersection at origin",
            result
        )
        result = p.intersectsSegment(Vec3(1.0, 0.0, 0.0), Vec3(2.0, 0.0, 0.0))
        Assert.assertTrue("Parallel, in plane, should produce intersection at origin", result)
        result = p.intersectsSegment(Vec3(1.0, 0.0, 1.0), Vec3(2.0, 0.0, 1.0))
        Assert.assertFalse("Parallel, integer end points off origin, should produce no intersection", result)
    }

    @Test
    @Throws(Exception::class)
    fun testOnSameSide() {
        val p = Plane(0.0, 0.0, -1.0, 0.0) // a plane at the origin
        var result: Int
        result = p.onSameSide(Vec3(1.0, 2.0, -1.0), Vec3(3.0, 4.0, -1.0))
        Assert.assertEquals(
            "Different points on positive side of the plane (with respect to normal vector)",
            1,
            result.toLong()
        )
        result = p.onSameSide(Vec3(1.0, 2.0, 1.0), Vec3(3.0, 4.0, 1.0))
        Assert.assertEquals(
            "Different points on negative side of the plane (with respect to normal vector)",
            -1,
            result.toLong()
        )
        result = p.onSameSide(Vec3(1.0, 2.0, 0.0), Vec3(3.0, 4.0, -1.0))
        Assert.assertEquals(
            "One point located on the plane, the other on the positive side the plane",
            0,
            result.toLong()
        )
        result = p.onSameSide(Vec3(1.0, 2.0, 0.0), Vec3(3.0, 4.0, 1.0))
        Assert.assertEquals(
            "One point located on the plane, the other on the negative side the plane",
            0,
            result.toLong()
        )
        result = p.onSameSide(Vec3(1.0, 0.0, 0.0), Vec3(1.0, 0.0, 0.0))
        Assert.assertEquals("Coincident points, located on the plane", 0, result.toLong())
        result = p.onSameSide(Vec3(1.0, 2.0, 0.0), Vec3(3.0, 4.0, 0.0))
        Assert.assertEquals("Different points located on the plane", 0, result.toLong())
        result = p.onSameSide(Vec3(1.0, 2.0, 1.0), Vec3(3.0, 4.0, -1.0))
        Assert.assertEquals("Different points on opposite sides of the plane", 0, result.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testClip() {
        val p = Plane(0.0, 0.0, -1.0, 0.0) // a plane at the origin
        val result: Array<Vec3>
        val a = Vec3(1.0, 2.0, 0.0)
        val b = Vec3(3.0, 4.0, 0.0)
        // If the segment is coincident with the plane, the input points are returned, in their input order.
        result = p.clip(a, b)!!
        Assert.assertNotNull("Segment coincident with plane", result)
        assertEquals("Coincident segment, start point unchanged", result[0], a)
        assertEquals("Coincident segment, end point unchanged", result[1], b)
    }

    @Test
    @Throws(Exception::class)
    fun testClip_NonIntersecting() {
        val p = Plane(0.0, 0.0, -1.0, 0.0) // a plane at the origin
        val result: Array<Vec3>
        val a = Vec3(1.0, 2.0, -1.0)
        val b = Vec3(3.0, 4.0, -1.0)
        // If the segment does not intersect the plane, null is returned.
        result = p.clip(a, b)!!
        Assert.assertNull("Non-intersecting points", result)
    }

    @Test
    @Throws(Exception::class)
    fun testClip_PositiveDirection() { // If the direction of the line formed by the two points is positive with respect to this plane's normal vector,
// the first point in the array will be the intersection point on the plane, and the second point will be the
// original segment end point.
        val p = Plane(0.0, 0.0, -1.0, 0.0) // a plane at the origin
        val result: Array<Vec3>
        val a = Vec3(1.0, 2.0, 1.0)
        val b = Vec3(3.0, 4.0, -1.0)
        val expected0 = Vec3(2.0, 3.0, 0.0)
        val expected1 = Vec3(b)
        result = p.clip(a, b)!!
        Assert.assertNotNull("Positive direction with respect normal, intersecting the plane", result)
        assertEquals(
            "Positive direction, the start point is the segment's original begin point",
            expected0,
            result[0]
        )
        assertEquals(
            "Positive direction, the end point is the segment's intersection with the plane",
            expected1,
            result[1]
        )
    }

    @Test
    @Throws(Exception::class)
    fun testClip_NegativeDirection() { // If the direction of the line is negative with respect to this plane's normal vector, the first point in the
// array will be the original segment's begin point, and the second point will be the intersection point on the
// plane.
        val p = Plane(0.0, 0.0, -1.0, 0.0) // a plane at the origin
        val result: Array<Vec3>
        val a = Vec3(1.0, 2.0, -1.0)
        val b = Vec3(3.0, 4.0, 1.0)
        val expected0 = Vec3(a)
        val expected1 = Vec3(2.0, 3.0, 0.0)
        result = p.clip(a, b)!!
        Assert.assertNotNull("Negative direction with respect normal, intersecting the plane", result)
        assertEquals(
            "Negative direction, the start point is the segment's original begin point",
            expected0,
            result[0]
        )
        assertEquals(
            "Negative direction, the end point is the segment's intersection with the plane",
            expected1,
            result[1]
        )
    }
    @Test
    fun testClip_NegativeasDirection() {
        val powerOfTwo = WWMath.isPowerOfTwo(4)
        System.out.println(powerOfTwo)

        val powerOfTwoCeiling = WWMath.powerOfTwoCeiling(17);
        System.out.println(powerOfTwoCeiling)
    }
}