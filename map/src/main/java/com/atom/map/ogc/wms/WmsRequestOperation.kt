package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmsRequestOperation : XmlModel() {
    open var name: String? = null

    open var formats: MutableList<String> = ArrayList()

    open var getUrl: String? = null

    open var postUrl: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "name" -> {
                name = value as String
            }
            "Format" -> {
                formats.add((value as String))
            }
            "DCPType" -> {
                val dcpType = value as WmsDcpType
                getUrl = dcpType.getGetHref()
                postUrl = dcpType.getPostHref()
            }
        }
    }
}