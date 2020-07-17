package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel

open class WmsRequest : XmlModel() {

    open var getCapabilities: WmsRequestOperation? = null

    open var getMap: WmsRequestOperation? = null

    open var getFeatureInfo: WmsRequestOperation? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "GetCapabilities" -> {
                getCapabilities = value as WmsRequestOperation
            }
            "GetMap" -> {
                getMap = value as WmsRequestOperation
            }
            "GetFeatureInfo" -> {
                getFeatureInfo = value as WmsRequestOperation
            }
        }
    }
}