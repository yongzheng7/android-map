package com.atom.wyz.worldwind

import com.atom.map.geom.Matrix3
import com.atom.map.util.Logger
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class) // We mock the Logger class to avoid its calls to android.util.log
class Matrix3Test {

    @Before
    fun setup() {
        PowerMockito.mockStatic(Logger::class.java)
    }


    /**
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testConstructor_Default() {
        val m1 = Matrix3()
        Assert.assertNotNull(m1)
        assertArrayEquals("identity matrix", Matrix3.identity, m1.m, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testConstructor_Doubles() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val elements =
            doubleArrayOf(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0) // identical
        Assert.assertNotNull(m1)
        assertArrayEquals("matrix components", elements, m1.m, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun testEquals() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0) // identical
        assertEquals("self", m1, m1)
        assertEquals("identical matrix", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testEquals_Inequality() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(
            11.0,
            12.0,
            13.0,
            21.0,
            22.0,
            23.0,
            31.0,
            32.0,
            0.0
        ) // last element is different
        assertNotEquals("different matrix", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testEquals_WithNull() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        assertNotEquals("null matrix", null, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testHashCode() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 0.0)
        val hashCode1: Int = m1.hashCode()
        val hashCode2: Int = m2.hashCode()
        Assert.assertNotEquals("hash codes", hashCode1.toLong(), hashCode2.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testToString() {
        val string: String =
            Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0).toString()
        Assert.assertTrue(
            "all elements in proper order",
            string.contains("[11.0, 12.0, 13.0], [21.0, 22.0, 23.0], [31.0, 32.0, 33.0]")
        )
    }


    @Test
    @Throws(Exception::class)
    fun testSet() {
        val m1 = Matrix3() // matrix under test
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m3: Matrix3 = m1.set(m2)
        assertEquals("set method argument", m2, m1)
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testSet_WithNull() {
        //PowerMockito.mockStatic(Logger.class);
        val m1 = Matrix3() // matrix under test
        //m1.set(null)
        Assert.fail("Expected an IllegalArgumentException to be thrown.")
    }

    @Test
    @Throws(Exception::class)
    fun testSet_Doubles() {
        val m11 = 11.0
        val m12 = 12.0
        val m13 = 13.0
        val m21 = 21.0
        val m22 = 22.0
        val m23 = 23.0
        val m31 = 31.0
        val m32 = 32.0
        val m33 = 33.0
        val m1 = Matrix3() // matrix under test
        val m2: Matrix3 = m1.set(m11, m12, m13, m21, m22, m23, m31, m32, m33)
        assertEquals("m11", m11, m1.m.get(0), 0.0)
        assertEquals("m12", m12, m1.m.get(1), 0.0)
        assertEquals("m13", m13, m1.m.get(2), 0.0)
        assertEquals("m21", m21, m1.m.get(3), 0.0)
        assertEquals("m22", m22, m1.m.get(4), 0.0)
        assertEquals("m23", m23, m1.m.get(5), 0.0)
        assertEquals("m31", m31, m1.m.get(6), 0.0)
        assertEquals("m32", m32, m1.m.get(7), 0.0)
        assertEquals("m33", m33, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetTranslation() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0) // identical
        val dx = 5.0
        val dy = 7.0
        val m3: Matrix3 = m1.setTranslation(dx, dy)

        // Test for translation matrix form
        // [m11  m12  dx ]
        // [m21  m22  dy ]
        // [m31  m32  m33]
        assertEquals("m11", m2.m.get(0), m1.m.get(0), 0.0)
        assertEquals("m12", m2.m.get(1), m1.m.get(1), 0.0)
        assertEquals("m13", dx, m1.m.get(2), 0.0)
        assertEquals("m21", m2.m.get(3), m1.m.get(3), 0.0)
        assertEquals("m22", m2.m.get(4), m1.m.get(4), 0.0)
        assertEquals("m23", dy, m1.m.get(5), 0.0)
        assertEquals("m31", m2.m.get(6), m1.m.get(6), 0.0)
        assertEquals("m32", m2.m.get(7), m1.m.get(7), 0.0)
        assertEquals("m33", m2.m.get(8), m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetRotation() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0) // identical
        val theta = 30.0 // rotation angle degrees
        val c = Math.cos(Math.toRadians(theta))
        val s = Math.sin(Math.toRadians(theta))
        val m3: Matrix3 = m1.setRotation(theta)

        // Test for Euler rotation matrix
        // [cos(a) -sin(a)  m13]
        // [sin(a)  cos(a)  m23]
        // [  m31    m32    m33]
        assertEquals("m11", c, m1.m.get(0), 0.0)
        assertEquals("m12", -s, m1.m.get(1), 0.0)
        assertEquals("m13", m2.m.get(2), m1.m.get(2), 0.0)
        assertEquals("m21", s, m1.m.get(3), 0.0)
        assertEquals("m22", c, m1.m.get(4), 0.0)
        assertEquals("m23", m2.m.get(5), m1.m.get(5), 0.0)
        assertEquals("m31", m2.m.get(6), m1.m.get(6), 0.0)
        assertEquals("m32", m2.m.get(7), m1.m.get(7), 0.0)
        assertEquals("m33", m2.m.get(8), m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetScale() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0) // identical
        val sx = 5.0
        val sy = 7.0
        val m3: Matrix3 = m1.setScale(sx, sy)

        // Test for scaling matrix form
        // [sx   m12  m13]
        // [m21  sy   m23]
        // [m31  m32  m33]
        assertEquals("m11", sx, m1.m.get(0), 0.0)
        assertEquals("m12", m2.m.get(1), m1.m.get(1), 0.0)
        assertEquals("m13", m2.m.get(2), m1.m.get(2), 0.0)
        assertEquals("m21", m2.m.get(3), m1.m.get(3), 0.0)
        assertEquals("m22", sy, m1.m.get(4), 0.0)
        assertEquals("m23", m2.m.get(5), m1.m.get(5), 0.0)
        assertEquals("m31", m2.m.get(6), m1.m.get(6), 0.0)
        assertEquals("m32", m2.m.get(7), m1.m.get(7), 0.0)
        assertEquals("m33", m2.m.get(8), m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToIdentity() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2: Matrix3 = m1.setToIdentity()
        assertArrayEquals("identity matrix", Matrix3.identity, m1.m, 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToTranslation() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val dx = 5.0
        val dy = 7.0
        val m2: Matrix3 = m1.setToTranslation(dx, dy)

        // Test for translation matrix form
        // [1 0 x]
        // [0 1 y]
        // [0 0 1]
        assertEquals("m11", 1.0, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", dx, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", 1.0, m1.m.get(4), 0.0)
        assertEquals("m23", dy, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToRotation() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val theta = 30.0 // rotation angle degrees
        val c = Math.cos(Math.toRadians(theta))
        val s = Math.sin(Math.toRadians(theta))
        val m2: Matrix3 = m1.setToRotation(theta)

        // Test for Euler (pronounced "oiler") rotation matrix
        // [cos(a) -sin(a)  0]
        // [sin(a)  cos(a)  0]
        // [  0       0     1]
        assertEquals("m11", c, m1.m.get(0), 0.0)
        assertEquals("m12", -s, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", s, m1.m.get(3), 0.0)
        assertEquals("m22", c, m1.m.get(4), 0.0)
        assertEquals("m23", 0.0, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToScale() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val sx = 5.0
        val sy = 7.0
        val m2: Matrix3 = m1.setToScale(sx, sy)

        // Test for scaling matrix form
        // [sx  0  0]
        // [0  sy  0]
        // [0   0  1]
        assertEquals("m11", sx, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", sy, m1.m.get(4), 0.0)
        assertEquals("m23", 0.0, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToVerticalFlip() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val m2: Matrix3 = m1.setToVerticalFlip()

        // Sets this matrix to one that reflects about the x-axis and translates the y-axis origin.
        // [1  0  0]
        // [0 -1  1] <-- *
        // [0  0  1]
        assertEquals("m11", 1.0, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", -1.0, m1.m.get(4), 0.0) // *
        assertEquals("m23", 1.0, m1.m.get(5), 0.0) // *
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testSetToMultiply() {
        val m1 = Matrix3()
        val a = Matrix3(
            11.0, 12.0, 13.0,
            21.0, 22.0, 23.0,
            31.0, 32.0, 33.0
        )
        val b = Matrix3(
            11.0, 12.0, 13.0,
            21.0, 22.0, 23.0,
            31.0, 32.0, 33.0
        )
        val m2: Matrix3 = m1.setToMultiply(a, b)

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals(
            "m11",
            a.m.get(0) * b.m.get(0) + a.m.get(1) * b.m.get(3) + a.m.get(2) * b.m.get(6),
            m1.m.get(0),
            0.0
        )
        assertEquals(
            "m21",
            a.m.get(3) * b.m.get(0) + a.m.get(4) * b.m.get(3) + a.m.get(5) * b.m.get(6),
            m1.m.get(3),
            0.0
        )
        assertEquals(
            "m31",
            a.m.get(6) * b.m.get(0) + a.m.get(7) * b.m.get(3) + a.m.get(8) * b.m.get(6),
            m1.m.get(6),
            0.0
        )
        // 2nd Column:
        assertEquals(
            "m12",
            a.m.get(0) * b.m.get(1) + a.m.get(1) * b.m.get(4) + a.m.get(2) * b.m.get(7),
            m1.m.get(1),
            0.0
        )
        assertEquals(
            "m22",
            a.m.get(3) * b.m.get(1) + a.m.get(4) * b.m.get(4) + a.m.get(5) * b.m.get(7),
            m1.m.get(4),
            0.0
        )
        assertEquals(
            "m23",
            a.m.get(6) * b.m.get(1) + a.m.get(7) * b.m.get(4) + a.m.get(8) * b.m.get(7),
            m1.m.get(7),
            0.0
        )
        // 3rd Column:
        assertEquals(
            "m13",
            a.m.get(0) * b.m.get(2) + a.m.get(1) * b.m.get(5) + a.m.get(2) * b.m.get(8),
            m1.m.get(2),
            0.0
        )
        assertEquals(
            "m32",
            a.m.get(3) * b.m.get(2) + a.m.get(4) * b.m.get(5) + a.m.get(5) * b.m.get(8),
            m1.m.get(5),
            0.0
        )
        assertEquals(
            "m33",
            a.m.get(6) * b.m.get(2) + a.m.get(7) * b.m.get(5) + a.m.get(8) * b.m.get(8),
            m1.m.get(8),
            0.0
        )
        //
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByTranslation() {
        val m1 = Matrix3() // identity matrix
        val dx = 2.0
        val dy = 3.0
        val m2: Matrix3 = m1.multiplyByTranslation(dx, dy)

        // Test for translation matrix form
        // [1 0 x]
        // [0 1 y]
        // [0 0 1]
        assertEquals("m11", 1.0, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", dx, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", 1.0, m1.m.get(4), 0.0)
        assertEquals("m23", dy, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByRotation() {
        val m1 = Matrix3() // identity matrix
        val theta = 30.0 // rotation angle degrees
        val c = Math.cos(Math.toRadians(theta))
        val s = Math.sin(Math.toRadians(theta))
        val m2: Matrix3 = m1.multiplyByRotation(theta)

        // Test for Euler rotation matrix
        // [cos(a) -sin(a)  0]
        // [sin(a)  cos(a)  0]
        // [  0       0     1]
        assertEquals("m11", c, m1.m.get(0), 0.0)
        assertEquals("m12", -s, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", s, m1.m.get(3), 0.0)
        assertEquals("m22", c, m1.m.get(4), 0.0)
        assertEquals("m23", 0.0, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByScale() {
        val m1 = Matrix3()
        val sx = 5.0
        val sy = 7.0
        val m2: Matrix3 = m1.multiplyByScale(sx, sy)

        // Test for scaling matrix form
        // [sx  0  0]
        // [0  sy  0]
        // [0   0  1]
        assertEquals("m11", sx, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", sy, m1.m.get(4), 0.0)
        assertEquals("m23", 0.0, m1.m.get(5), 0.0)
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByVerticalFlip() {
        val m1 = Matrix3() // identity matrix
        val m2: Matrix3 = m1.multiplyByVerticalFlip()

        // Sets this matrix to one that reflects about the x-axis and translates the y-axis origin.
        // [1  0  0]
        // [0 -1  1] <-- *
        // [0  0  1]
        assertEquals("m11", 1.0, m1.m.get(0), 0.0)
        assertEquals("m12", 0.0, m1.m.get(1), 0.0)
        assertEquals("m13", 0.0, m1.m.get(2), 0.0)
        assertEquals("m21", 0.0, m1.m.get(3), 0.0)
        assertEquals("m22", -1.0, m1.m.get(4), 0.0) // *
        assertEquals("m23", 1.0, m1.m.get(5), 0.0) // *
        assertEquals("m31", 0.0, m1.m.get(6), 0.0)
        assertEquals("m32", 0.0, m1.m.get(7), 0.0)
        assertEquals("m33", 1.0, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByMatrix() {
        val m1 = Matrix3( // matrix under test
            11.0, 12.0, 13.0,
            21.0, 22.0, 23.0,
            31.0, 32.0, 33.0
        )
        val m2 = Matrix3( // multiplier
            11.0, 12.0, 13.0,
            21.0, 22.0, 23.0,
            31.0, 32.0, 33.0
        )
        val copy = Matrix3()
        copy.set(m1) // copy of m1 before its mutated
        val m3: Matrix3 = m1.multiplyByMatrix(m2)

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals(
            "m11",
            copy.m.get(0) * m2.m.get(0) + copy.m.get(1) * m2.m.get(3) + copy.m.get(2) * m2.m.get(
                6
            ),
            m1.m.get(0),
            0.0
        )
        assertEquals(
            "m21",
            copy.m.get(3) * m2.m.get(0) + copy.m.get(4) * m2.m.get(3) + copy.m.get(5) * m2.m.get(
                6
            ),
            m1.m.get(3),
            0.0
        )
        assertEquals(
            "m31",
            copy.m.get(6) * m2.m.get(0) + copy.m.get(7) * m2.m.get(3) + copy.m.get(8) * m2.m.get(
                6
            ),
            m1.m.get(6),
            0.0
        )
        // 2nd Column:
        assertEquals(
            "m12",
            copy.m.get(0) * m2.m.get(1) + copy.m.get(1) * m2.m.get(4) + copy.m.get(2) * m2.m.get(
                7
            ),
            m1.m.get(1),
            0.0
        )
        assertEquals(
            "m22",
            copy.m.get(3) * m2.m.get(1) + copy.m.get(4) * m2.m.get(4) + copy.m.get(5) * m2.m.get(
                7
            ),
            m1.m.get(4),
            0.0
        )
        assertEquals(
            "m23",
            copy.m.get(6) * m2.m.get(1) + copy.m.get(7) * m2.m.get(4) + copy.m.get(8) * m2.m.get(
                7
            ),
            m1.m.get(7),
            0.0
        )
        // 3rd Column:
        assertEquals(
            "m13",
            copy.m.get(0) * m2.m.get(2) + copy.m.get(1) * m2.m.get(5) + copy.m.get(2) * m2.m.get(
                8
            ),
            m1.m.get(2),
            0.0
        )
        assertEquals(
            "m32",
            copy.m.get(3) * m2.m.get(2) + copy.m.get(4) * m2.m.get(5) + copy.m.get(5) * m2.m.get(
                8
            ),
            m1.m.get(5),
            0.0
        )
        assertEquals(
            "m33",
            copy.m.get(6) * m2.m.get(2) + copy.m.get(7) * m2.m.get(5) + copy.m.get(8) * m2.m.get(
                8
            ),
            m1.m.get(8),
            0.0
        )
        //
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testMultiplyByMatrix_Doubles() {
        // multipliers
        val m11 = 11.0
        val m12 = 12.0
        val m13 = 13.0
        val m21 = 21.0
        val m22 = 22.0
        val m23 = 23.0
        val m31 = 31.0
        val m32 = 32.0
        val m33 = 33.0
        // matrix under test
        val m1 = Matrix3( // matrix under test
            11.0, 12.0, 13.0,
            21.0, 22.0, 23.0,
            31.0, 32.0, 33.0
        )
        val copy = Matrix3()
        copy.set(m1) // copy of m1 before its mutated
        val m3: Matrix3 = m1.multiplyByMatrix(
            m11, m12, m13,
            m21, m22, m23,
            m31, m32, m33
        )

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals(
            "m11",
            copy.m.get(0) * m11 + copy.m.get(1) * m21 + copy.m.get(2) * m31,
            m1.m.get(0),
            0.0
        )
        assertEquals(
            "m21",
            copy.m.get(3) * m11 + copy.m.get(4) * m21 + copy.m.get(5) * m31,
            m1.m.get(3),
            0.0
        )
        assertEquals(
            "m31",
            copy.m.get(6) * m11 + copy.m.get(7) * m21 + copy.m.get(8) * m31,
            m1.m.get(6),
            0.0
        )
        // 2nd Column:
        assertEquals(
            "m12",
            copy.m.get(0) * m12 + copy.m.get(1) * m22 + copy.m.get(2) * m32,
            m1.m.get(1),
            0.0
        )
        assertEquals(
            "m22",
            copy.m.get(3) * m12 + copy.m.get(4) * m22 + copy.m.get(5) * m32,
            m1.m.get(4),
            0.0
        )
        assertEquals(
            "m23",
            copy.m.get(6) * m12 + copy.m.get(7) * m22 + copy.m.get(8) * m32,
            m1.m.get(7),
            0.0
        )
        // 3rd Column:
        assertEquals(
            "m13",
            copy.m.get(0) * m13 + copy.m.get(1) * m23 + copy.m.get(2) * m33,
            m1.m.get(2),
            0.0
        )
        assertEquals(
            "m32",
            copy.m.get(3) * m13 + copy.m.get(4) * m23 + copy.m.get(5) * m33,
            m1.m.get(5),
            0.0
        )
        assertEquals(
            "m33",
            copy.m.get(6) * m13 + copy.m.get(7) * m23 + copy.m.get(8) * m33,
            m1.m.get(8),
            0.0
        )
        //
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testTranspose() {
        val m11 = 11.0
        val m12 = 12.0
        val m13 = 13.0
        val m21 = 21.0
        val m22 = 22.0
        val m23 = 23.0
        val m31 = 31.0
        val m32 = 32.0
        val m33 = 33.0
        val m1 =
            Matrix3(m11, m12, m13, m21, m22, m23, m31, m32, m33) // matrix to be tested/transposed
        val m2: Matrix3 = m1.transpose()
        assertEquals("m11", m11, m1.m.get(0), 0.0)
        assertEquals("m12", m21, m1.m.get(1), 0.0)
        assertEquals("m13", m31, m1.m.get(2), 0.0)
        assertEquals("m21", m12, m1.m.get(3), 0.0)
        assertEquals("m22", m22, m1.m.get(4), 0.0)
        assertEquals("m23", m32, m1.m.get(5), 0.0)
        assertEquals("m31", m13, m1.m.get(6), 0.0)
        assertEquals("m32", m23, m1.m.get(7), 0.0)
        assertEquals("m33", m33, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testTransposeMatrix() {
        val m11 = 11.0
        val m12 = 12.0
        val m13 = 13.0
        val m21 = 21.0
        val m22 = 22.0
        val m23 = 23.0
        val m31 = 31.0
        val m32 = 32.0
        val m33 = 33.0
        val m1 = Matrix3() // matrix under test
        val m2 =
            Matrix3(m11, m12, m13, m21, m22, m23, m31, m32, m33) // matrix to be transposed
        val m3: Matrix3 = m1.transposeMatrix(m2)
        assertEquals("m11", m11, m1.m.get(0), 0.0)
        assertEquals("m12", m21, m1.m.get(1), 0.0)
        assertEquals("m13", m31, m1.m.get(2), 0.0)
        assertEquals("m21", m12, m1.m.get(3), 0.0)
        assertEquals("m22", m22, m1.m.get(4), 0.0)
        assertEquals("m23", m32, m1.m.get(5), 0.0)
        assertEquals("m31", m13, m1.m.get(6), 0.0)
        assertEquals("m32", m23, m1.m.get(7), 0.0)
        assertEquals("m33", m33, m1.m.get(8), 0.0)
        Assert.assertSame("fluent api result", m3, m1)
    }

    @Ignore("invert is not implemented at time of test")
    @Test
    @Throws(Exception::class)
    fun testInvert() {
        val m1 = Matrix3( // matrix to be tested/inverted
            -4.0, -3.0, 3.0,
            0.0, 2.0, -2.0,
            1.0, 4.0, -1.0
        )
        val mOriginal = Matrix3(m1)
        val m2: Matrix3 = m1.invert()
        val mIdentity: Matrix3 = Matrix3(m1).multiplyByMatrix(mOriginal)
        assertArrayEquals("identity matrix array", Matrix3.identity, mIdentity.m, 0.0)
        Assert.assertSame("fluent api result", m2, m1)
    }

    @Ignore("invertMatrix was not implemented at time of test")
    @Test
    @Throws(
        Exception::class
    )
    fun testInvertMatrix() {
        val m1 = Matrix3()
        val m2 = Matrix3( // matrix to be inverted
            -4.0, -3.0, 3.0,
            0.0, 2.0, -2.0,
            1.0, 4.0, -1.0
        )
        val det = computeDeterminant(m2)
        System.out.println(m2)
        println("Determinate: $det")
        val mInv: Matrix3 = m1.invertMatrix(m2)
        val mIdentity: Matrix3 = mInv.multiplyByMatrix(m2)
        assertArrayEquals("identity matrix array", Matrix3.identity, mIdentity.m, 0.0)
        Assert.assertSame("fluent api result", mInv, m1)
    }

    @Test
    @Throws(Exception::class)
    fun testTransposeToArray() {
        val m1 = Matrix3(11.0, 12.0, 13.0, 21.0, 22.0, 23.0, 31.0, 32.0, 33.0)
        val result: FloatArray = m1.transposeToArray(FloatArray(9), 0)
        val expected: DoubleArray = m1.transpose().m
        for (i in 0..8) {
            Assert.assertEquals(
                Integer.toString(i),
                expected[i],
                result[i].toDouble(),
                0.0
            )
        }
    }

    //////////////////////
    // Helper methods
    //////////////////////

    //////////////////////
    // Helper methods
    //////////////////////
    private fun computeDeterminant(matrix: Matrix3): Double {
        // |m11  m12  m13|
        // |m21  m22  m23| = m11(m22*m33 - m23*m32) + m12(m23*m31 - m21*m33) + m13(m21*m32 - m22*m31)
        // |m31  m32  m33|
        val m: DoubleArray = matrix.m
        return (m[0] * (m[4] * m[8] - m[5] * m[7]) //m11(m22*m33 - m23*m32)
                + m[1] * (m[5] * m[6] - m[3] * m[8]) //m12(m23*m31 - m21*m33)
                + m[2] * (m[3] * m[7] - m[4] * m[6]))
    }

    private fun prettyPrint(m: Matrix3) {
        System.out.println(
            "[ " + m.m.get(0).toString() + "  " + m.m.get(1).toString() + "  " + m.m.get(2)
                .toString() + " ]"
        )
        System.out.println(
            "[ " + m.m.get(3).toString() + "  " + m.m.get(4).toString() + "  " + m.m.get(5)
                .toString() + " ]"
        )
        System.out.println(
            "[ " + m.m.get(6).toString() + "  " + m.m.get(7).toString() + "  " + m.m.get(8)
                .toString() + " ]"
        )
    }
}