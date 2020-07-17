package com.atom.wyz.worldwind.util.xml

import com.atom.wyz.worldwind.util.Logger
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*
import javax.xml.namespace.QName

open class XmlModelParser() {

    open var xpp: XmlPullParser? = null

    open var xmlModelRegistry: MutableMap<QName, Class<out XmlModel>?> = HashMap()

    open var txtModelRegistry: MutableSet<QName> = HashSet()

    open var characters = StringBuilder()

    @Throws(XmlPullParserException::class, IOException::class)
    open fun parse(): Any? {
        val xmlPullParser = xpp ?: return null

        while (xmlPullParser.eventType != XmlPullParser.START_TAG) {
            xmlPullParser.next() // skip to the start of the first element
        }
        val name = QName(xmlPullParser.namespace, xmlPullParser.name)
        return this.parseElement(name, null /*parent*/)
    }

    open fun registerXmlModel(namespace: String?, name: String, parsableModel: Class<out XmlModel>?)
    {
        xmlModelRegistry[QName(namespace, name)] = parsableModel
    }

    open fun registerTxtModel(namespace: String?, name: String?)
    {
        txtModelRegistry.add(QName(namespace, name))
    }

    protected open fun createXmlModel(name: QName): XmlModel?
    {
        val clazz = xmlModelRegistry[name] ?: this.getUnrecognizedModel()
        return try {
            clazz.newInstance()
        } catch (e: Exception) {
            Logger.logMessage(
                Logger.ERROR, "XmlModelParser", "createParsableModel",
                "Exception invoking default constructor for " + clazz!!.name, e
            )
            null
        }
    }

    protected open fun getUnrecognizedModel(): Class<out XmlModel> {
        return DefaultXmlModel::class.java
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected open fun parseElement(
        name: QName,
        parent: XmlModel?
    ): Any? {
        return if (txtModelRegistry.contains(name)) {
            this.parseText(name)
        } else {
            this.parseXmlModel(name, parent)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected open fun parseXmlModel(
        name: QName,
        parent: XmlModel?
    ): XmlModel? {
        // Create an instance of an XML model object associated with the element's namespace and tag name.
        val model = createXmlModel(name) ?: return null
        model.parent = (parent)

        // Set up to accumulate the element's character data.
        var characters: StringBuilder? = null

        // Parse the element's attributes.
        var idx = 0
        val xmlPullParser = xpp ?: return null
        val len = xmlPullParser.attributeCount
        while (idx < len) {
            val attrName : String= xmlPullParser.getAttributeName(idx)
            val attrValue : String = xmlPullParser.getAttributeValue(idx)
            model.parseField(attrName, attrValue)
            idx++
        }

        // Parse the element's content until we reach either the end of the document or the end of the element.
        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
            if (xmlPullParser.eventType == XmlPullParser.START_TAG) {
                val childName = QName(
                    xmlPullParser.namespace,
                    xmlPullParser.name
                ) // store the child name before recursively parsing
                val childValue = parseElement(
                    childName,
                    model /*parent*/
                ) ?:continue// recursively assemble the child element
                model.parseField(childName.localPart, childValue) // parse the child element
            } else if (xmlPullParser.eventType == XmlPullParser.TEXT) {
                val text = xmlPullParser.text
                characters = appendText(text, characters) // accumulate the element's character data
            } else if (xmlPullParser.eventType == XmlPullParser.END_TAG) {
                if (xmlPullParser.name == name.localPart) {
                    if (characters != null) { // null if no character data encountered
                        model.parseText(characters.toString()) // parse the element's character data
                    }
                    break // reached the end of the element; stop parsing its content
                }
            }
        }
        return model
    }

    @Throws(XmlPullParserException::class, IOException::class)
    protected open fun parseText(name: QName): String {
        val xmlPullParser = xpp ?: return characters.toString()
        // Set up to accumulate the element's character data.
        characters.delete(0, characters.length)
        // Parse the element's content until we reach either the end of the document or the end of the element.
        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
            if (xmlPullParser.eventType == XmlPullParser.TEXT) {
                val text = xmlPullParser.text
                appendText(text, characters)
            } else if (xmlPullParser.eventType == XmlPullParser.END_TAG) {
                if (xmlPullParser.name == name.localPart) {
                    break // reached the end of the element; stop parsing its content
                }
            }
        }
        return characters.toString()
    }

    protected open fun appendText(
        text: String?,
        result: StringBuilder?
    ): StringBuilder? {
        var temptext = text
        if (temptext != null && temptext.isNotEmpty()) { // ignore empty text
            temptext = temptext.replace("\n".toRegex(), "").trim { it <= ' ' }
            // suppress newlines and leading/trailing whitespace
            if (temptext.isNotEmpty()) { // ignore whitespace
                return result?.append(text) ?: StringBuilder(temptext)
            }
        }
        return result
    }
}