package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.render.DrawContext
import java.util.HashMap

interface Layer {
    var displayName: String
    var enabled: Boolean
    var pickEnabled: Boolean
    var opacity: Double
    var minActiveAltitude: Double
    var maxActiveAltitude: Double
    var userProperties: HashMap<Any, Any>?

    fun render(dc: DrawContext);

    fun isWithinActiveAltitudes(dc: DrawContext): Boolean;

    fun getUserProperty(key: Any): Any?

    fun putUserProperty(key: Any, value: Any): Any?

    fun removeUserProperty(key: Any?): Any?

    fun hasUserProperty(key: Any?): Boolean
}