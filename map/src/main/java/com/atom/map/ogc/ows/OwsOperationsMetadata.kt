package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsOperationsMetadata : XmlModel() {

    protected var operations: MutableList<OwsOperation> =
        ArrayList()

    open fun getGetCapabilities(): OwsOperation? {
        for (operation in operations) {
            if (operation.name.equals("GetCapabilities")) {
                return operation
            }
        }
        return null
    }
    open fun getGetTile(): OwsOperation? {
        for (operation in operations) {
            if (operation.name.equals("GetTile")) {
                return operation
            }
        }
        return null
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Operation" -> {
                operations.add((value as OwsOperation))
            }
        }
    }
}