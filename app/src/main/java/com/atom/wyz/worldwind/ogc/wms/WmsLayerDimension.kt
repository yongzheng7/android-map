package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import javax.xml.namespace.QName

class WmsLayerDimension(defaultNamespaceUri: String?) : XmlModel(defaultNamespaceUri) {
    lateinit var name: QName

    lateinit var units: QName

    lateinit var unitSymbol: QName

    lateinit var defaultValue: QName

    lateinit var multipleValues: QName

    lateinit var nearestValue: QName

    lateinit var current: QName

    init {
        initialize()
    }

    protected fun initialize() {
        name = QName("", "name")
        units = QName("", "units")
        unitSymbol = QName("", "unitSymbol")
        defaultValue = QName("", "default")
        multipleValues = QName("", "multipleValues")
        nearestValue = QName("", "nearestValue")
        current = QName("", "current")
    }

    fun getName(): String? {
        val o = this.getField(name)
        return o?.toString()
    }

    fun getUnits(): String? {
        val o = this.getField(units)
        return o?.toString()
    }

    fun getUnitSymbol(): String? {
        val o = this.getField(unitSymbol)
        return o?.toString()
    }

    fun getDefaultValue(): String? {
        val o = this.getField(defaultValue)
        return o?.toString()
    }

    fun getMultipleValues(): Boolean? {
        return getBooleanAttributeValue(multipleValues, false)
    }

    fun getNearestValue(): Boolean? {
        return getBooleanAttributeValue(nearestValue, false)
    }

    fun getCurrent(): Boolean? {
        return getBooleanAttributeValue(current, false)
    }
}