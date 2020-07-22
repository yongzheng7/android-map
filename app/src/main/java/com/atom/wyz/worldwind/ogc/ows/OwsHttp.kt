package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel
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