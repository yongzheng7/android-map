package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmsInfoUrl : XmlModel() {

    open var type: String? = null

    open var formats: MutableList<String> = ArrayList()

    open var url: String? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Format" -> {
                formats.add((value as String))
            }
            "OnlineResource" -> {
                url = (value as WmsOnlineResource).url
            }
            "type" -> {
                type = value as String
            }
        }
    }
}