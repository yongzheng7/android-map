package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.xml.namespace.QName

class WmsCapabilities(namespaceUri: String?) : XmlModel(namespaceUri) {

    companion object{
        val VERSION = QName("", "version")

        val UPDATE_SEQUENCE =
            QName("", "updateSequence")

        @Throws(XmlPullParserException::class, IOException::class)
        fun getCapabilities(`is`: InputStream ): WmsCapabilities {

            // Initialize the pull parser context
            val ctx = WmsPullParserContext(
                XmlPullParserContext.DEFAULT_NAMESPACE
            )
            ctx.setParserInput(`is`)

            // Parse the Xml document until a Wms service is discovered
            val wmsCapabilities =
                WmsCapabilities(
                    XmlPullParserContext.DEFAULT_NAMESPACE
                )
            wmsCapabilities.read(ctx)
            return wmsCapabilities
        }
    }

    lateinit var capabilityInformation: QName

    lateinit var serviceInformation: QName

    init {
        initialize()
    }

    protected fun initialize() {
        capabilityInformation = QName(this.namespaceUri, "Capability")
        serviceInformation = QName(this.namespaceUri, "Service")
    }

    fun getNamedLayers(): List<WmsLayerCapabilities>? {
        val capInfo: WmsCapabilityInformation =
            this.getField(capabilityInformation) as WmsCapabilityInformation?
                ?: return null
        val namedLayers: MutableList<WmsLayerCapabilities> =
            ArrayList<WmsLayerCapabilities>()

        for (layer in getCapabilityInformation()!!.getLayerList()) {
            namedLayers.addAll(layer.getNamedLayers()!!)
        }
        return namedLayers
    }

    fun getLayerByName(name: String?): WmsLayerCapabilities? {
        if (name == null || name.isEmpty()) {
            return null
        }
        val namedLayers = getNamedLayers()
        if (namedLayers != null) {
            for (layer in namedLayers) {
                if (layer.getName().equals(name)) {
                    return layer
                }
            }
        }
        return null
    }

    fun getCapabilityInformation(): WmsCapabilityInformation? {
        return this.getField(capabilityInformation) as WmsCapabilityInformation?
    }

    /**
     * Returns the document's service information.
     *
     * @return the document's service information.
     */
    fun getServiceInformation(): WmsServiceInformation? {
        return this.getField(serviceInformation) as WmsServiceInformation?
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    fun getVersion(): String? {
        return this.getField(VERSION).toString()
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */
    fun getUpdateSequence(): String? {
        val o = this.getField(UPDATE_SEQUENCE)
        return o?.toString()
    }

    fun getImageFormats(): MutableSet<String>? {
        val capInfo: WmsCapabilityInformation = getCapabilityInformation() ?: return null
        return capInfo.getImageFormats()
    }

    fun getRequestURL(
        requestName: String?,
        requestMethod: String?
    ): String? {
        if (requestName == null || requestMethod == null) {
            return null
        }
        val capabilityInformation = getCapabilityInformation()
            ?: return null
        var requestDescription: WmsRequestDescription? = null
        if (requestName == "GetCapabilities") {
            requestDescription = capabilityInformation.getCapabilitiesInfo()
        } else if (requestName == "GetMap") {
            requestDescription = capabilityInformation.getMapInfo()
        } else if (requestName == "GetFeatureInfo") {
            requestDescription = capabilityInformation.getFeatureInfo()
        }
        if (requestDescription == null) {
            return null
        }
        val onlineResource =
            requestDescription.getOnlineResouce(requestMethod)
                ?: return null
        return onlineResource.getHref()
    }

    override fun toString(): String
    {
        val sb = StringBuilder()
        sb.append("Version: ").append(if (getVersion() != null) getVersion() else "none")
            .append("\n")
        sb.append("UpdateSequence: ")
            .append(if (getUpdateSequence() != null) getUpdateSequence() else "none")
        sb.append("\n")
        sb.append(if (getServiceInformation() != null) getServiceInformation() else "Service Information: none")
        sb.append("\n")
        sb.append(if (getCapabilityInformation() != null) getCapabilityInformation() else "Capability Information: none")
        sb.append("\n")
        sb.append("LAYERS\n")
        for (layerCaps in getNamedLayers()!!) {
            sb.append(layerCaps.toString()).append("\n")
        }
        return sb.toString()
    }
}