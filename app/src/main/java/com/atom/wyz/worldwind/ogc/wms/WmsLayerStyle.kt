package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsLayerStyle//    protected String name;
//    protected String title;
//    protected String styleAbstract;
//    protected WmsLayerInfoUrl styleSheetURL;
//    protected WmsLayerInfoUrl styleURL;
//    protected Set<WmsLogoUrl> legendURLs;
    (namespaceURI: String?) : XmlModel(namespaceURI) {
    lateinit var name: QName

    lateinit var title: QName

    lateinit var description: QName

    lateinit var legendUrl: QName

    lateinit var styleSheetUrl: QName

    lateinit var styleUrl: QName

    init {
        this.initialize()
    }

    private fun initialize() {
        name = QName(this.namespaceUri, "Name")
        title = QName(this.namespaceUri, "Title")
        description = QName(this.namespaceUri, "Abstract")
        legendUrl = QName(this.namespaceUri, "LegendURL")
        styleSheetUrl = QName(this.namespaceUri, "StyleSheetURL")
        styleUrl = QName(this.namespaceUri, "StyleURL")
    }

    //    @Override
    //    public XmlEventParser allocate(XmlEventParserContext ctx, XmlEvent event)
    //    {
    //        XmlEventParser defaultParser = null;
    //
    //        XmlEventParser parser = super.allocate(ctx, event);
    //        if (parser != null)
    //            return parser;
    //
    //        if (ctx.isStartElement(event, LEGEND_URL))
    //            defaultParser = new WmsLogoURL(this.getNamespaceURI());
    //        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
    //            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
    //        else if (ctx.isStartElement(event, STYLE_URL))
    //            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
    //
    //        return ctx.allocate(event, defaultParser);
    //    }
    //    @Override
    //    protected void doParseEventContent(XmlEventParserContext ctx, XmlEvent event, Object... args)
    //        throws XMLStreamException
    //    {
    //        if (ctx.isStartElement(event, TITLE))
    //        {
    //            String s = ctx.getStringParser().parseString(ctx, event);
    //            if (!WWUtil.isEmpty(s))
    //                this.setTitle(s);
    //        }
    //        else if (ctx.isStartElement(event, NAME))
    //        {
    //            String s = ctx.getStringParser().parseString(ctx, event);
    //            if (!WWUtil.isEmpty(s))
    //                this.setName(s);
    //        }
    //        else if (ctx.isStartElement(event, ABSTRACT))
    //        {
    //            String s = ctx.getStringParser().parseString(ctx, event);
    //            if (!WWUtil.isEmpty(s))
    //                this.setStyleAbstract(s);
    //        }
    //        else if (ctx.isStartElement(event, LEGEND_URL))
    //        {
    //            XmlEventParser parser = this.allocate(ctx, event);
    //            if (parser != null)
    //            {
    //                Object o = parser.parse(ctx, event, args);
    //                if (o != null && o instanceof WmsLogoURL)
    //                    this.addLegendURL((WmsLogoURL) o);
    //            }
    //        }
    //        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
    //        {
    //            XmlEventParser parser = this.allocate(ctx, event);
    //            if (parser != null)
    //            {
    //                Object o = parser.parse(ctx, event, args);
    //                if (o != null && o instanceof WmsLayerInfoURL)
    //                    this.setStyleSheetURL((WmsLayerInfoURL) o);
    //            }
    //        }
    //        else if (ctx.isStartElement(event, STYLE_URL))
    //        {
    //            XmlEventParser parser = this.allocate(ctx, event);
    //            if (parser != null)
    //            {
    //                Object o = parser.parse(ctx, event, args);
    //                if (o != null && o instanceof WmsLayerInfoURL)
    //                    this.setStyleURL((WmsLayerInfoURL) o);
    //            }
    //        }
    //    }
    fun getName(): String? {
        return getChildCharacterValue(name)
    }

    protected fun setName(name: String?) {
        setChildCharacterValue(this.name, name)
    }

    fun getTitle(): String? {
        return getChildCharacterValue(title)
    }

    protected fun setTitle(title: String?) {
        setChildCharacterValue(this.title, title)
    }

    fun getStyleAbstract(): String? {
        return getChildCharacterValue(description)
    }

    protected fun setStyleAbstract(styleAbstract: String?) {
        setChildCharacterValue(description, styleAbstract)
    }

    fun getStyleSheetUrl(): WmsLayerInfoUrl? {
        return this.getField(styleSheetUrl) as WmsLayerInfoUrl?
    }

    protected fun setStyleSheetUrl(styleSheetUrl: WmsLayerInfoUrl?) {
        this.setField(this.styleSheetUrl, styleSheetUrl)
    }

    fun getStyleUrl(): WmsLayerInfoUrl? {
        return this.getField(styleUrl) as WmsLayerInfoUrl?
    }

    protected fun setStyleUrl(styleUrl: WmsLayerInfoUrl?) {
        this.setField(this.styleUrl, styleUrl)
    }

    fun getLegendUrls(): Set<WmsLogoUrl?>? {
        return this.getField(legendUrl) as Set<WmsLogoUrl?>?
    }

    /**
     * Sets the WmsLogoUrls associated with this layer style. This method will replace any existing legend urls.
     *
     * @param legendUrls
     */
    protected fun setLegendUrls(legendUrls: Set<WmsLogoUrl?>?) {
        var logoUrls = this.getField(legendUrl) as MutableSet<WmsLogoUrl?>?
        if (logoUrls != null) {
            logoUrls.clear()
            logoUrls.addAll(legendUrls!!)
        } else {
            logoUrls = HashSet()
            logoUrls.addAll(legendUrls!!)
            super.setField(legendUrl, logoUrls)
        }
    }

    protected fun addLegendUrl(url: WmsLogoUrl?) {
        this.setField(legendUrl, url)
    }

    override fun setField(keyName: QName, value: Any?) {
        // Since we have a list, need to check for multiple values of the WmsLogoUrl
        if (keyName.localPart == legendUrl.localPart && keyName.namespaceURI == legendUrl.namespaceURI) {
            var logoUrls =
                this.getField(legendUrl) as MutableSet<WmsLogoUrl?>?
            if (logoUrls == null) {
                super.setField(legendUrl, HashSet<WmsLogoUrl>())
                logoUrls =
                    super.getField(legendUrl) as MutableSet<WmsLogoUrl?>?
            }
            if (value is WmsLogoUrl) {
                logoUrls!!.add(value as WmsLogoUrl?)
                return
            }
        }
        super.setField(keyName, value)
    }
}