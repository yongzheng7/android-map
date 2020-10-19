package com.atom.map.ogc.wms

import java.util.*

open class WmsAuthorityUrl() : WmsInfoUrl() {

    open var name: String? = null

    override var type: String? = null

    override var formats: MutableList<String> = ArrayList()

    override var url: String? = null

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
            "name" -> {
                name = value as String
            }
        }
    }

}