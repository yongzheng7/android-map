package com.atom.map.layer.render

abstract class AbstractRenderable(displayer: String = "") :
    Renderable {

    override var displayName: String = displayer

    override var enabled: Boolean = true

    override var pickDelegate: Any? = null

    private var userProperties: HashMap<Any, Any?>? = null

    override fun getUserProperty(key: Any): Any? {
        return userProperties?.get(key)
    }

    override fun putUserProperty(key: Any, value: Any?): Any? {
        userProperties ?: let { userProperties = HashMap() }
        return userProperties?.put(key, value)
    }

    override fun removeUserProperty(key: Any): Any? {
        return userProperties?.remove(key)
    }

    override fun hasUserProperty(key: Any): Boolean {
        return userProperties?.containsKey(key) ?: false
    }

    override fun render(rc: RenderContext) {
        if (!enabled) {
            return
        }

        this.doRender(rc)
    }

    protected abstract fun doRender(rc: RenderContext)
}