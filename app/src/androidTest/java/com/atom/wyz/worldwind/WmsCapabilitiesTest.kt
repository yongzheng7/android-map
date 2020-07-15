package com.atom.wyz.worldwind

import android.content.res.Resources
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.wms.*
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.*
import javax.xml.namespace.QName

@RunWith(AndroidJUnit4::class)
@SmallTest
class WmsCapabilitiesTest {
    companion object{
        const val DELTA = 1e-9
    }

    lateinit var wmsCapabilities130: WmsCapabilities

    lateinit var inputStream130: InputStream

    lateinit var wmsCapabilities111: WmsCapabilities

    lateinit var inputStream111: InputStream


    @Before
    @Throws(Exception::class)
    fun setup() {
        val resources: Resources = getInstrumentation().getTargetContext().getResources()
        inputStream130 =
            resources.openRawResource(R.raw.test_gov_nasa_worldwind_wms_capabilities_v1_3_0_spec)
        inputStream111 =
            resources.openRawResource(R.raw.test_gov_nasa_worldwind_wms_capabilities_v1_1_1_spec)
        wmsCapabilities130 =
            WmsCapabilities.getCapabilities(BufferedInputStream(inputStream130))
        wmsCapabilities111 =
            WmsCapabilities.getCapabilities(BufferedInputStream(inputStream111))
    }

    @After
    @Throws(java.lang.Exception::class)
    fun shutdown() {
        inputStream130.close()
        inputStream111.close()
    }

    @Test
    fun testGetVersion_Version130() {
        Assert.assertTrue(
            "Version",
            wmsCapabilities130.getVersion().equals("1.3.0")
        )
    }

    @Test
    fun testGetVersion_Version111() {
        Assert.assertTrue(
            "Version",
            wmsCapabilities111.getVersion().equals("1.1.1")
        )
    }

    @Test
    fun testGetImageFormats_Version130() {
        val expectedValues: MutableSet<String> =
            HashSet()
        expectedValues.addAll(
            Arrays.asList(
                "image/gif",
                "image/png",
                "image/jpeg"
            )
        )
        val actualValues =
            wmsCapabilities130.getImageFormats()
        Assert.assertEquals("Image Version", expectedValues, actualValues)
    }

    @Test
    fun testGetImageFormats_Version111() {
        val expectedValues: MutableSet<String> =
            HashSet()
        expectedValues.addAll(
            Arrays.asList(
                "image/gif",
                "image/png",
                "image/jpeg"
            )
        )
        val actualValues =
            wmsCapabilities111.getImageFormats()
        Assert.assertEquals("Image Version", expectedValues, actualValues)
    }

    @Test
    fun testGetServiceInformation_GetAbstract_Version130() {
        val expectedValue =
            "Map Server maintained by Acme Corporation. Contact: webmaster@wmt.acme.com. High-quality maps showing" +
                    "            roadrunner nests and possible ambush locations."
        val serviceInformation: WmsServiceInformation? =
            wmsCapabilities130.getServiceInformation()
        val serviceAbstract: String = serviceInformation!!.getServiceAbstract()!!
        Assert.assertEquals("Service Abstract", expectedValue, serviceAbstract)
    }

    @Test
    fun testGetServiceInformation_GetAbstract_Version111() {
        val expectedValue =
            "WMT Map Server maintained by Acme Corporation. Contact: webmaster@wmt.acme.com. High-quality maps" +
                    "            showing roadrunner nests and possible ambush locations."
        val serviceInformation: WmsServiceInformation? =
            wmsCapabilities111.getServiceInformation()
        val serviceAbstract: String = serviceInformation!!.getServiceAbstract()!!
        Assert.assertEquals("Service Abstract", expectedValue, serviceAbstract)
    }

    @Test
    fun testGetServiceInformation_GetName_Version130() {
        val expectedValue = "WMS"
        val serviceName =
            wmsCapabilities130.getServiceInformation()!!.getServiceName()
        Assert.assertEquals("Service Name", expectedValue, serviceName)
    }

    @Test
    fun testGetServiceInformation_GetName_Version111() {
        val expectedValue = "OGC:WMS"
        val serviceName =
            wmsCapabilities111.getServiceInformation()!!.getServiceName()
        Assert.assertEquals("Service Name", expectedValue, serviceName)
    }

