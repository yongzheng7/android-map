package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmsService : XmlModel() {

    open var name: String? = null

    open var title: String? = null

    open var description: String? = null

    open var fees: String? = null

    open var accessConstraints: String? = null

    open var keywordList: MutableList<String> =
        ArrayList()

    open var url: String? = null

    open var contactInformation: WmsContactInformation? = null

    open var maxWidth: Int? = null

    open var maxHeight: Int? = null

    open var layerLimit: Int? = null

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Name" -> {
                name = value as String
            }
            "Title" -> {
                title = value as String
            }
            "Abstract" -> {
                description = value as String
            }
            "KeywordList" -> {
                keywordList.addAll((value as WmsKeywords).keywords)
            }
            "OnlineResource" -> {
                url = (value as WmsOnlineResource).url
            }
            "ContactInformation" -> {
                contactInformation = value as WmsContactInformation
            }
            "Fees" -> {
                fees = value as String
            }
            "AccessConstraints" -> {
                accessConstraints = value as String
            }
            "MaxWidth" -> {
                maxWidth = (value as String).toInt()
            }
            "MaxHeight" -> {
                maxHeight = (value as String).toInt()
            }
            "LayerLimit" -> {
                layerLimit = (value as String).toInt()
            }
        }
    }
}