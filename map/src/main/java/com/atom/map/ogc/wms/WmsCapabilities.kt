package com.atom.map.ogc.wms

import android.util.Xml
import com.atom.map.util.Logger
import com.atom.map.util.xml.XmlModel
import com.atom.map.util.xml.XmlModelParser
import java.io.InputStream
import java.util.*

open class WmsCapabilities() : XmlModel() {

    companion object {
        @Throws(Exception::class)
        fun getCapabilities(inputStream: InputStream): WmsCapabilities {
            val pullParser = Xml.newPullParser()
            pullParser.setInput(inputStream, null /*inputEncoding*/)
            val modelParser: XmlModelParser = WmsXmlParser()
            modelParser.xpp = (pullParser)
            return modelParser.parse() as? WmsCapabilities
                ?: throw RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "WmsCapabilities",
                        "getCapability",
                        "Invalid WMS Capabilities input"
                    )
                )
        }
    }

    open var version: String? = null

    open var updateSequence: String? = null

    open var service: WmsService? = null

    open var capability: WmsCapability? = null

    open fun getNamedLayers(): List<WmsLayer> {
        val namedLayers: MutableList<WmsLayer> = ArrayList()
        capability?.layers?.forEach {
            namedLayers.addAll(it.getNamedLayers())
        }
        return namedLayers
    }

    open fun getNamedLayer(name: String?): WmsLayer? {
        if (name == null || name.isEmpty()) {
            return null
        }
        val namedLayers = getNamedLayers()
        for (layer in namedLayers) {
            if (layer.name.equals(name)) {
                return layer
            }
        }
        return null
    }
    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "version" -> {
                version = value as String
            }
            "updateSequence" -> {
                updateSequence = value as String
            }
            "Service" -> {
                service = value as WmsService
            }
            "Capability" -> {
                capability = value as WmsCapability
            }
        }
    }
}