    @Test
    fun testGetServiceInformation_GetTitle_Version130() {
        val expectedValue = "Acme Corp. Map Server"
        val serviceTitle =
            wmsCapabilities130.getServiceInformation()!!.getServiceTitle()
        Assert.assertEquals("Service Title", expectedValue, serviceTitle)
    }

    @Test
    fun testGetServiceInformation_GetTitle_Version111() {
        val expectedValue = "Acme Corp. Map Server"
        val serviceTitle =
            wmsCapabilities111.getServiceInformation()!!.getServiceTitle()
        Assert.assertEquals("Service Title", expectedValue, serviceTitle)
    }

    @Test
    fun testGetServiceInformation_GetKeywords_Version130() {
        val expectedKeywords: MutableSet<String> =
            HashSet()
        expectedKeywords.addAll(
            Arrays.asList(
                "bird",
                "roadrunner",
                "ambush"
            )
        )
        val keywords =
            wmsCapabilities130.getServiceInformation()!!.getKeywords()
        Assert.assertEquals("Service Keywords", expectedKeywords, keywords)
    }

    @Test
    fun testGetServiceInformation_GetKeywords_Version111() {
        val expectedKeywords: MutableSet<String> =
            HashSet()
        expectedKeywords.addAll(
            Arrays.asList(
                "bird",
                "roadrunner",
                "ambush"
            )
        )
        val keywords =
            wmsCapabilities111.getServiceInformation()!!.getKeywords()
        Assert.assertEquals("Service Keywords", expectedKeywords, keywords)
    }

    @Test
    fun testGetServiceInformation_GetOnlineResource_Version130() {
        val expectedLinkType = "simple"
        val expectedLink = "http://hostname/"
        val serviceInformation: WmsServiceInformation? =
            wmsCapabilities130.getServiceInformation()
        val linkType: String = serviceInformation!!.getOnlineResource()!!.getType()!!
        val link: String = serviceInformation.getOnlineResource()!!.getHref()!!
        Assert.assertEquals(
            "Service Online Resource Link Type",
            expectedLinkType,
            linkType
        )
        Assert.assertEquals("Service Online Resource Link", expectedLink, link)
    }

    @Test
    fun testGetServiceInformation_GetOnlineResource_Version111() {
        val expectedLinkType = "simple"
        val expectedLink = "http://hostname/"
        val serviceInformation: WmsServiceInformation? =
            wmsCapabilities111.getServiceInformation()
        val linkType: String = serviceInformation!!.getOnlineResource()!!.getType()!!
        val link: String = serviceInformation.getOnlineResource()!!.getHref()!!
        Assert.assertEquals(
            "Service Online Resource Link Type",
            expectedLinkType,
            linkType
        )
        Assert.assertEquals("Service Online Resource Link", expectedLink, link)
    }

    @Test
    fun testGetServiceInformation_GetContactPersonPrimary_Version130() {
        val expectedPerson = "Jeff Smith"
        val expectedOrganization = "NASA"
        val contactInformation: WmsContactInformation? =
            wmsCapabilities130.getServiceInformation()!!.getContactInformation()
        val person: String = contactInformation!!.getPersonPrimary()!!
        val organization: String = contactInformation.getOrganization()!!
        Assert.assertEquals(
            "Service Contact Information Person Primary",
            expectedPerson,
            person
        )
        Assert.assertEquals(
            "Service Contact Information Organization",
            expectedOrganization,
            organization
        )
    }

    @Test
    fun testGetServiceInformation_GetContactPersonPrimary_Version111() {
        val expectedPerson = "Jeff deLaBeaujardiere"
        val expectedOrganization = "NASA"
        val contactInformation: WmsContactInformation? =
            wmsCapabilities111.getServiceInformation()!!.getContactInformation()
        val person: String = contactInformation!!.getPersonPrimary()!!
        val organization: String = contactInformation.getOrganization()!!
        Assert.assertEquals(
            "Service Contact Information Person Primary",
            expectedPerson,
            person
        )
        Assert.assertEquals(
            "Service Contact Information Organization",
            expectedOrganization,
            organization
        )
    }

