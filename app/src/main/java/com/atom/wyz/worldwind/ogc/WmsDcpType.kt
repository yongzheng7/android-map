package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.util.xml.NameStringModel
import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*
import javax.xml.namespace.QName

class WmsDcpType : XmlModel {

    lateinit var get: QName

    lateinit var post: QName

    lateinit var http: QName

    lateinit var onlineResource: QName

    class DcpInfo(var protocol: String) {
        var method: String? = null
        var onlineResource: WmsOnlineResource? = null
    }

    constructor(namespaceURI: String?) : super(namespaceURI) {
        this.initialize()
    }

    private fun initialize() {
        get = QName(this.namespaceUri, "Get")
        post = QName(this.namespaceUri, "Post")
        http = QName(this.namespaceUri, "HTTP")
        onlineResource = QName(this.namespaceUri, "OnlineResource")
    }

    fun getDCPInfos(): List<DcpInfo> {
        val infos: MutableList<DcpInfo> = ArrayList()

        val httpModel: NameStringModel? = this.getField(http) as NameStringModel?
        if (httpModel != null) {
            var model = httpModel.getField(get) as NameStringModel?
            if (model != null) {
                val dcpInfo = DcpInfo(httpModel.getField(CHARACTERS_CONTENT).toString())
                dcpInfo.method = model.getField(CHARACTERS_CONTENT).toString()
                dcpInfo.onlineResource = model.getField(onlineResource) as WmsOnlineResource?
                infos.add(dcpInfo)
            }
            model = httpModel.getField(post) as NameStringModel?
            if (model != null) {
                val dcpInfo =
                    DcpInfo(httpModel.getField(CHARACTERS_CONTENT).toString())
                dcpInfo.method = model.getField(CHARACTERS_CONTENT).toString()
                dcpInfo.onlineResource = model.getField(onlineResource) as WmsOnlineResource?
                infos.add(dcpInfo)
            }
        }

        return infos
    }


    override fun toString(): String {
        val sb = StringBuilder()
        for (dcpi in this.getDCPInfos()) {
            sb.append(dcpi.protocol).append(", ")
            sb.append(dcpi.method).append(", ")
            sb.append(dcpi.onlineResource.toString())
        }
        return sb.toString()
    }
}