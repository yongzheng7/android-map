package com.atom.map.ogc.wms

import com.atom.map.util.xml.XmlModel

open class WmsDcpType() : XmlModel() {

    open var get: WmsOnlineResource? = null

    open var post: WmsOnlineResource? = null

    fun getGetHref(): String? {
        return this.get?.url
    }

    fun getPostHref(): String? {
        return this.post?.url
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "HTTP" -> {
                val http = value as WmsDcpHttp
                get = if (http.get != null) http.get!!.onlineResource else null
                post = if (http.post != null) http.post!!.onlineResource else null
            }
        }
    }

     open class WmsDcpHttp : XmlModel() {
        open var get: WmsDcpHttpProtocol? = null
        open var post: WmsDcpHttpProtocol? = null

        override fun parseField(keyName: String, value: Any) {
            when (keyName) {
                "Get" -> {
                    get = value as WmsDcpHttpProtocol
                }
                "Post" -> {
                    post = value as WmsDcpHttpProtocol
                }
            }
        }
    }

     open class WmsDcpHttpProtocol : XmlModel() {
        open var onlineResource: WmsOnlineResource? = null
        override fun parseField(keyName: String, value: Any) {
            when (keyName) {
                "OnlineResource" -> {
                    onlineResource = value as WmsOnlineResource
                }
            }
        }
    }
}