    @Test
    fun testGetServiceInformation_GetContactAddress_Version130() {
        val expectedAddressType = "postal"
        val expectedAddress = "NASA Goddard Space Flight Center"
        val expectedCity = "Greenbelt"
        val expectedState = "MD"
        val expectedPostCode = "20771"
        val expectedCountry = "USA"
        val contactAddress: WmsAddress? =
            wmsCapabilities130.getServiceInformation()!!.getContactInformation()!!.getContactAddress()
        val addressType: String = contactAddress!!.getAddressType()!!
        val address: String = contactAddress.getAddress()!!
        val city: String = contactAddress.getCity()!!
        val state: String = contactAddress.getStateOrProvince()!!
        val postCode: String = contactAddress.getPostCode()!!
        val country: String = contactAddress.getCountry()!!
        Assert.assertEquals(
            "Service Contact Address Type",
            expectedAddressType,
            addressType
        )
        Assert.assertEquals("Service Contact Address", expectedAddress, address)
        Assert.assertEquals("Service Contact Address City", expectedCity, city)
        Assert.assertEquals("Service Contact Address State", expectedState, state)
        Assert.assertEquals(
            "Service Contact Address Post Code",
            expectedPostCode,
            postCode
        )
        Assert.assertEquals(
            "Service Contact Address Country",
            expectedCountry,
            country
        )
    }

    @Test
    fun testGetServiceInformation_GetContactAddress_Version111() {
        val expectedAddressType = "postal"
        val expectedAddress = "NASA Goddard Space Flight Center, Code 933"
        val expectedCity = "Greenbelt"
        val expectedState = "MD"
        val expectedPostCode = "20771"
        val expectedCountry = "USA"
        val contactAddress: WmsAddress? =
            wmsCapabilities111.getServiceInformation()!!.getContactInformation()!!.getContactAddress()
        val addressType: String = contactAddress!!.getAddressType()!!
        val address: String = contactAddress.getAddress()!!
        val city: String = contactAddress.getCity()!!
        val state: String = contactAddress.getStateOrProvince()!!
        val postCode: String = contactAddress.getPostCode()!!
        val country: String = contactAddress.getCountry()!!
        Assert.assertEquals(
            "Service Contact Address Type",
            expectedAddressType,
            addressType
        )
        Assert.assertEquals("Service Contact Address", expectedAddress, address)
        Assert.assertEquals("Service Contact Address City", expectedCity, city)
        Assert.assertEquals("Service Contact Address State", expectedState, state)
        Assert.assertEquals(
            "Service Contact Address Post Code",
            expectedPostCode,
            postCode
        )
        Assert.assertEquals(
            "Service Contact Address Country",
            expectedCountry,
            country
        )
    }

    @Test
    fun testGetServiceInformation_GetPhone_Version130() {
        val expectedValue = "+1 301 555-1212"
        val voiceTelephone =
            wmsCapabilities130.getServiceInformation()!!.getContactInformation()!!.getVoiceTelephone()
        Assert.assertEquals("Service Phone", expectedValue, voiceTelephone)
    }

    @Test
    fun testGetServiceInformation_GetPhone_Version111() {
        val expectedValue = "+1 301 286-1569"
        val voiceTelephone =
            wmsCapabilities111.getServiceInformation()!!.getContactInformation()!!.getVoiceTelephone()
        Assert.assertEquals("Service Fees", expectedValue, voiceTelephone)
    }

    @Test
    fun testGetServiceInformation_GetEmail_Version130() {
        val expectedValue = "user@host.com"
        val fees =
            wmsCapabilities130.getServiceInformation()!!.getContactInformation()!!.getElectronicMailAddress()
        Assert.assertEquals("Service Email", expectedValue, fees)
    }

    @Test
    fun testGetServiceInformation_GetEmail_Version111() {
        val expectedValue = "delabeau@iniki.gsfc.nasa.gov"
        val fees =
            wmsCapabilities111.getServiceInformation()!!.getContactInformation()!!.getElectronicMailAddress()
        Assert.assertEquals("Service Email", expectedValue, fees)
    }

    @Test
    fun testGetServiceInformation_GetFees_Version130() {
        val expectedValue = "none"
        val fees = wmsCapabilities130.getServiceInformation()!!.getFees()
        Assert.assertEquals("Service Fees", expectedValue, fees)
    }

    @Test
    fun testGetServiceInformation_GetFees_Version111() {
        val expectedValue = "none"
        val fees = wmsCapabilities111.getServiceInformation()!!.getFees()
        Assert.assertEquals("Service Fees", expectedValue, fees)
    }

    @Test
    fun testGetServiceInformation_GetAccessConstraints_Version130() {
        val expectedValue = "none"
        val accessConstraints =
            wmsCapabilities130.getServiceInformation()!!.getAccessConstraints()
        Assert.assertEquals("Service Fees", expectedValue, accessConstraints)
    }

