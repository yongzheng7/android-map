package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsLogoUrl(namespaceURI: String?) : XmlModel(namespaceURI) {
    lateinit var format: QName

    lateinit var onlineResource: QName

    var width = QName("", "width")

    var height = QName("", "height")

    init {
        initialize()
    }
    protected fun initialize() {
        format = QName(this.namespaceUri, "Format")
        onlineResource = QName(this.namespaceUri, "OnlineResource")
    }

    override fun setField(keyName: QName, value: Any?) {
        if (keyName == format) {
            var formats = this.getField(format) as MutableSet<String?>?
            if (formats == null) {
                formats = HashSet()
                super.setField(format, formats)
            }
            if (value is XmlModel) {
                formats.add(value.getCharactersContent())
                return
            }
        }
        super.setField(keyName, value)
    }

    fun getWidth(): Int? {
        return getIntegerAttributeValue(width, false)
    }

    fun getHeight(): Int? {
        return getIntegerAttributeValue(height, false)
    }

    fun getFormats(): Set<String?>? {
        val o = this.getField(format)
        return if (o is Set<*>) {
            o as Set<String?>?
        } else {
            emptySet<String>()
        }
    }

    fun getOnlineResource(): WmsOnlineResource? {
        val o = this.getField(onlineResource)
        return if (o is WmsOnlineResource) {
            o
        } else {
            null
        }
    }
}