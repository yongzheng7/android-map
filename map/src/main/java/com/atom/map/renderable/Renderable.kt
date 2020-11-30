package com.atom.map.renderable

interface Renderable {

    var displayName: String

    var enabled: Boolean

    var pickDelegate: Any?

    fun getUserProperty(key: Any): Any?

    fun putUserProperty(key: Any, value: Any?): Any?

    fun removeUserProperty(key: Any): Any?

    fun hasUserProperty(key: Any): Boolean

    fun render(rc: RenderContext)
}