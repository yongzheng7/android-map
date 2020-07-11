package com.atom.wyz.worldwind.util.xml

import android.util.Xml
import com.atom.wyz.worldwind.ogc.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Constructor
import javax.xml.namespace.QName

class XmlPullParserContext {
    companion object {
        const val UNRECOGNIZED_ELEMENT_PARSER =
            "gov.nasa.worldwind.util.xml.UnknownElementParser"
        const val DEFAULT_NAMESPACE = "http://www.opengis.net/wms"

    }

    var parser: XmlPullParser? = null

    var parserModels: HashMap<QName, XmlModel?> = hashMapOf()

    var defaultNamespaceUri :String ?= null

    constructor(defaultNamespaceUri: String?) {
        this.defaultNamespaceUri = defaultNamespaceUri
        this.initializeParsers()
    }

    protected fun initializeParsers() {

        // Wms Element Registration
        registerParsableModel(
            QName(defaultNamespaceUri, "ContactAddress"),
            WmsAddress(defaultNamespaceUri)
        )
        // TODO check wms schema for element name
        registerParsableModel(
            QName(defaultNamespaceUri, "AuthorityUrl"),
            WmsAuthorityUrl(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "BoundingBox"),
            WmsBoundingBox(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "DCPType"),
            WmsDcpType(defaultNamespaceUri)
        )
        // TODO check wms schema for element name
        registerParsableModel(
            QName(defaultNamespaceUri, "LayerInfo"),
            WmsLayerInfoURL(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "OnlineResource"),
            WmsOnlineResource(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "LogoURL"),
            WmsLogoUrl(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "Attribution"),
            WmsLayerAttribution(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(
                defaultNamespaceUri,
                "AddressType"
            ), XmlModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "Address"),
            XmlModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "City"),
            XmlModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(
                defaultNamespaceUri,
                "StateOrProvince"
            ), XmlModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "PostCode"),
            XmlModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "Country"),
            XmlModel(defaultNamespaceUri)
        )

        registerParsableModel(
            QName(defaultNamespaceUri, "HTTP"),
            NameStringModel(defaultNamespaceUri)
        )
        registerParsableModel(
            QName(defaultNamespaceUri, "Get"),
            NameStringModel(defaultNamespaceUri)
        )

    }

    @Throws(XmlPullParserException::class)
    fun setParserInput(`is`: InputStream?) {
        this.parser = Xml.newPullParser()
        this.parser?.setInput(`is`, null)
    }

    fun createParsableModel(eventName: QName): XmlModel? {
        val model = this.parserModels[eventName] ?: return XmlModel(eventName.namespaceURI)
        try {
            // create a duplicate instance using reflective utilities
            val ctor: Constructor<out XmlModel> = model::class.java.getConstructor(String::class.java)
            //ctor.setAccessible(true);
            return ctor.newInstance(eventName.namespaceURI) as XmlModel
        } catch (e: Exception) {
            // TODO log error
        }
        return null
    }

    fun registerParsableModel(elementName: QName, parsableModel: XmlModel) {
        parserModels.put(elementName, parsableModel)
    }

    fun getUnrecognizedElementModel(): XmlModel {
        return XmlModel(this.defaultNamespaceUri)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun isStartElement(event: QName): Boolean {
        val xmlPullParser = this.parser ?: return false
        return xmlPullParser.eventType == XmlPullParser.START_TAG
                && xmlPullParser.name != null
                && xmlPullParser.name == event.localPart
                && xmlPullParser.namespace == event.namespaceURI
    }
}