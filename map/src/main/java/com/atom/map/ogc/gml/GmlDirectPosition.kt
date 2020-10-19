package com.atom.map.ogc.gml

open class GmlDirectPosition : GmlDoubleList() {

    open var srsName: String? = null

    open var srsDimension: String? = null

    open var axisLabels = mutableListOf<String>()

    open var uomLabels = mutableListOf<String>()

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
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