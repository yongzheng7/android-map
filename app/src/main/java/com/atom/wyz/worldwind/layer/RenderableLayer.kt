package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.context.RenderContext
import com.atom.wyz.worldwind.render.Renderable
import com.atom.wyz.worldwind.util.Logger

open class RenderableLayer(displayName: String) : AbstractLayer(displayName), Iterable<Renderable> {

    protected val renderables = arrayListOf<Renderable>()

    constructor() : this("RenderableLayer")

    constructor(renderables: Iterable<Renderable>) : this("RenderableLayer") {
        addAllRenderables(renderables)
    }

    constructor(layer: RenderableLayer) : this(layer.displayName) {
        addAllRenderables(layer)
    }
    fun count(): Int {
        return renderables.size
    }

    fun getRenderable(index: Int): Renderable? {
        if (index < 0 || index >= renderables.size) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "getRenderable", "invalidIndex"))
        }
        return renderables[index]
    }

    fun setRenderable(index: Int, renderable: Renderable?): Renderable? {
        if (index < 0 || index >= renderables.size) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "setRenderable", "invalidIndex"))
        }
        if (renderable == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "setRenderable", "missingRenderable"))
        }
        return renderables.set(index, renderable)
    }

    fun indexOfRenderable(renderable: Renderable?): Int {
        if (renderable == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "indexOfRenderable", "missingRenderable"))
        }
        return renderables.indexOf(renderable)
    }

    fun indexOfRenderableNamed(name: String?): Int {
        for (i in renderables.indices) {
            if (renderables[i].displayName == name) {
                return i
            }
        }
        return -1
    }

    fun indexOfRenderableWithProperty(key: Any, value: Any?): Int {
        for (i in renderables.indices) {
            val renderable: Renderable = renderables[i]
            if (renderable.hasUserProperty(key)) {
                val layerValue: Any? = renderable.getUserProperty(key)
                if (if (layerValue == null) value == null else layerValue == value) {
                    return i
                }
            }
        }
        return -1
    }

    fun addRenderable(renderable: Renderable) {
        renderables.add(renderable)
    }

    fun addRenderable(index: Int, renderable: Renderable) {
        if (index < 0 || index > renderables.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "addRenderable", "invalidIndex"))
        }
        renderables.add(index, renderable)
    }

    open fun addAllRenderables(layer: RenderableLayer) {
        val thisList = renderables
        val thatList = layer.renderables
        thisList.ensureCapacity(thatList.size)
        var idx = 0
        val len = thatList.size
        while (idx < len) {
            thisList.add(thatList[idx]) // we know the contents of layer.renderables is valid
            idx++
        }
    }
    fun addAllRenderables(renderables: Iterable<Renderable>) {
        for (renderable in renderables) {
            this.renderables.add(renderable)
        }
    }

    fun removeRenderable(renderable: Renderable): Boolean {
        return renderables.remove(renderable)
    }

    fun removeRenderable(index: Int): Renderable? {
        if (index < 0 || index >= renderables.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "removeRenderable", "invalidIndex"))
        }
        return renderables.removeAt(index)
    }

    fun removeAllRenderables(renderables: Iterable<Renderable>): Boolean {
        var removed = false
        for (renderable in renderables) {
            removed = removed or this.renderables.remove(renderable)
        }
        return removed
    }

    fun clearRenderables() {
        renderables.clear()
    }

    override fun doRender(rc: RenderContext) {
        for (renderable in renderables) {
            try {
                renderable.render(rc)
            } catch (e: Exception) {
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "doRender",
                        "Exception while rendering shape \'" + renderable.displayName + "\'", e)
            }
        }
    }

    override fun iterator(): Iterator<Renderable> {
        return renderables.iterator()
    }


}