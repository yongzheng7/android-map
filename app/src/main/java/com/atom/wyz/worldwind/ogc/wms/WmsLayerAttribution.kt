package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

class WmsLayerAttribution : XmlModel {

    protected var TITLE: QName? = null

    protected var ONLINE_RESOURCE: QName? = null

    protected var LOGO_URL: QName? = null

    protected var title: String? = null

    protected var onlineResource: WmsOnlineResource? = null

    protected var logoURL: WmsLogoUrl? = null

    constructor(namespaceURI: String?) : super(namespaceURI) {
        this.initialize()
    }

    private fun initialize() {
        TITLE = QName(this.namespaceUri, "Title")
        ONLINE_RESOURCE = QName(this.namespaceUri, "OnlineResource")
        LOGO_URL = QName(this.namespaceUri, "LogoURL")
    }

    // TODO remove once thoroughly tested
    // This is the first unique use of allocate observed. The object type return should now be captured by the
    // XmlPullParserContext registry. Retaining in case the behavior isn't as expected.
    //    @Override
    //    public XmlEventParser allocate(XmlEventParserContext ctx, XmlEvent event)
    //    {
    //        XmlEventParser defaultParser = null;
    //
    //        if (ctx.isStartElement(event, ONLINE_RESOURCE))
    //            defaultParser = new OGCOnlineResource(this.getNamespaceURI());
    //        else if (ctx.isStartElement(event, LOGO_URL))
    //            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
    //
    //        return ctx.allocate(event, defaultParser);
    //    }
    @Throws(XmlPullParserException::class, IOException::class)
    override fun doParseEventContent(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        if (ctx.isStartElement(TITLE!!)) {
            this.title = (xpp.name.trim { it <= ' ' })
        } else if (ctx.isStartElement(ONLINE_RESOURCE!!)) {
            val model: XmlModel? = ctx.createParsableModel(ONLINE_RESOURCE!!)
            if (model != null) {
                val o = model.read(ctx)
                if (o != null) {
                    this.onlineResource = (o as WmsOnlineResource?)
                }
            }
        } else if (ctx.isStartElement(LOGO_URL!!)) {
            val model: XmlModel? = ctx.createParsableModel(LOGO_URL!!)
            if (model != null) {
                val o = model.read(ctx)
                if (o != null) {
                    this.logoURL = (o as WmsLogoUrl?)
                }
            }
        }
    }
}