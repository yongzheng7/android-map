package com.atom.wyz.worldwind

import com.atom.map.WorldWind
import com.atom.map.geom.Matrix4
import com.atom.map.geom.Position
import com.atom.map.geom.Vec3
import com.atom.map.globe.Globe
import com.atom.map.globe.ProjectionWgs84
import com.atom.map.util.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*


@RunWith(PowerMockRunner::class) // Support for mocking static methods
// Support for mocking static methods
// Support for mocking static methods
@PrepareForTest(Logger::class) // We mock the Logger class to avoid its calls to android.util.log

class ProjectionWgs84Test {

    private var globe: Globe? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger::class.java)

        // Create a globe with a WGS84 definition.
        globe = Globe(WorldWind.WGS84_ELLIPSOID, ProjectionWgs84())
    }
    companion object{
        fun fromEcef(xEcef: Double, yEcef: Double, zEcef: Double): Vec3 {
            return Vec3(yEcef, zEcef, xEcef)
        }
        fun getStations(): Map<String, Array<Any>> {
            val stations: MutableMap<String, Array<Any>> = HashMap()
//            stations["Colorado Springs"] = arrayOf(
//                Position.fromDegrees(38.80305456, 255.47540844, 1911.755),
//                fromEcef(-1248.597295e3, -4819.433239e3, 3976.500175e3)
//            )
//            stations["Ascension"] = arrayOf(
//                Position.fromDegrees(-7.95132970, 345.58786950, 106.558),
//                fromEcef(6118.524122e3, -1572.350853e3, -876.463990e3)
//            )
//            stations["Diego Garcia"] = arrayOf(
//                Position.fromDegrees(-7.26984347, 72.37092177, -64.063),
//                fromEcef(1916.197142e3, 6029.999007e3, -801.737366e3)
//            )
//            stations["Kwajalein"] = arrayOf(
//                Position.fromDegrees(8.72250074, 167.73052625, 39.927),
//                fromEcef(-6160.884370e3, 1339.851965e3, 960.843071e3)
//            )
//            stations["Hawaii"] = arrayOf(
//                Position.fromDegrees(21.56149086, 201.76066922, 426.077),
//                fromEcef(-5511.980484e3, -2200.247093e3, 2329.480952e3)
//            )
//            stations["Cape Canaveral"] = arrayOf(
//                Position.fromDegrees(28.48373800, 279.42769549, -24.005),
//                fromEcef(918.988120e3, -5534.552966e3, 3023.721377e3)
//            )
            stations["wyz 0 "] = arrayOf(
                Position.fromDegrees(0.0, 0.0, 0.0),
                fromEcef(918.988120e3, -5534.552966e3, 3023.721377e3)
            )
            stations["wyz 10 "] = arrayOf(
                Position.fromDegrees(0.0, -90.0, 0.0),
                fromEcef(918.988120e3, -5534.552966e3, 3023.721377e3)
            )
            stations["wyz 20 "] = arrayOf(
                Position.fromDegrees(45.0, 45.0, 0.0),
                fromEcef(918.988120e3, -5534.552966e3, 3023.721377e3)
            )
            return stations
        }
    }



    @Test
    @Throws(java.lang.Exception::class)
    fun testGeographicToCartesian() {
        val wgs84 = ProjectionWgs84()
        val p: Position = Position.fromDegrees(90.0, 0.0, 0.0)
        var a = Vec3(0.0 ,0.0 ,0.0)
        var b = Vec3(0.0 ,0.0 ,0.0)
        val result1 = Matrix4()
        wgs84.geographicToCartesianTransform(globe!!, p.latitude, p.longitude, p.altitude ,  result1)
        println("1 ---$result1")
        a.multiplyByMatrix(result1)
        println("1 ---$a")
        result1.invertOrthonormal()
        b.multiplyByMatrix(result1)
        println("2 ---$result1")
        println("2 ---$b")


    }
}