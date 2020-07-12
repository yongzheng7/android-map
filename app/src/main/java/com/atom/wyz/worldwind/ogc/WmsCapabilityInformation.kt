package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsCapabilityInformation : XmlModel {

    lateinit var capabilities: QName

    lateinit var map: QName

    lateinit var feature: QName

    lateinit var exceptions: QName

    lateinit var layers: QName

    lateinit var request: QName

    constructor(namespaceUri: String?) : super(namespaceUri) {
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

    fun getImageFormats(): Set<String>? {
        val requests = this.getField(request) as XmlModel? ?: return emptySet()
        val mapInfo = requests.getField(map) as WmsRequestDescription? ?: return emptySet()
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
}