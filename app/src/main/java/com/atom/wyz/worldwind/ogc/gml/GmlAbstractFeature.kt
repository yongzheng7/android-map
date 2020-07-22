package com.atom.wyz.worldwind.ogc.gml

open class GmlAbstractFeature : GmlAbstractGml() {
    open var boundedBy: GmlBoundingShape? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "boundedBy" -> boundedBy = value as GmlBoundingShape
        }
    }
}