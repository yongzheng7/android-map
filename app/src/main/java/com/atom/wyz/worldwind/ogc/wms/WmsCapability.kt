package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.util.xml.XmlModel
import java.util.*

open class WmsCapability : XmlModel() {

    open var request: WmsRequest? = null

    open var layers: MutableList<WmsLayer> = ArrayList()

    /**
     * Object representation of an Exception element. Pre-allocated to prevent NPE in the event the server does not
     * include an Exception block.
     */
    open var exception: WmsException = WmsException()


    fun getCapabilities(): WmsCapabilities? {
        var model: XmlModel? = this
        while (model != null) {
            model = model.parent
            if (model is WmsCapabilities) {
                return model
            }
        }
        return null
    }

    override fun parseField(keyName: String, value: Any) {
        when (keyName) {
            "Request" -> {
                request = value as WmsRequest
            }
            "Exception" -> {
                exception.formats.addAll((value as WmsException).formats)
            }
            "Layer" -> {
                layers.add(value as WmsLayer)
            }
        }
    }

}