    @Test
    fun testGetServiceInformation_GetAccessConstraints_Version111() {
        val expectedValue = "none"
        val accessConstraints =
            wmsCapabilities111.getServiceInformation()!!.getAccessConstraints()
        Assert.assertEquals("Service Fees", expectedValue, accessConstraints)
    }

    @Test
    fun testGetServiceInformation_GetLayerLimit_Version130() {
        val expectedValue = 16
        val layerLimit = wmsCapabilities130.getServiceInformation()!!.getLayerLimit()
        Assert.assertEquals("Service Layer Limit", expectedValue, layerLimit)
    }

    @Test
    fun testGetServiceInformation_GetMaxHeightWidth_Version130() {
        val expectedHeight = 2048
        val expectedWidth = 2048
        val maxHeight = wmsCapabilities130.getServiceInformation()!!.getMaxHeight()
        val maxWidth = wmsCapabilities130.getServiceInformation()!!.getMaxWidth()
        Assert.assertEquals("Service Max Height", expectedHeight, maxHeight)
        Assert.assertEquals("Service Max Width", expectedWidth, maxWidth)
    }

    @Test
    fun testGetLayerByName_Version130() {
        val layersToTest =
            Arrays.asList(
                "ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
                "Pressure", "ozone_image", "population"
            )
        for (layer in layersToTest) {
            val wmsLayer: WmsLayerCapabilities? = wmsCapabilities130.getLayerByName(layer)
            Assert.assertNotNull("Get Layer By Name $layer", wmsLayer)
        }
    }

    @Test
    fun testGetLayerByName_Version111() {
        val layersToTest =
            Arrays.asList(
                "ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
                "Pressure", "ozone_image", "population"
            )
        for (layer in layersToTest) {
            val wmsLayer: WmsLayerCapabilities? = wmsCapabilities111.getLayerByName(layer)
            Assert.assertNotNull("Get Layer By Name $layer", wmsLayer)
        }
    }

    @Test
    fun testGetNamedLayers_Version130() {
        val expectedLayers =
            Arrays.asList(
                "ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
                "Pressure", "ozone_image", "population"
            )
        val initialSize = expectedLayers.size
        val layers: List<WmsLayerCapabilities>? =
            wmsCapabilities130.getNamedLayers()
        var foundCount = 0
        for (layer in layers!!) {
            if (expectedLayers.contains(layer.getName())) {
                foundCount++
            }
        }
        Assert.assertEquals("Get Named Layers Count", initialSize, layers.size)
        Assert.assertEquals("Get Named Layers Content", initialSize, foundCount)
    }

    @Test
    fun testGetNamedLayers_Version111() {
        val expectedLayers =
            Arrays.asList(
                "ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
                "Pressure", "ozone_image", "population"
            )
        val initialSize = expectedLayers.size
        val layers: List<WmsLayerCapabilities>? =
            wmsCapabilities111.getNamedLayers()
        var foundCount = 0
        for (layer in layers!!) {
            if (expectedLayers.contains(layer.getName())) {
                foundCount++
            }
        }
        Assert.assertEquals("Get Named Layers Count", initialSize, layers.size)
        Assert.assertEquals("Get Named Layers Content", initialSize, foundCount)
    }

    @Test
    fun testNamedLayerProperties_GetAttribution_Version130() {
        val expectedAttributionTitle = "State College University"
        val expectedAttributionUrl = "http://www.university.edu/"
        val expectedAttributionLogoFormat = "image/gif"
        val expectedAttributionLogoUrl = "http://www.university.edu/icons/logo.gif"
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val attributions: Set<WmsLayerAttribution> =
            wmsLayerCapabilities!!.getAttributions()!!
        val attribution: WmsLayerAttribution = attributions.iterator().next()
        Assert.assertEquals("Layer Attributions Count", 1, attributions.size)
        assertEquals("Layer Attributions Title", expectedAttributionTitle, attribution.getTitle())
        assertEquals(
            "Layer Attributions Url",
            expectedAttributionUrl,
            attribution.getOnlineResource()!!.getHref()
        )
        assertEquals(
            "Layer Attributions Logo Format",
            expectedAttributionLogoFormat,
            attribution.getLogoURL()!!.getFormats()!!.iterator().next()
        )
        assertEquals(
            "Layer Attributions Logo Url",
            expectedAttributionLogoUrl,
            attribution.getLogoURL()!!.getOnlineResource()!!.getHref()
        )
    }

