package com.atom.wyz.worldwind.util.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Constructor
import javax.xml.namespace.QName

open class XmlPullParserContext {
    companion object {
        const val DEFAULT_NAMESPACE = "http://www.opengis.net/wms"

    }

    var parser: XmlPullParser? = null

    var parserModels: HashMap<QName, XmlModel?> = hashMapOf()

    var namespaceUri :String ?= null
    set(value) {
        field = value
        parserModels.clear()
        initializeParsers()
    }

    constructor(namespaceUri: String?) {
        this.namespaceUri = namespaceUri
        this.initializeParsers()
    }

    protected open fun initializeParsers() {}

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
        return XmlModel(this.namespaceUri)
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