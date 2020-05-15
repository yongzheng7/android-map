package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.*
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.Logger
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner


@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class)

class BasicTerrainTest {

    companion object{
        private const val OFFICIAL_WGS84_SEMI_MAJOR_AXIS = 6378137.0
        private const val OFFICIAL_WGS84_EC2 = 6.69437999014E-3
        private const val TOLERANCE = 0.0015 // Cartesian XYZ components must be within 1.5 millimeters

        private fun officialWgs84Ecef(
            latitude: Double,
            longitude: Double,
            altitude: Double
        ): Vec3 {
            val radLat = Math.toRadians(latitude)
            val radLon = Math.toRadians(longitude)
            val cosLat = Math.cos(radLat)
            val sinLat = Math.sin(radLat)
            val cosLon = Math.cos(radLon)
            val sinLon = Math.sin(radLon)
            val normal = OFFICIAL_WGS84_SEMI_MAJOR_AXIS / Math.sqrt(1.0 - OFFICIAL_WGS84_EC2 * sinLat * sinLat)
            val x = (normal + altitude) * cosLat * cosLon
            val y = (normal + altitude) * cosLat * sinLon
            val z = (normal * (1.0 - OFFICIAL_WGS84_EC2) + altitude) * sinLat
            return Vec3(x, y, z)
        }
        private fun worldWindEcef(officialEcef: Vec3): Vec3? {
            val x = officialEcef.y
            val y = officialEcef.z
            val z = officialEcef.x
            return Vec3(x, y, z)
        }

        private fun bilinearCentroid(sw: Vec3, se: Vec3, nw: Vec3, ne: Vec3): Vec3? {
            val px = sw.x * 0.25 + se.x * 0.25 + nw.x * 0.25 + ne.x * 0.25
            val py = sw.y * 0.25 + se.y * 0.25 + nw.y * 0.25 + ne.y * 0.25
            val pz = sw.z * 0.25 + se.z * 0.25 + nw.z * 0.25 + ne.z * 0.25
            return Vec3(px, py, pz)
        }
    }

    private lateinit var globe: Globe

