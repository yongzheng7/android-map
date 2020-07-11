package com.atom.wyz.worldwind.ogc

import android.text.TextUtils
import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.xml.namespace.QName

class WmsBoundingBox : XmlModel {
    companion object{
        protected var BOUNDING_BOX_ATTRIBUTE_NS = ""

        protected var CRS =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "CRS")

        protected var SRS =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "SRS")

        protected var MINX =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "minx")

        protected var MINY =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "miny")

        protected var MAXX =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "maxx")

        protected var MAXY =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "maxy")

        protected var RESX =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "resx")

        protected var RESY =
            QName(BOUNDING_BOX_ATTRIBUTE_NS, "resy")

        fun createFromStrings(
            crs: String?,
            minx: String,
            maxx: String,
            miny: String,
            maxy: String,
            resx: String?,
            resy: String?
        ): WmsBoundingBox? {
            val bbox = WmsBoundingBox(null)
            try {
                bbox.crs = crs
                bbox.minx = minx.toDouble()
                bbox.maxx = maxx.toDouble()
                bbox.miny = miny.toDouble()
                bbox.maxy = maxy.toDouble()
                bbox.resx = if (!TextUtils.isEmpty(resx)) resx!!.toDouble() else 0.0
                bbox.resy = if (!TextUtils.isEmpty(resy)) resy!!.toDouble() else 0.0
            } catch (e: NumberFormatException) {
                // TODO log error and handle
            }
            return bbox
        }
    }

    protected var crs: String? = null

    protected var minx = 0.0

    protected var maxx = 0.0

    protected var miny = 0.0

    protected var maxy = 0.0

    protected var resx = 0.0

    protected var resy = 0.0

    constructor(namespaceURI: String?) :super(namespaceURI)

    @Throws(XmlPullParserException::class, IOException::class)
    protected override fun doParseEventAttributes(ctx: XmlPullParserContext) {
        val xpp: XmlPullParser = ctx.parser ?: return
        var referenceSystem = xpp.getAttributeValue(
            CRS.namespaceURI,
            CRS.localPart
        )
        if (referenceSystem == null) {
            referenceSystem = xpp.getAttributeValue(
                SRS.namespaceURI,
                SRS.localPart
            )
        }
        if (referenceSystem != null && !referenceSystem.isEmpty()) {
            this.crs = (referenceSystem)
        }
        var attrValue = xpp.getAttributeValue(
            MINX.namespaceURI,
            MINX.localPart
        )
        var value: Double? = this.parseDouble(attrValue)
        if (value != null) {
            this.minx = (value)
        }
        attrValue = xpp.getAttributeValue(
            MINY.namespaceURI,
            MINY.localPart
        )
        value = this.parseDouble(attrValue)
        if (value != null) {
            this.miny = (value)
        }
        attrValue = xpp.getAttributeValue(
            MAXX.namespaceURI,
            MAXX.localPart
        )
        value = this.parseDouble(attrValue)
        if (value != null) {
            this.maxx = (value)
        }
        attrValue = xpp.getAttributeValue(
            MAXY.namespaceURI,
            MAXY.localPart
        )
        value = this.parseDouble(attrValue)
        if (value != null) {
            this.maxy = (value)
        }
        attrValue = xpp.getAttributeValue(
            RESX.namespaceURI,
            RESX.localPart
        )
        value = this.parseDouble(attrValue)
        if (value != null) {
            this.resx  =(value)
        }
        attrValue = xpp.getAttributeValue(
            RESY.namespaceURI,
            RESY.localPart
        )
        value = this.parseDouble(attrValue)
        if (value != null) {
            this.resy = (value)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(crs)
        sb.append(": minx = ")
        sb.append(minx)
        sb.append(" miny = ")
        sb.append(miny)
        sb.append(" maxx = ")
        sb.append(maxx)
        sb.append(" maxy = ")
        sb.append(maxy)
        sb.append(" resx = ")
        sb.append(resx)
        sb.append(" resy = ")
        sb.append(resy)
        return sb.toString()
    }

    protected fun parseDouble(value: String?): Double? {
        var parsedValue: Double? = null
        if (value != null && !value.isEmpty()) {
            try {
                parsedValue = value.toDouble()
            } catch (e: java.lang.NumberFormatException) {
                // TODO log and handle
            }
        }
        return parsedValue
    }
}