    @Test
    fun testNamedLayerProperties_GetAttribution_Version111() {
        val expectedAttributionTitle = "State College University"
        val expectedAttributionUrl = "http://www.university.edu/"
        val expectedAttributionLogoFormat = "image/gif"
        val expectedAttributionLogoUrl = "http://www.university.edu/icons/logo.gif"
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val attributions: Set<WmsLayerAttribution> =
            wmsLayerCapabilities!!.getAttributions()!!
        val attribution: WmsLayerAttribution = attributions.iterator().next()
        Assert.assertEquals("Layer Attributions Count", 1, attributions.size)
        assertEquals("Layer Attributions Title", expectedAttributionTitle, attribution.getTitle())
        assertEquals(
            "Layer Attributions Url",
            expectedAttributionUrl,
            attribution.getOnlineResource()!!.getHref()
        )
        assertEquals(
            "Layer Attributions Logo Format",
            expectedAttributionLogoFormat,
            attribution.getLogoURL()!!.getFormats()!!.iterator().next()
        )
        assertEquals(
            "Layer Attributions Logo Url",
            expectedAttributionLogoUrl,
            attribution.getLogoURL()!!.getOnlineResource()!!.getHref()
        )
    }

    @Test
    fun testNamedLayerProperties_GetTitleAbstract_Version130() {
        val expectedTitle = "Roads at 1:1M scale"
        val expectedAbstract = "Roads at a scale of 1 to 1 million."
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val title: String = wmsLayerCapabilities!!.getTitle()!!
        val layerAbstract: String = wmsLayerCapabilities.getLayerAbstract()!!
        Assert.assertEquals("Layer Title", expectedTitle, title)
        Assert.assertEquals("Layer Abstract", expectedAbstract, layerAbstract)
    }

    @Test
    fun testNamedLayerProperties_GetTitleAbstract_Version111() {
        val expectedTitle = "Roads at 1:1M scale"
        val expectedAbstract = "Roads at a scale of 1 to 1 million."
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val title: String = wmsLayerCapabilities!!.getTitle()!!
        val layerAbstract: String = wmsLayerCapabilities.getLayerAbstract()!!
        Assert.assertEquals("Layer Title", expectedTitle, title)
        Assert.assertEquals("Layer Abstract", expectedAbstract, layerAbstract)
    }

    @Test
    fun testNamedLayerProperties_GetKeywords_Version130() {
        val expectedKeywords: MutableSet<String> =
            HashSet()
        expectedKeywords.addAll(
            Arrays.asList(
                "road",
                "transportation",
                "atlas"
            )
        )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val keywords: Set<String> = wmsLayerCapabilities!!.getKeywords()!!
        Assert.assertEquals("Layer Keywords", expectedKeywords, keywords)
    }

    @Test
    fun testNamedLayerProperties_GetKeywords_Version111() {
        val expectedKeywords: MutableSet<String> =
            HashSet()
        expectedKeywords.addAll(
            Arrays.asList(
                "road",
                "transportation",
                "atlas"
            )
        )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val keywords: Set<String> = wmsLayerCapabilities!!.getKeywords()!!
        Assert.assertEquals("Layer Keywords", expectedKeywords, keywords)
    }

    @Test
    fun testNamedLayerProperties_GetIdentities_Version130() {
        val expectedIdentities = 1
        val expectedAuthority = "DIF_ID"
        val expectedIdentifier = "123456"
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val identities: Set<WmsLayerIdentifier> =
            wmsLayerCapabilities!!.getIdentifiers()!!
        val authority: String = identities.iterator().next().getAuthority()!!
        val identifier: String = identities.iterator().next().getIdentifier()!!
        Assert.assertEquals(
            "Layer Identifier Count",
            expectedIdentities,
            identities.size
        )
        Assert.assertEquals("Layer Authority", expectedAuthority, authority)
        Assert.assertEquals("Layer Identifier", expectedIdentifier, identifier)
    }

