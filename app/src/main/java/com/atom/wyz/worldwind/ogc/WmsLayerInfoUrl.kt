package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

open class WmsLayerInfoUrl : XmlModel {

    lateinit var FORMAT: QName
    lateinit var ONLINE_RESOURCE: QName

    protected var onlineResource: WmsOnlineResource? = null
    protected var name: String? = null
    protected var format: String? = null

    constructor(namespaceUri: String?) : super(namespaceUri) {
        this.initialize()
    }

    private fun initialize() {
        FORMAT = QName(this.namespaceUri, "Format")
        ONLINE_RESOURCE = QName(this.namespaceUri, "OnlineResource")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventContent(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        if (xpp.eventType == XmlPullParser.START_TAG) {
            val event = QName(xpp.namespace, xpp.name)
            if (event == FORMAT) {
                if (xpp.next() == XmlPullParser.TEXT) {
                    this.format = (xpp.text.trim { it <= ' ' })
                }
            } else if (event == ONLINE_RESOURCE) {
                val elementModel: XmlModel = ctx.createParsableModel(ONLINE_RESOURCE) ?: return
                val o = elementModel.read(ctx)
                if (o != null && o is WmsOnlineResource) {
                    this.onlineResource = o
                }
            }
        }
    }
    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventAttributes(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        val nameValue = xpp.getAttributeValue(this.namespaceUri, "name")
        if (nameValue != null && !nameValue.isEmpty()) {
            this.name = (nameValue)
        }
    }

}