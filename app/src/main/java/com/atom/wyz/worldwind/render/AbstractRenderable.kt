package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.RenderContext

abstract class AbstractRenderable(displayer: String) : Renderable {

    override var displayName: String = displayer

    override var enabled: Boolean = true

    override var pickDelegate: Any? = null

    protected var userProperties: HashMap<Any, Any>? = null

    constructor() :this("")

    override fun getUserProperty(key: Any): Any? {
        return if (userProperties == null) userProperties!![key] else null
    }

    override fun putUserProperty(key: Any, value: Any): Any? {
        if (userProperties == null) {
            userProperties = java.util.HashMap()
        }

        return userProperties!!.put(key, value)
    }

    override fun removeUserProperty(key: Any): Any? {
        return if (userProperties != null) userProperties!!.remove(key) else null
    }

    override fun hasUserProperty(key: Any): Boolean {
        return userProperties != null && userProperties!!.containsKey(key)
    }

    override fun render(rc: RenderContext) {
        if (!enabled) {
            return
        }

        this.doRender(rc)
    }

    protected abstract fun doRender(rc: RenderContext)
}