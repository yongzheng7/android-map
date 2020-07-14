package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import com.atom.wyz.worldwind.util.xml.XmlPullParserContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*
import javax.xml.namespace.QName

class WmsRequestDescription : XmlModel {

    lateinit var format: QName
    lateinit var dcpType: QName

    protected var requestName: String? = null

    constructor(namespaceURI: String?):  super(namespaceURI) {
        initialize()
    }

    private fun initialize() {
        format = QName(this.namespaceUri, "Format")
        dcpType = QName(this.namespaceUri, "DCPType")
    }

    @Throws(XmlPullParserException::class, IOException::class)
   override fun read(ctx: XmlPullParserContext): Any? {
        // Use the name of the element to define the name of this description
        this.requestName = ctx.parser?.name
        return super.read(ctx)
    }

    fun getOnlineResouce(
        requestMethod: String?
    ): WmsOnlineResource? {
        for (dct in this.getDcpTypes()!!) {
            for (dcpInfo in dct.getDCPInfos()) {
                if ( dcpInfo.method.equals(requestMethod)) {
                    return dcpInfo.onlineResource
                }
            }
        }
        return null
    }
    fun getFormats(): Set<String>? {
        return super.getField(format) as Set<String>?
    }


    protected fun setFormats(formats: Set<String>?) {
        var currentFormats =
            super.getField(format) as MutableSet<String>?
        if (currentFormats == null) {
            currentFormats = HashSet()
            super.setField(format, currentFormats)
        }
        currentFormats.clear()
        currentFormats.addAll(formats!!)
    }

    protected fun addFormat(format: String?) {
        this.setField(this.format, format)
    }

    protected fun setDCPTypes(dcTypes: Set<WmsDcpType>?) {
        var currentDcpTypes =
            super.getField(dcpType) as MutableSet<WmsDcpType>?
        if (currentDcpTypes == null) {
            currentDcpTypes = HashSet()
            super.setField(dcpType, currentDcpTypes)
        }
        currentDcpTypes.clear()
        currentDcpTypes.addAll(dcTypes!!)
    }

    fun getDcpTypes(): Set<WmsDcpType>? {
        return getField(dcpType) as Set<WmsDcpType>?
    }

    fun addDcpType(dct: WmsDcpType?) {
        this.setField(dcpType, dct)
    }

    override fun setField(keyName: QName, value: Any?) {

        // Check if this is a format element
        if (keyName == format) {
            // Formats are stored as a set
            var formats =
                super.getField(keyName) as MutableSet<String>?
            if (formats == null) {
                formats = HashSet()
                super.setField(keyName, formats)
            }
            if (value is String) {
                formats.add(value)
            } else if (value is XmlModel) {
                formats.add(value.getField(CHARACTERS_CONTENT).toString())
            } else {
                super.setField(keyName, value)
            }
        } else if (keyName == dcpType) {
            // DCP Types are stored as a set
            var dcpTypes =
                super.getField(dcpType) as MutableSet<WmsDcpType?>?
            if (dcpTypes == null) {
                dcpTypes = HashSet()
                super.setField(keyName, dcpTypes)
            }
            if (value is WmsDcpType) {
                dcpTypes.add(value as WmsDcpType?)
            } else {
                super.setField(keyName, value)
            }
        } else {
            super.setField(keyName, value)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (requestName != null) sb.append(requestName).append("\n")
        sb.append("\tFormats: ")
        for (format in getFormats()!!) {
            sb.append("\t").append(format).append(", ")
        }
        sb.append("\n\tDCPTypes:\n")
        for (dcpt in getDcpTypes()!!) {
            sb.append("\t\t").append(dcpt.toString()).append("\n")
        }
        return sb.toString()
    }
}