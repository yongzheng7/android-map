package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.ogc.WmsGetMapUrlFactory
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.Logger
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner


@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class)
class WmsGetMapUrlFactoryTest {

    companion object{
        private fun checkQueryDelimiter(url: String) {
            val queryDelimiter = '?'
            val index = url.indexOf(queryDelimiter)
            Assert.assertTrue("added delimiter", index > 0)
            val lastIndex = url.lastIndexOf(queryDelimiter)
            Assert.assertTrue("one delimiter", index == lastIndex)
            Assert.assertTrue("no following parameters", url.length - 1 > index)
            System.out.println(url[index + 1])
            Assert.assertFalse("ampersand trailing", url[index + 1] == '&')
        }
    }
    @Before
    @Throws(Exception::class)
    fun setUp() {
        PowerMockito.mockStatic(Logger::class.java)
    }

    @Test
    fun testUrlForTile_QueryDelimiterPositioning() { // Values used for the Blue Marble
        val serviceAddress = "http://worldwind25.arc.nasa.gov/wms"
        val wmsVersion = "1.3.0"
        val layerNames = "BlueMarble-200405"
        val imageFormat = "image/png"
        // Mocking of method object parameters - notional values
        val tileHeight = 5
        val tileWidth = 4
        val minLat = 20.0
        val maxLat = 30.0
        val minLon = -90.0
        val maxLon = -80.0
        val levelSet = LevelSet()
        val tileLevel = Level(levelSet, 0, 0.1)
        val sector:Sector  = Sector(minLat , minLon , maxLat-minLat , maxLon-minLon)

        val tile = Tile(sector, tileLevel, tileHeight, tileWidth)
        var wmsUrlFactory = WmsGetMapUrlFactory(serviceAddress, wmsVersion, layerNames, null)
        var url: String = wmsUrlFactory.urlForTile(tile, imageFormat)
        checkQueryDelimiter(url)
        wmsUrlFactory = WmsGetMapUrlFactory("$serviceAddress?", wmsVersion, layerNames, null)
        url = wmsUrlFactory.urlForTile(tile, imageFormat)
//        checkQueryDelimiter(url)
        wmsUrlFactory = WmsGetMapUrlFactory("$serviceAddress?NOTIONAL=YES", wmsVersion, layerNames, null)
        url = wmsUrlFactory.urlForTile(tile, imageFormat)
        checkQueryDelimiter(url)
    }


}