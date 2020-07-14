package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class WmsAuthorityUrl : WmsLayerInfoUrl {

    protected var authority: String? = null

    constructor(namespaceURI: String?) : super(namespaceURI)

    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventAttributes(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        val authorityValue =
            xpp.getAttributeValue(this.namespaceUri, "authority")
        if (authorityValue != null && !authorityValue.isEmpty()) {
            this.authority  = (authorityValue.trim { it <= ' ' })
        }
    }

}