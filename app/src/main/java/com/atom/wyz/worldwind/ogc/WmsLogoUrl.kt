package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class WmsLogoUrl : XmlModel {
    protected var width: Int? = null
    protected var height: Int? = null
    constructor(namespaceURI: String?) : super(namespaceURI)

    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventAttributes(ctx: XmlPullParserContext) {
        super.doParseEventAttributes(ctx)
        val xpp: XmlPullParser = ctx.parser ?: return
        try {
            val width = xpp.getAttributeValue("", "width").toInt()
            this.width = (width)
        } catch (e: NumberFormatException) {
            // TODO log the exception
        }
        try {
            val height = xpp.getAttributeValue("", "height").toInt()
            this.height = (height)
        } catch (e: NumberFormatException) {
            // TODO log the exception
        }
    }
}