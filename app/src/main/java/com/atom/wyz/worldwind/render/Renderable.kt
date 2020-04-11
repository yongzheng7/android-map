package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext

interface Renderable {

    var displayName: String
    var enabled: Boolean
    var pickDelegate: Any?

    fun getUserProperty(key: Any): Any?

    fun putUserProperty(key: Any, value: Any): Any?

    fun removeUserProperty(key: Any): Any?

    fun hasUserProperty(key: Any): Boolean

    fun render(dc: DrawContext)
}