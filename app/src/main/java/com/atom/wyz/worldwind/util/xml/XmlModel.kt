package com.atom.wyz.worldwind.util.xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

open class XmlModel {
    companion object{
         const val CHARACTERS_CONTENT = "CharactersContent"
    }

    var namespaceUri : String = ""

    protected var fields: HashMap<String, Any?>? = null

    var parent: XmlModel? = null


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


            // Update the namespace based on the WMS version
            if (attributeName.equals("version", ignoreCase = true)) {
                if (attributeValue.equals("1.3.0", ignoreCase = true)) {
                    this.namespaceUri = (XmlPullParserContext.DEFAULT_NAMESPACE)
                    ctx.namespaceUri = (XmlPullParserContext.DEFAULT_NAMESPACE)
                } else if (attributeValue.equals("1.1.1", ignoreCase = true)) {
                    this.namespaceUri = ("")
                    ctx.namespaceUri = ("")
                }
            }
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
                model.parent = this
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

    open fun setField(keyName: QName, value: Any?) {
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

    protected open fun getInheritedField(keyName: QName): Any? {
        var model: XmlModel? = this
        var value: Any? = null
        while (model != null && value == null) {
            value = model.getField(keyName)
            model = model.parent
        }
        return value
    }

    protected open fun <T> getAdditiveInheritedField(
        keyName: QName,
        values: MutableCollection<T>
    ): Collection<T>? {
        var model: XmlModel? = this
        var value: Any? = null
        while (model != null) {
            value = model.getField(keyName)
            if (value is Collection<*>) {
                values.addAll((value as Collection<T>))
            }
            model = model.parent
        }
        return values
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
    open fun getCharactersContent(): String? {
        return this.getField(CHARACTERS_CONTENT)?.toString()
    }

    open fun getChildCharacterValue(name: QName): String? {
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

    open fun getDoubleAttributeValue(name: QName ,  inherited:Boolean ): Double? {
        val o: Any? = if (inherited) {
            getInheritedField(name)
        } else {
            this.getField(name)
        }
        return if (o != null) {
            if (o is java.lang.StringBuilder) {
                try {
                    o.toString().toDouble()
                } catch (ignore: Exception) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    open fun getIntegerAttributeValue(name: QName ,  inherited:Boolean): Int? {
        val o: Any? = if (inherited) {
            getInheritedField(name)
        } else {
            this.getField(name)
        }
        return if (o != null) {
            if (o is java.lang.StringBuilder) {
                try {
                    o.toString().toInt()
                } catch (ignore: Exception) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    open fun getBooleanAttributeValue(name: QName,  inherited:Boolean): Boolean? {
        val o: Any? = if (inherited) {
            getInheritedField(name)
        } else {
            this.getField(name)
        }
        return if (o != null) {
            if (o is java.lang.StringBuilder) {
                try {
                    java.lang.Boolean.parseBoolean(o.toString())
                } catch (ignore: Exception) {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }
}