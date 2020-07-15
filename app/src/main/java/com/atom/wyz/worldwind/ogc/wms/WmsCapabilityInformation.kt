package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsCapabilityInformation(namespaceUri: String?) : XmlModel(namespaceUri) {

    lateinit var capabilities: QName

    lateinit var map: QName

    lateinit var feature: QName

    lateinit var exceptions: QName

    lateinit var layers: QName

    lateinit var request: QName

    init {
        initialize()
    }

    protected fun initialize() {
        capabilities = QName(this.namespaceUri, "GetCapabilities")
        map = QName(this.namespaceUri, "GetMap")
        feature = QName(this.namespaceUri, "GetFeatureInfo")
        exceptions = QName(this.namespaceUri, "Exceptions")
        layers = QName(this.namespaceUri, "Layer")
        request = QName(this.namespaceUri, "Request")

    }

    fun getLayerList(): List<WmsLayerCapabilities> {
        val layers = this.getField(layers) as List<WmsLayerCapabilities>?
        return layers ?: emptyList()
    }

    fun getImageFormats(): MutableSet<String>? {
        val requests = this.getField(request) as XmlModel? ?: return mutableSetOf()
        val mapInfo = requests.getField(map) as WmsRequestDescription? ?: return mutableSetOf()
        return mapInfo.getFormats()
    }

    fun getCapabilitiesInfo(): WmsRequestDescription? {
        return (this.getField(request) as XmlModel?)?.getField(capabilities) as WmsRequestDescription?
    }

    fun getMapInfo(): WmsRequestDescription? {
        val request = this.getField(request) as XmlModel?
        return if (request != null) {
            request.getField(map) as WmsRequestDescription?
        } else null
    }

    fun getFeatureInfo(): WmsRequestDescription? {
        val request = this.getField(request) as XmlModel?
        return if (request != null) {
            request.getField(feature) as WmsRequestDescription?
        } else null
    }

    override fun setField(keyName: QName, value: Any?) {
        if (keyName == layers) {
            var layers =
                this.getField(layers) as MutableList<WmsLayerCapabilities>?
            if (layers == null) {
                layers = ArrayList()
                super.setField(this.layers, layers)
            }
            if (value is WmsLayerCapabilities) {
                layers.add(value)
                return
            }
        }
        super.setField(keyName, value)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Image Formats:\n")
        for (imageFormat in getImageFormats()!!) {
            sb.append(imageFormat).append("\n")
        }
        sb.append("Capabilities Info: " + getCapabilitiesInfo()).append("\n")
        sb.append("Map Info: ").append(getMapInfo()).append("\n")
        sb.append("Feature Info: ")
            .append(if (getFeatureInfo() != null) getFeatureInfo() else "none")
            .append("\n")
        return sb.toString()
    }
}