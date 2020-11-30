package com.atom.map.layer

import com.atom.map.renderable.RenderContext
import java.util.*

interface Layer {
    var displayName: String
    var enabled: Boolean
    var pickEnabled: Boolean
    var opacity: Double
    var minActiveAltitude: Double
    var maxActiveAltitude: Double
    var userProperties: HashMap<Any, Any>?

    fun render(rc: RenderContext);

    fun isWithinActiveAltitudes(rc: RenderContext): Boolean;

    fun getUserProperty(key: Any): Any?

    fun putUserProperty(key: Any, value: Any): Any?

    fun removeUserProperty(key: Any?): Any?

    fun hasUserProperty(key: Any?): Boolean
}