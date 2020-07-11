package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

class WmsOnlineResource : XmlModel {
    companion object {
        const val DEFAULT_NAMESPACE = "http://www.w3.org/1999/xlink"

        var href =
            QName(DEFAULT_NAMESPACE, "href")

        var type =
            QName(DEFAULT_NAMESPACE, "type")
    }


    constructor() : super(XmlPullParserContext.DEFAULT_NAMESPACE)

    constructor(namespaceURI: String?) : super(
        namespaceURI ?: XmlPullParserContext.DEFAULT_NAMESPACE
    )

    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventAttributes(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        this.setType(xpp.getAttributeValue(type.namespaceURI, "type"))
        this.setHref(xpp.getAttributeValue(href.namespaceURI, "href"))
    }

    fun getType(): String? {
        return this.getField(type) as String?
    }

    fun setType(type: String?) {
        this.setField(Companion.type, type)
    }

    fun getHref(): String? {
        return this.getField(href) as String?
    }

    fun setHref(href: String?) {
        this.setField(Companion.href, href)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("href: ").append(if (getHref() != null) getHref() else "null")
        sb.append(", type: ").append(if (getType() != null) getType() else "null")
        return sb.toString()
    }
}