    @Test
    fun testNamedLayerProperties_GetIdentities_Version111() {
        val expectedIdentities = 1
        val expectedAuthority = "DIF_ID"
        val expectedIdentifier = "123456"
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val identities: Set<WmsLayerIdentifier> =
            wmsLayerCapabilities!!.getIdentifiers()!!
        val authority: String = identities.iterator().next().getAuthority()!!
        val identifier: String = identities.iterator().next().getIdentifier()!!
        Assert.assertEquals(
            "Layer Identifier Count",
            expectedIdentities,
            identities.size
        )
        Assert.assertEquals("Layer Authority", expectedAuthority, authority)
        Assert.assertEquals("Layer Identifier", expectedIdentifier, identifier)
    }

    @Test
    fun testNamedLayerProperties_GetMetadataUrls_Version130() {
        val expectedMetadataUrls = 2
        val expectedMetadataUrlFormats =
            Arrays.asList("text/plain", "text/xml")
        val expectedMetadataUrlTypes =
            Arrays.asList("FGDC:1998", "ISO19115:2003")
        val expectedMetadataUrlUrls =
            Arrays.asList(
                "http://www.university.edu/metadata/roads.txt",
                "http://www.university.edu/metadata/roads.xml"
            )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val metadataUrls: Set<WmsLayerInfoUrl> =
            wmsLayerCapabilities!!.getMetadataUrls()!!
        for (metadataUrl in metadataUrls) {
            val metadataUrlType: String =
                metadataUrl.getField(QName("", "type")).toString()
            Assert.assertTrue(
                "Layer MetadataUrl Format",
                expectedMetadataUrlFormats.contains(metadataUrl.getFormat())
            )
            Assert.assertTrue(
                "Layer MetadataUrl Names",
                expectedMetadataUrlTypes.contains(metadataUrlType)
            )
            Assert.assertTrue(
                "Layer MetadataUrl Url",
                expectedMetadataUrlUrls.contains(metadataUrl.getOnlineResource()!!.getHref())
            )
        }
        Assert.assertEquals(
            "Layer MetadataUrl Count",
            expectedMetadataUrls,
            metadataUrls.size
        )
    }

    @Test
    fun testNamedLayerProperties_GetMetadataUrls_Version111() {
        val expectedMetadataUrls = 2
        val expectedMetadataUrlFormats =
            Arrays.asList("text/plain", "text/xml")
        val expectedMetadataUrlTypes =
            Arrays.asList("FGDC", "FGDC")
        val expectedMetadataUrlUrls =
            Arrays.asList(
                "http://www.university.edu/metadata/roads.txt",
                "http://www.university.edu/metadata/roads.xml"
            )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val metadataUrls: Set<WmsLayerInfoUrl> =
            wmsLayerCapabilities!!.getMetadataUrls()!!
        for (metadataUrl in metadataUrls) {
            val metadataUrlType: String =
                metadataUrl.getField(QName("", "type")).toString()
            Assert.assertTrue(
                "Layer MetadataUrl Format",
                expectedMetadataUrlFormats.contains(metadataUrl.getFormat())
            )
            Assert.assertTrue(
                "Layer MetadataUrl Names",
                expectedMetadataUrlTypes.contains(metadataUrlType)
            )
            Assert.assertTrue(
                "Layer MetadataUrl Url",
                expectedMetadataUrlUrls.contains(metadataUrl.getOnlineResource()!!.getHref())
            )
        }
        Assert.assertEquals(
            "Layer MetadataUrl Count",
            expectedMetadataUrls,
            metadataUrls.size
        )
    }

    @Test
    fun testNamedLayerProperties_GetStyles_Version130() {
        val expectedStyles = 2
        val expectedStyleNames =
            Arrays.asList("ATLAS", "USGS")
        val expectedStyleTitles =
            Arrays.asList("Road atlas style", "USGS Topo Map Style")
        val expectedStyleLegendUrl =
            Arrays.asList(
                "http://www.university.edu/legends/atlas.gif",
                "http://www.university.edu/legends/usgs.gif"
            )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val styles: Set<WmsLayerStyle> = wmsLayerCapabilities!!.getStyles()
        for (style in styles) {
            Assert.assertTrue(
                "Layer Style Names",
                expectedStyleNames.contains(style.getName())
            )
            Assert.assertTrue(
                "Layer Style Titles",
                expectedStyleTitles.contains(style.getTitle())
            )
            val legendUrl: String =
                style.getLegendUrls()!!.iterator().next()!!.getOnlineResource()!!.getHref()!!
            Assert.assertTrue(
                "Layer Style Legend Url",
                expectedStyleLegendUrl.contains(legendUrl)
            )
        }
        Assert.assertEquals("Layer Style Count", expectedStyles, styles.size)
    }

