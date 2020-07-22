package com.atom.wyz.worldwind.ogc.ows

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class OwsDcp : XmlModel() {

    open var getMethod: MutableList<OwsHttpMethod> =
        ArrayList<OwsHttpMethod>()

    open var postMethod: MutableList<OwsHttpMethod> =
        ArrayList<OwsHttpMethod>()

    override fun parseField(keyName: String, value: Any) {
        if (keyName == "HTTP") {
            val http: OwsHttp = value as OwsHttp
            getMethod.addAll(http.get)
            postMethod.addAll(http.post)
        }
    }
}