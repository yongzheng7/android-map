package com.atom.wyz.worldwind.ogc.wcs

import com.atom.wyz.worldwind.ogc.gml.GmlAbstractFeature
import com.atom.wyz.worldwind.ogc.gml.GmlDomainSet

open class Wcs201CoverageDescription  : GmlAbstractFeature() {
    open var coverageId: String? = null

    open var domainSet: GmlDomainSet? = null

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "CoverageId" -> coverageId = value as String
            "domainSet" -> domainSet = value as GmlDomainSet
        }
    }
}