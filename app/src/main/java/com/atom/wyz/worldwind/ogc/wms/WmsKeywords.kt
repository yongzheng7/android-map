package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsKeywords(namespaceUri: String?) : XmlModel(namespaceUri) {
    lateinit var keywords: QName

    init {
        initialize()
    }

    protected fun initialize() {
        keywords = QName(this.namespaceUri, "Keyword")
    }
    fun getKeywords(): Set<String>? {
        return this.getField(keywords) as Set<String>?
    }

    override fun setField(keyName: QName, value: Any?) {
        if (keyName == keywords) {
            var keywords =
                this.getField(keywords) as MutableSet<String>?
            if (keywords == null) {
                keywords = HashSet()
                super.setField(this.keywords, keywords)
            }
            if (value is XmlModel) {
                val o = value.getField(CHARACTERS_CONTENT)
                if (o != null) {
                    keywords.add(o.toString())
                } else {
                    super.setField(keyName, value)
                }
            } else {
                super.setField(keyName, value)
            }
        } else {
            super.setField(keyName, value)
        }
    }
}