    @Test
    fun testNamedLayerProperties_GetStyles_Version111() {
        val expectedStyles = 2
        val expectedStyleNames =
            Arrays.asList("ATLAS", "USGS")
        val expectedStyleTitles =
            Arrays.asList("Road atlas style", "USGS Topo Map Style")
        val expectedStyleLegendUrl =
            Arrays.asList(
                "http://www.university.edu/legends/atlas.gif",
                "http://www.university.edu/legends/usgs.gif"
            )
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val styles: Set<WmsLayerStyle> = wmsLayerCapabilities!!.getStyles()
        for (style in styles) {
            Assert.assertTrue(
                "Layer Style Names",
                expectedStyleNames.contains(style.getName())
            )
            Assert.assertTrue(
                "Layer Style Titles",
                expectedStyleTitles.contains(style.getTitle())
            )
            val legendUrl: String =
                style.getLegendUrls()!!.iterator().next()!!.getOnlineResource()!!.getHref()!!
            Assert.assertTrue(
                "Layer Style Legend Url",
                expectedStyleLegendUrl.contains(legendUrl)
            )
        }
        Assert.assertEquals("Layer Style Count", expectedStyles, styles.size)
    }

    @Test
    fun testNamedLayerProperties_GetReferenceSystems_Version130() {
        val expectedCrsValues: MutableSet<String> =
            HashSet()
        expectedCrsValues.addAll(Arrays.asList("EPSG:26986", "CRS:84"))
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val referenceSystems: Set<String> =
            wmsLayerCapabilities!!.getReferenceSystem()!!
        Assert.assertEquals(
            "Layer Reference System",
            expectedCrsValues,
            referenceSystems
        )
    }

    @Test
    fun testNamedLayerProperties_GetReferenceSystems_Version111() {
        val expectedSrsValues: MutableSet<String> =
            HashSet()
        expectedSrsValues.addAll(Arrays.asList("EPSG:26986", "EPSG:4326"))
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val referenceSystems: Set<String> =
            wmsLayerCapabilities!!.getReferenceSystem()!!
        Assert.assertEquals(
            "Layer Reference System",
            expectedSrsValues,
            referenceSystems
        )
    }

    @Test
    fun testNamedLayerProperties_GetGeographicBoundingBox_Version130() {
        val expectedGeographicBoundingBoxWestLong = -71.63
        val expectedGeographicBoundingBoxEastLong = -70.78
        val expectedGeographicBoundingBoxSouthLat = 41.75
        val expectedGeographicBoundingBoxNorthLat = 42.90
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val sector: Sector = wmsLayerCapabilities!!.getGeographicBoundingBox()!!
        assertEquals(
            "Layer Geographic Bounding Box West",
            expectedGeographicBoundingBoxWestLong,
            sector.minLongitude
        )
        assertEquals(
            "Layer Geographic Bounding Box East",
            expectedGeographicBoundingBoxEastLong,
            sector.maxLongitude
        )
        assertEquals(
            "Layer Geographic Bounding Box North",
            expectedGeographicBoundingBoxNorthLat,
            sector.maxLatitude
        )
        assertEquals(
            "Layer Geographic Bounding Box South",
            expectedGeographicBoundingBoxSouthLat,
            sector.minLatitude
        )
    }

    @Test
    fun testNamedLayerProperties_GetGeographicBoundingBox_Version11() {
        val expectedGeographicBoundingBoxWestLong = -71.63
        val expectedGeographicBoundingBoxEastLong = -70.78
        val expectedGeographicBoundingBoxSouthLat = 41.75
        val expectedGeographicBoundingBoxNorthLat = 42.90
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val sector: Sector = wmsLayerCapabilities!!.getGeographicBoundingBox()!!
        assertEquals(
            "Layer Geographic Bounding Box West",
            expectedGeographicBoundingBoxWestLong,
            sector.minLongitude
        )
        assertEquals(
            "Layer Geographic Bounding Box East",
            expectedGeographicBoundingBoxEastLong,
            sector.maxLongitude
        )
        assertEquals(
            "Layer Geographic Bounding Box North",
            expectedGeographicBoundingBoxNorthLat,
            sector.maxLatitude
        )
        assertEquals(
            "Layer Geographic Bounding Box South",
            expectedGeographicBoundingBoxSouthLat,
            sector.minLatitude
        )
    }

