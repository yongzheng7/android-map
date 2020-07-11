package com.atom.wyz.worldwind.util.xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

open class XmlModel {
    companion object{
         const val CHARACTERS_CONTENT = "CharactersContent"
    }

    protected var namespaceUri : String = ""

    protected var fields: HashMap<String, Any?>? = null

    protected var parent: XmlModel? = null


    constructor (namespaceUri: String?) {
        namespaceUri?.let {
            this.namespaceUri = it
        }
    }


    @Throws(XmlPullParserException::class, IOException::class)
    open fun read(ctx: XmlPullParserContext): Any? {

        val xpp: XmlPullParser = ctx.parser ?: return null
        if (xpp.eventType == XmlPullParser.START_DOCUMENT) {
            xpp.next()
        }
        this.doParseEventAttributes(ctx)
        // eliminated the symbol table and the exception call

        // Capture the start element name
        val startElementName = xpp.name
        while (xpp.next() != XmlPullParser.END_DOCUMENT) {
            if (xpp.eventType == XmlPullParser.END_TAG && xpp.name != null && xpp.name == startElementName
            ) {
                return this
            }
            if (xpp.eventType == XmlPullParser.TEXT) {
                this.doAddCharacters(ctx)
            } else {
                this.doParseEventContent(ctx)
            }
        }
        return null
    }
    @Throws(XmlPullParserException::class, IOException::class)
    protected open fun doParseEventAttributes(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        val attributeCount = xpp.attributeCount
        var attributeName: String?
        var attributeValue: String?
        for (i in 0 until attributeCount) {
            attributeName = xpp.getAttributeName(i)
            attributeValue = xpp.getAttributeValue(i)
            setField(attributeName, attributeValue)
        }
    }
    protected fun doAddCharacters(ctx: XmlPullParserContext) {
        var s: String = ctx.parser?.getText() ?.also { if(it.isEmpty()) return } ?: return
        s = s.replace("\n".toRegex(), "").trim { it <= ' ' }
        val sb = this.getField(CHARACTERS_CONTENT) as StringBuilder?
        sb?.append(s) ?: this.setField(CHARACTERS_CONTENT, StringBuilder(s))
    }

    @Throws(XmlPullParserException::class, IOException::class)
    open protected fun doParseEventContent(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        if (xpp.eventType == XmlPullParser.START_TAG) {
            val qName = QName(xpp.namespace, xpp.name)
            var model = ctx.createParsableModel(qName)

            if (model == null) {
                model = ctx.getUnrecognizedElementModel()
                model ?.let{
                    ctx.registerParsableModel(qName, it)
                }
            }

            if (model != null) {
                val o = model.read(ctx)
                if (o == null) {
                    return
                } else {
                    doAddEventContent(o, ctx)
                }
            }
        }
    }

    protected fun doAddEventContent(o: Any?, ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?:return
        this.setField(QName(xpp.namespace, xpp.name), o)
    }

    fun setField(keyName: QName, value: Any?) {
        this.setField(keyName.localPart, value)
    }

    fun setField(keyName: String, value: Any?) {
        if (fields == null) fields = hashMapOf()
        fields ?.put(keyName , value)
    }

    fun setFields(newFields: Map<String, Any?>) {
        if (fields == null) fields = hashMapOf()
        for ((key, value) in newFields) {
            this.setField(key, value)
        }
    }

    fun getField(keyName: QName): Any? {
        return this.getField(keyName.localPart)
    }

    fun getField(keyName: String): Any? {
        return fields ?.get(keyName)
    }

    fun hasField(keyName: QName): Boolean {
        return this.hasField(keyName.localPart)
    }

    fun hasField(keyName: String): Boolean {
        return fields != null && fields!!.containsKey(keyName)
    }

    fun removeField(keyName: String) {
        fields?.remove(keyName)
    }

    fun hasFields(): Boolean {
        return fields != null
    }

    fun getFields(): Map<String, Any?>? {
        return fields
    }

    protected open fun getChildCharacterValue(name: QName): String? {
        val model = this.getField(name) as XmlModel?
        if (model != null) {
            val o = model.getField(CHARACTERS_CONTENT)
            if (o != null) {
                return o.toString()
            }
        }
        return null
    }

    protected open fun setChildCharacterValue(
        name: QName,
        value: String?
    ) {
        var model = this.getField(name) as XmlModel?
        if (model != null) {
            model.setField(CHARACTERS_CONTENT, value)
        } else {
            model = XmlModel(this.namespaceUri)
            model.setField(CHARACTERS_CONTENT, value)
            this.setField(name, model)
        }
    }
}