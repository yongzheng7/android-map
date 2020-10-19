package com.atom.map.ogc.gml

import com.atom.map.util.xml.XmlModel

open class GmlEnvelope : XmlModel() {

    open var id: String? = null

    open var lowerCorner: GmlDirectPosition? = null

    open var upperCorner: GmlDirectPosition? = null

    open var srsName: String? = null

    open var srsDimension: String? = null

    open var axisLabels : MutableList<String> = mutableListOf()

    open var uomLabels : MutableList<String> = mutableListOf()

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "lowerCorner" -> lowerCorner = value as GmlDirectPosition
            "upperCorner" -> upperCorner = value as GmlDirectPosition
            "srsName" -> srsName = value as String
            "srsDimension" -> srsDimension = value as String
            "axisLabels" -> axisLabels = mutableListOf(
                *value.toString().split(" ".toRegex()).toTypedArray()
            )
            "uomLabels" -> uomLabels = mutableListOf(
                *value.toString().split(" ".toRegex()).toTypedArray()
            )
        }
    }
}