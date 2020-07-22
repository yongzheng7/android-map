package com.atom.wyz.worldwind.ogc.gml

import java.util.*

open class GmlGrid : GmlAbstractGeometry() {

    open var limits: GmlGridLimits? = null

    open var axisNames: MutableList<String> =
        ArrayList()

    open var dimension: String? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "limits" -> limits = value as GmlGridLimits
            "axisName" -> axisNames.add((value as String))
            "dimension" -> dimension = value as String
        }
    }
}