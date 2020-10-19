package com.atom.map.ogc.wcs

import com.atom.map.util.xml.XmlModel
import java.util.*

open class Wcs201CoverageDescriptions : XmlModel() {
    open var coverageDescriptions: MutableList<Wcs201CoverageDescription> =
        ArrayList()

    open fun getCoverageDescription(identifier: String?): Wcs201CoverageDescription? {
        for (coverageDescription in coverageDescriptions) {
            if (coverageDescription.coverageId.equals(identifier)) {
                return coverageDescription
            }
        }
        return null
    }

    override fun parseField(keyName: String, value: Any) {
        super.parseField(keyName, value)
        when (keyName) {
            "CoverageDescription" -> coverageDescriptions.add((value as Wcs201CoverageDescription))
        }
    }
}