    @Test
    fun testNamedLayerProperties_GetBoundingBox_Version130() {
        val expectedCrs84BoundingBoxMinx = -71.63
        val expectedCrs84BoundingBoxMiny = 41.75
        val expectedCrs84BoundingBoxMaxx = -70.78
        val expectedCrs84BoundingBoxMaxy = 42.90
        val expectedEpsgBoundingBoxMinx = 189000.0
        val expectedEpsgBoundingBoxMiny = 834000.0
        val expectedEpsgBoundingBoxMaxx = 285000.0
        val expectedEpsgBoundingBoxMaxy = 962000.0
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities130.getLayerByName("ROADS_1M")
        val boxes: Set<WmsBoundingBox> = wmsLayerCapabilities!!.getBoundingBoxes()!!
        val boxIterator: Iterator<WmsBoundingBox> = boxes.iterator()
        while (boxIterator.hasNext()) {
            val box: WmsBoundingBox = boxIterator.next()
            val minx: Double = box.minx
            val miny: Double = box.miny
            val maxx: Double = box.maxx
            val maxy: Double = box.maxy
            if (box.crs.equals("CRS:84")) {
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Minx",
                    expectedCrs84BoundingBoxMinx,
                    minx
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Miny",
                    expectedCrs84BoundingBoxMiny,
                    miny
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Maxx",
                    expectedCrs84BoundingBoxMaxx,
                    maxx
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Maxy",
                    expectedCrs84BoundingBoxMaxy,
                    maxy
                )
            } else if (box.crs.equals("EPSG:26986")) {
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Minx",
                    expectedEpsgBoundingBoxMinx,
                    minx
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Miny",
                    expectedEpsgBoundingBoxMiny,
                    miny
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Maxx",
                    expectedEpsgBoundingBoxMaxx,
                    maxx
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Maxy",
                    expectedEpsgBoundingBoxMaxy,
                    maxy
                )
            } else {
                Assert.fail("Unexpected Layer Coordinate System")
            }
        }
        Assert.assertEquals("Layer Bounding Box Count", 2, boxes.size)
    }

    @Test
    fun testNamedLayerProperties_GetBoundingBox_Version111() {
        val expectedEpsg4326BoundingBoxMinx = -71.63
        val expectedEpsg4326BoundingBoxMiny = 41.75
        val expectedEpsg4326BoundingBoxMaxx = -70.78
        val expectedEpsg4326BoundingBoxMaxy = 42.90
        val expectedEpsgBoundingBoxMinx = 189000.0
        val expectedEpsgBoundingBoxMiny = 834000.0
        val expectedEpsgBoundingBoxMaxx = 285000.0
        val expectedEpsgBoundingBoxMaxy = 962000.0
        val wmsLayerCapabilities: WmsLayerCapabilities? =
            wmsCapabilities111.getLayerByName("ROADS_1M")
        val boxes: Set<WmsBoundingBox> = wmsLayerCapabilities!!.getBoundingBoxes()!!
        val boxIterator: Iterator<WmsBoundingBox> = boxes.iterator()
        while (boxIterator.hasNext()) {
            val box: WmsBoundingBox = boxIterator.next()
            val minx: Double = box.minx
            val miny: Double = box.miny
            val maxx: Double = box.maxx
            val maxy: Double = box.maxy
            if (box.crs.equals("EPSG:4326")) {
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Minx",
                    expectedEpsg4326BoundingBoxMinx,
                    minx
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Miny",
                    expectedEpsg4326BoundingBoxMiny,
                    miny
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Maxx",
                    expectedEpsg4326BoundingBoxMaxx,
                    maxx
                )
                Assert.assertEquals(
                    "Layer Bounding Box CRS:84 Maxy",
                    expectedEpsg4326BoundingBoxMaxy,
                    maxy
                )
            } else if (box.crs.equals("EPSG:26986")) {
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Minx",
                    expectedEpsgBoundingBoxMinx,
                    minx
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Miny",
                    expectedEpsgBoundingBoxMiny,
                    miny
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Maxx",
                    expectedEpsgBoundingBoxMaxx,
                    maxx
                )
                Assert.assertEquals(
                    "Layer Bounding Box EPSG:26986 Maxy",
                    expectedEpsgBoundingBoxMaxy,
                    maxy
                )
            } else {
                Assert.fail("Unexpected Layer Coordinate System")
            }
        }
        Assert.assertEquals("Layer Bounding Box Count", 2, boxes.size)
    }


}