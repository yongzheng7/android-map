package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmsLogoUrl : XmlModel() {
    open var formats: MutableSet<String> = LinkedHashSet()

    open var url: String? = null

    open var width: Int? = null

    open var height: Int? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Format" -> {
                formats.add((value as String))
            }
            "OnlineResource" -> {
                url = (value as WmsOnlineResource).url
            }
            "width" -> {
                width = (value as String).toInt()
            }
            "height" -> {
                height = (value as String).toInt()
            }
        }
    }
}