package com.atom.wyz.worldwind.ogc.gml

open class GmlPoint : GmlAbstractGeometricPrimitive() {

    open var pos: GmlDirectPosition? = null
    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "pos" -> pos = value as GmlDirectPosition
        }
    }
}