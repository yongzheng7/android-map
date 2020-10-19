package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel
import java.util.*

open class WmsStyle : XmlModel() {
    open var name: String? = null

    open var title: String? = null

    open var description: String? = null

    open var legendUrl: MutableList<WmsLogoUrl> = ArrayList()

    open var styleSheetUrl: WmsInfoUrl? = null

    open var styleUrl: WmsInfoUrl? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Name" -> {
                name = value as String
            }
            "Title" -> {
                title = value as String
            }
            "Abstract" -> {
                description = value as String
            }
            "LegendURL" -> {
                legendUrl.add((value as WmsLogoUrl))
            }
            "StyleSheetURL" -> {
                styleSheetUrl = value as WmsInfoUrl
            }
            "StyleURL" -> {
                styleUrl = value as WmsInfoUrl
            }
        }
    }

}