package com.atom.map.layer

import com.atom.map.layer.render.RenderContext
import com.atom.map.util.Logger

class LayerList() : Iterable<Layer> {
    private val layers = arrayListOf<Layer>()

    constructor(layers: Iterable<Layer>) : this() {
        this.addAllLayers(layers)
    }

    constructor(layerList: LayerList) : this() {
        this.addAllLayers(layerList)
    }

    fun count(): Int {
        return layers.size
    }

    fun getLayer(index: Int): Layer {
        if (index < 0 || index >= layers.size) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "getLayer", "invalidIndex")
            )
        }
        return this.layers[index]
    }

    fun setLayer(index: Int, layer: Layer): Layer {
        if (index < 0 || index >= layers.size) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "setLayer", "invalidIndex")
            )
        }
        return this.layers.set(index, layer)
    }

    fun indexOfLayer(layer: Layer): Int {
        return layers.indexOf(layer)
    }

    fun indexOfLayerNamed(name: String?): Int {
        for (i in layers.indices) {
            val layerName = layers[i].displayName
            if (layerName.equals(name)) {
                return i
            }
        }
        return -1
    }

    fun indexOfLayerWithProperty(key: Any, value: Any?): Int {
        for (i in layers.indices) {
            val layer: Layer = layers[i]
            if (layer.hasUserProperty(key)) {
                val layerValue = layer.getUserProperty(key)
                if (if (layerValue == null) value == null else layerValue == value) {
                    return i
                }
            }
        }
        return -1
    }

    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    fun addLayer(index: Int, layer: Layer) {
        if (index < 0 || index > layers.size) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "invalidIndex")
            )
        }
        layers.add(index, layer)
    }

    fun addAllLayers(layers: Iterable<Layer?>) {
        layers.forEach {
            it?.also { itLayer ->
                this.layers.add(itLayer)
            }
        }
    }

    fun addAllLayers(layerList: LayerList) {
        val thisList = layers
        val thatList = layerList.layers
        thisList.ensureCapacity(thatList.size)
        var idx = 0
        val len = thatList.size
        while (idx < len) {
            thisList.add(thatList[idx]) // we know the contents of layerList.layers is valid
            idx++
        }
    }

    fun removeLayer(layer: Layer): Boolean {
        return layers.remove(layer)
    }

    fun removeLayer(index: Int): Layer? {
        if (index < 0 || index >= layers.size) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "removeLayer", "invalidIndex")
            )
        }
        return layers.removeAt(index)
    }

    fun removeAllLayers(layers: Iterable<Layer?>): Boolean {
        var removed = false
        for (layer in layers) {
            val let = layer?.let {
                this.layers.remove(it)
            } ?: false
            removed = removed or let

        }
        return removed
    }

    fun clearLayers() {
        layers.clear()
    }

    override fun iterator(): Iterator<Layer> {
        return layers.iterator()
    }

    fun render(rc: RenderContext) {
        var idx = 0
        val len = layers.size
        while (idx < len) {
            rc.currentLayer = layers[idx]
            try {
                rc.currentLayer!!.render(rc)
            } catch (e: Exception) {
                Logger.logMessage(
                    Logger.ERROR,
                    "LayerList",
                    "render",
                    "Exception while rendering layer \'" + rc.currentLayer!!.displayName
                        .toString() + "\'",
                    e
                )
                // Keep going. Draw the remaining layers.
            }
            idx++
        }
        rc.currentLayer = null
    }
}