    private lateinit var terrain: Terrain
    @Before
    fun setUp() { // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger::class.java)
        // Create the globe object used by the test
        globe = GlobeWgs84()
        // Create the terrain object used by the test
        terrain = BasicTerrain()
        (terrain as BasicTerrain).globe = (globe)
        // Add a terrain tile used to the mocked terrain
        val levelSet = LevelSet(Sector().setFullSphere(), 1.0, 1, 5, 5) // tiles with 5x5 vertices
        val tile = TerrainTile(Sector(0.0, 0.0, 1.0, 1.0), levelSet.firstLevel(), 90, 180)
        (terrain as BasicTerrain).addTile(tile)
        // Populate the terrain tile's geometry
        val tileOrigin = globe.geographicToCartesian(0.5, 0.5, 0.0, Vec3())
        val points = FloatArray(tile.level.tileWidth * tile.level.tileHeight * 3)
        globe.geographicToCartesianGrid(
            tile.sector, tile.level.tileWidth, tile.level.tileHeight,
            null, tileOrigin, points, 3, 0
        )
        tile.vertexOrigin = (tileOrigin)
        tile.vertexPoints = (points)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() { // Release the terrain object
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetGlobe() {
        val expected = globe
        val actual: Globe = terrain.globe!!
        assertEquals("globe", expected, actual)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetVerticalExaggeration() {
        val expected = 1.0
        val actual: Double = terrain.verticalExaggeration
        Assert.assertEquals("vertical exaggeration", expected, actual, 0.0)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetSector() {
        val expected = Sector(0.0, 0.0, 1.0, 1.0)
        val actual: Sector = terrain.sector
        assertEquals("sector", expected, actual)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_SouthwestCorner() {
        val lat = 0.0
        val lon = 0.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint Southwest corner x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Southwest corner y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Southwest corner z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Southwest corner return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_SoutheastCorner() {
        val lat = 0.0
        val lon = 1.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint Southeast corner x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Southeast corner y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Southeast corner z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Southeast corner return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_NorthwestCorner() {
        val lat = 1.0
        val lon = 0.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint Northwest corner x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Northwest corner y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Northwest corner z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Northwest corner return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_NortheastCorner() {
        val lat = 1.0
        val lon = 1.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint Northeast corner x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Northeast corner y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Northeast corner z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Northeast corner return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_SouthEdge() {
        val lat = 0.0
        val lon = 0.5
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint South edge x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint South edge y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint South edge z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint South edge return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_NorthEdge() {
        val lat = 1.0
        val lon = 0.5
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint North edge x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint North edge y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint North edge z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint North edge return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_WestEdge() {
        val lat = 0.5
        val lon = 0.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint West edge x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint West edge y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint West edge z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint West edge return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_EastEdge() {
        val lat = 0.5
        val lon = 1.0
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint East edge x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint East edge y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint East edge z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint East edge return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_SouthwestCell() {
        val sw = officialWgs84Ecef(0.0, 0.0, 0.0)
        val se = officialWgs84Ecef(0.0, 0.25, 0.0)
        val nw = officialWgs84Ecef(0.25, 0.0, 0.0)
        val ne = officialWgs84Ecef(0.25, 0.25, 0.0)
        val expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne)!!)
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(0.125, 0.125, 0.0, actual)
        assertEquals("surfacePoint Southwest cell x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Southwest cell y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Southwest cell z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Southwest cell return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_SoutheastCell() {
        val sw = officialWgs84Ecef(0.0, 0.75, 0.0)
        val se = officialWgs84Ecef(0.0, 1.0, 0.0)
        val nw = officialWgs84Ecef(0.25, 0.75, 0.0)
        val ne = officialWgs84Ecef(0.25, 1.0, 0.0)
        val expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne)!!)
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(0.125, 0.875, 0.0, actual)
        assertEquals("surfacePoint Southeast cell x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Southeast cell y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Southeast cell z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Southeast cell return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_NorthwestCell() {
        val sw = officialWgs84Ecef(0.75, 0.0, 0.0)
        val se = officialWgs84Ecef(0.75, 0.25, 0.0)
        val nw = officialWgs84Ecef(1.0, 0.0, 0.0)
        val ne = officialWgs84Ecef(1.0, 0.25, 0.0)
        val expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne)!!)
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(0.875, 0.125, 0.0, actual)
        assertEquals("surfacePoint Northwest cell x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Northwest cell y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Northwest cell z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Northwest cell return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_NortheastCell() {
        val sw = officialWgs84Ecef(0.75, 0.75, 0.0)
        val se = officialWgs84Ecef(0.75, 1.0, 0.0)
        val nw = officialWgs84Ecef(1.0, 0.75, 0.0)
        val ne = officialWgs84Ecef(1.0, 1.0, 0.0)
        val expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne)!!)
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(0.875, 0.875, 0.0, actual)
        assertEquals("surfacePoint Northeast cell x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint Northeast cell y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint Northeast cell z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint Northeast cell return", expectedReturn, actualReturn)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testSurfacePoint_Centroid() {
        val lat = 0.5
        val lon = 0.5
        val alt = 0.0
        val expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt))
        val expectedReturn = true
        val actual = Vec3()
        val actualReturn = terrain.surfacePoint(lat, lon, alt, actual)
        assertEquals("surfacePoint centroid x", expected!!.x, actual.x, TOLERANCE)
        assertEquals("surfacePoint centroid y", expected.y, actual.y, TOLERANCE)
        assertEquals("surfacePoint centroid z", expected.z, actual.z, TOLERANCE)
        Assert.assertEquals("surfacePoint centroid return", expectedReturn, actualReturn)
    }
}