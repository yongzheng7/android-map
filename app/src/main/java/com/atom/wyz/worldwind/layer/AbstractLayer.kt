package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.DrawContext
import java.util.*

abstract class AbstractLayer : Layer {

    final override var displayName: String

    override var enabled: Boolean = true

    override var pickEnabled: Boolean = true

    override var opacity = 1.0

    override var minActiveAltitude = Double.NEGATIVE_INFINITY

    override var maxActiveAltitude = Double.POSITIVE_INFINITY

    override var userProperties: HashMap<Any, Any>? = null

    constructor(displayName: String){
        this.displayName = displayName
    }

    override fun getUserProperty(key: Any): Any? {
        return if (userProperties != null) userProperties!![key] else null
    }

    override fun putUserProperty(key: Any, value: Any): Any? {
        if (userProperties == null) {
            userProperties = HashMap()
        }
        return userProperties!!.put(key, value)
    }

    override fun removeUserProperty(key: Any?): Any? {
        return if (userProperties != null) userProperties!!.remove(key) else null
    }

    override fun hasUserProperty(key: Any?): Boolean {
        return userProperties != null && userProperties!!.containsKey(key)
    }

    override fun render(dc: DrawContext) {
        if (!this.enabled) {
            return
        }

        if (dc.pickingMode && !this.pickEnabled) {
            return
        }

        if (!isWithinActiveAltitudes(dc)) {
            return
        }

        this.doRender(dc)
    }

    override fun isWithinActiveAltitudes(dc: DrawContext): Boolean {
        val eyeAltitude: Double = dc.eyePosition.altitude
        return eyeAltitude >= minActiveAltitude && eyeAltitude <= maxActiveAltitude
    }

    protected abstract fun doRender(dc: DrawContext)
}