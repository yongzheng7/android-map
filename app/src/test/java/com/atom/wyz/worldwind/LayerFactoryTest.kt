package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.layer.LayerFactory
import com.atom.wyz.worldwind.ogc.wms.WmsCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayerCapabilities
import com.atom.wyz.worldwind.ogc.wms.WmsLayerConfig
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*

@RunWith(PowerMockRunner::class)
@PrepareForTest(Logger::class) // We mock the Logger class to avoid its calls to android.util.log
class LayerFactoryTest {
    companion object{
        const val DEFAULT_LAYER_NAME = "LayerName"

        const val DEFAULT_REQUEST_URL = "http://example.com"

        val DEFAULT_REFERENCE_SYSTEMS: Set<String> =
            HashSet(
                Arrays.asList(
                    "CRS:84",
                    "EPSG:4326"
                )
            )

        val DEFAULT_IMAGE_FORMATS: MutableSet<String> =
            HashSet(
                Arrays.asList(
                    "image/png",
                    "image/jpeg"
                )
            )

        const val DEFAULT_MIN_SCALE_DENOMINATOR = 1e-6

        const val DEFAULT_VERSION = "1.3.0"

        fun getBoilerPlateWmsCapabilities(): WmsCapabilities {
            val mockedLayerCapabilities: WmsLayerCapabilities =
                PowerMockito.mock<WmsLayerCapabilities>(
                    WmsLayerCapabilities::class.java
                )
            PowerMockito.`when`(mockedLayerCapabilities.getName())
                .thenReturn(DEFAULT_LAYER_NAME)
            PowerMockito.`when`(mockedLayerCapabilities.getReferenceSystem())
                .thenReturn(DEFAULT_REFERENCE_SYSTEMS)
            PowerMockito.`when`(mockedLayerCapabilities.getMinScaleDenominator())
                .thenReturn(DEFAULT_MIN_SCALE_DENOMINATOR)
            PowerMockito.`when`(mockedLayerCapabilities.getGeographicBoundingBox())
                .thenReturn(Sector().setFullSphere())
            val mockedCapabilities: WmsCapabilities =
                PowerMockito.mock<WmsCapabilities>(WmsCapabilities::class.java)
            PowerMockito.`when`(mockedCapabilities.getVersion())
                .thenReturn(DEFAULT_VERSION)
            PowerMockito.`when`(
                mockedCapabilities.getRequestURL(
                    Matchers.anyString(),
                    Matchers.anyString()
                )
            ).thenReturn(DEFAULT_REQUEST_URL)
            PowerMockito.`when`(mockedCapabilities.getLayerByName(Matchers.anyString()))
                .thenReturn(mockedLayerCapabilities)
            PowerMockito.`when`(mockedCapabilities.getImageFormats())
                .thenReturn(DEFAULT_IMAGE_FORMATS)
            return mockedCapabilities
        }
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger::class.java)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_Nominal() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities? = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)
        val layerFactory = LayerFactory()
        val wmsLayerConfig: WmsLayerConfig =
            layerFactory.getLayerConfigFromWmsCapabilities(wmsCapabilities, wmsLayerCapabilities!!)!!
        assertEquals("Version", DEFAULT_VERSION, wmsLayerConfig.wmsVersion)
        assertEquals("Layer Name", DEFAULT_LAYER_NAME, wmsLayerConfig.layerNames)
        assertEquals(
            "Request URL",
            DEFAULT_REQUEST_URL,
            wmsLayerConfig.serviceAddress
        )
        assertEquals("Reference Systems", "EPSG:4326", wmsLayerConfig.coordinateSystem)
        assertEquals("Image Format", "image/png", wmsLayerConfig.imageFormat)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_InvalidVersion() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        // Invalid WMS version
        PowerMockito.`when`(wmsCapabilities.getVersion()).thenReturn("1.2.1")
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val layerFactory = LayerFactory()
        try {
            val wmsLayerConfig: WmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(
                wmsCapabilities,
                wmsLayerCapabilities
            )!!
            Assert.fail("Invalid Version")
        } catch (e: RuntimeException) {
            Assert.assertNotNull("Invalid Version", e)
        }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_InvalidRequestUrl() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        // Invalid WMS version
        PowerMockito.`when`(
            wmsCapabilities.getRequestURL(
                Matchers.anyString(),
                Matchers.anyString()
            )
        ).thenReturn(null)
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val layerFactory = LayerFactory()
        try {
            val wmsLayerConfig: WmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(
                wmsCapabilities,
                wmsLayerCapabilities
            )!!
            Assert.fail("Invalid Request URL")
        } catch (e: RuntimeException) {
            Assert.assertNotNull("Invalid Request URL", e)
        }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_OtherCoordinateSystem() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val modifiedReferenceSystems: Set<String> =
            HashSet(Arrays.asList("CRS:84"))
        PowerMockito.`when`(wmsLayerCapabilities.getReferenceSystem())
            .thenReturn(modifiedReferenceSystems)
        val layerFactory = LayerFactory()
        val wmsLayerConfig: WmsLayerConfig =
            layerFactory.getLayerConfigFromWmsCapabilities(wmsCapabilities, wmsLayerCapabilities)!!
        assertEquals("Alternate Coordinate System", "CRS:84", wmsLayerConfig.coordinateSystem)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_InvalidCoordinateSystem() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val modifiedReferenceSystems: Set<String> =
            HashSet(Arrays.asList("EPSG:1234"))
        PowerMockito.`when`(wmsLayerCapabilities.getReferenceSystem())
            .thenReturn(modifiedReferenceSystems)
        val layerFactory = LayerFactory()
        try {
            val wmsLayerConfig: WmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(
                wmsCapabilities,
                wmsLayerCapabilities
            )!!
            Assert.fail("Invalid Coordinate System")
        } catch (e: RuntimeException) {
            Assert.assertNotNull("Invalid Coordinate System", e)
        }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_OtherImageFormat() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val modifiedImageFormats: MutableSet<String> =
            HashSet(
                Arrays.asList(
                    "image/dds",
                    "image/jpg"
                )
            )
        PowerMockito.`when`(wmsCapabilities.getImageFormats()).thenReturn(modifiedImageFormats)!!
        val layerFactory = LayerFactory()
        val wmsLayerConfig: WmsLayerConfig =
            layerFactory.getLayerConfigFromWmsCapabilities(wmsCapabilities, wmsLayerCapabilities)!!
        assertEquals("Alternate Image Format", "image/jpg", wmsLayerConfig.imageFormat)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLayerConfigFromWmsCapabilities_InvalidImageFormat() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val modifiedImageFormats: MutableSet<String> =
            HashSet(
                Arrays.asList(
                    "image/dds",
                    "image/never"
                )
            )
        PowerMockito.`when`(wmsCapabilities.getImageFormats()).thenReturn(modifiedImageFormats)!!
        val layerFactory = LayerFactory()
        try {
            val wmsLayerConfig: WmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(
                wmsCapabilities,
                wmsLayerCapabilities
            )!!
            Assert.fail("Invalid Image Format")
        } catch (e: RuntimeException) {
            Assert.assertNotNull("Invalid Image Format")
        }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLevelSetConfigFromWmsCapabilities_Nominal() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        val layerFactory = LayerFactory()
        val levelSetConfig: LevelSetConfig =
            layerFactory.getLevelSetConfigFromWmsCapabilities(wmsLayerCapabilities)!!
        assertEquals("Bounding Box", Sector().setFullSphere(), levelSetConfig.sector)
        assertEquals("Number of Levels", 47, levelSetConfig.numLevels)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLevelSetConfigFromWmsCapabilities_CoarseScaleDenominator() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        PowerMockito.`when`(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(1e13)
        val layerFactory = LayerFactory()
        val levelSetConfig: LevelSetConfig =
            layerFactory.getLevelSetConfigFromWmsCapabilities(wmsLayerCapabilities)!!
        assertEquals("Number of Levels", 1, levelSetConfig.numLevels)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLevelSetConfigFromWmsCapabilities_NullScaleDenominator() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        PowerMockito.`when`(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(null)
        val layerFactory = LayerFactory()
        val levelSetConfig: LevelSetConfig =
            layerFactory.getLevelSetConfigFromWmsCapabilities(wmsLayerCapabilities)!!
        assertEquals("Number of Levels", 13, levelSetConfig.numLevels)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testGetLevelSetConfigFromWmsCapabilities_CoarseScaleHint() {
        val wmsCapabilities: WmsCapabilities = LayerFactoryTest.getBoilerPlateWmsCapabilities()
        val wmsLayerCapabilities: WmsLayerCapabilities =
            wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME)!!
        PowerMockito.`when`(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(null)
        PowerMockito.`when`(wmsLayerCapabilities.getMinScaleHint()).thenReturn(1336.34670162102)
        val layerFactory = LayerFactory()
        val levelSetConfig: LevelSetConfig =
            layerFactory.getLevelSetConfigFromWmsCapabilities(wmsLayerCapabilities)!!
        assertEquals("Number of Levels", 5, levelSetConfig.numLevels)
    }
}