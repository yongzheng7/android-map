package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.DoubleModel
import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsGeographicBoundingBox(namespaceUri: String?) : XmlModel(namespaceUri) {
    lateinit var west: QName

    lateinit var east: QName

    lateinit var north: QName

    lateinit var south: QName

    lateinit var minx: QName

    lateinit var miny: QName

    lateinit var maxx: QName

    lateinit var maxy: QName

    init {
        initialize()
    }

    protected fun initialize() {
        west = QName(this.namespaceUri, "westBoundLongitude")
        east = QName(this.namespaceUri, "eastBoundLongitude")
        north = QName(this.namespaceUri, "northBoundLatitude")
        south = QName(this.namespaceUri, "southBoundLatitude")
        minx = QName("", "minx")
        miny = QName("", "miny")
        maxx = QName("", "maxx")
        maxy = QName("", "maxy")
    }

    protected fun getValue(name: QName): Double? {
        return (getField(name) as DoubleModel?)?.getValue()
    }

    fun getWestBound(): Double? {
        // Default to handling the 1.3.0 style
        var value: Double? = this.getParsedDoubleElementValue(west)
        if (value == null) {
            // try the 1.1.1 style
            value = this.getParsedDoubleAttributeValue(minx)
        }

        return value
    }

    fun getEastBound(): Double? {
        var value: Double? = this.getParsedDoubleElementValue(east)
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(maxx)
        }

        return value
    }

    fun getNorthBound(): Double? {
        var value: Double? = this.getParsedDoubleElementValue(north)
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(maxy)
        }

        return value
    }

    fun getSouthBound(): Double? {
        var value: Double? = this.getParsedDoubleElementValue(south)
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(miny)
        }

        return value
    }

    private fun getParsedDoubleElementValue(name: QName): Double? {
        val textValue = getChildCharacterValue(name)
        if (textValue != null && !textValue.isEmpty()) {
            try {
                return textValue.toDouble()
            } catch (ignore: NumberFormatException) {
            }
        }
        return null
    }
     private fun getParsedDoubleAttributeValue(name: QName?): Double? {
        val o = this.getField(name!!)
        if (o != null) {
            try {
                return o.toString().toDouble()
            } catch (ignore: java.lang.NumberFormatException) {
            }
        }
        return null
    }

}