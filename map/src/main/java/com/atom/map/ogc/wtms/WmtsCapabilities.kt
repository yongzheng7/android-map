package com.atom.map.ogc.wtms

import android.util.Xml
import com.atom.map.ogc.ows.OwsOperationsMetadata
import com.atom.map.ogc.ows.OwsServiceIdentification
import com.atom.map.ogc.ows.OwsServiceProvider
import com.atom.map.util.Logger
import com.atom.map.util.xml.XmlModel
import com.atom.map.util.xml.XmlModelParser
import java.io.InputStream
import java.util.*

open class WmtsCapabilities : XmlModel() {

    companion object {
        @Throws(Exception::class)
        fun getCapabilities(inputStream: InputStream): WmtsCapabilities {
            val pullParser = Xml.newPullParser()
            pullParser.setInput(inputStream, null /*inputEncoding*/)
            val modelParser: XmlModelParser = WmtsXmlParser()
            modelParser.xpp = (pullParser)
            return modelParser.parse() as? WmtsCapabilities
                ?: throw RuntimeException(
                    Logger.logMessage(
                        Logger.ERROR,
                        "WmtsCapabilities",
                        "getCapabilities",
                        "Invalid WMTS Capabilities input"
                    )
                )
        }
    }

    open var version: String? = null

    open var updateSequence: String? = null

    open var serviceIdentification: OwsServiceIdentification? = null

    open var serviceProvider: OwsServiceProvider? = null

    open var operationsMetadata: OwsOperationsMetadata? = null

    open var contents: WmtsContents? = null

    open var themes: MutableList<WmtsTheme> = ArrayList<WmtsTheme>()

    open var serviceMetadataUrls: MutableList<WmtsElementLink> =
        ArrayList()

    open fun getLayers(): MutableList<WmtsLayer>? {
        return this.contents?.layers
    }

    open fun getLayer(identifier: String?): WmtsLayer? {
        getLayers()?.forEach {
            if (it.identifier.equals(identifier)) {
                return it
            }
        }
        return null
    }

    open fun getTileMatrixSets(): List<WmtsTileMatrixSet>? {
        return this.contents?.tileMatrixSets
    }

    open fun getTileMatrixSet(identifier: String?): WmtsTileMatrixSet? {
        getTileMatrixSets() ?.forEach{
            if (it.identifier.equals(identifier)) {
                return it
            }
        }
        return null
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "ServiceIdentification" -> {
                serviceIdentification = value as OwsServiceIdentification
            }
            "ServiceProvider" -> {
                serviceProvider = value as OwsServiceProvider
            }
            "OperationsMetadata" -> {
                operationsMetadata = value as OwsOperationsMetadata
            }
            "Contents" -> {
                contents = value as WmtsContents
            }
            "Themes" -> {
                themes.addAll((value as WmtsThemes).themes)
            }
            "ServiceMetadataURL" -> {
                serviceMetadataUrls.add((value as WmtsElementLink))
            }
            "version" -> {
                version = value as String
            }
            "updateSequence" -> {
                updateSequence = value as String
            }
        }
    }
}