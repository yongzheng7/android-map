package com.atom.map.ogc.ows

import com.atom.map.util.xml.XmlModel
import java.util.*

open class OwsHttp : XmlModel() {

    open var get: MutableList<OwsHttpMethod> = ArrayList<OwsHttpMethod>()

    open var post: MutableList<OwsHttpMethod> = ArrayList<OwsHttpMethod>()

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Get" -> {
                get.add(value as OwsHttpMethod)
            }
            "Post" -> {
                post.add(value as OwsHttpMethod)
            }
        }
    }
}