package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.util.Logger

class LayerList() : Iterable<Layer> {
    protected val layers = arrayListOf<Layer>()

    constructor(layers: Iterable<Layer>?) : this(){
        if (layers == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "constructor", "missingList"))
        }
        addAllLayers(layers)
    }
    
    fun count(): Int {
        return layers.size
    }

    fun getLayer(index: Int): Layer? {
        if (index < 0 || index >= layers.size) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "getLayer", "invalidIndex"))
        }
        return this.layers[index]
    }

    fun setLayer(index: Int, layer: Layer?): Layer? {
        if (index < 0 || index >= layers.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "setLayer", "invalidIndex"))
        }
        if (layer == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "setLayer", "missingLayer"))
        }
        return this.layers.set(index, layer)
    }

    fun indexOfLayer(layer: Layer?): Int {
        if (layer == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "indexOfLayer", "missingLayer"))
        }
        return layers.indexOf(layer)
    }

    fun indexOfLayerNamed(name: String?): Int {
        for (i in layers.indices) {
            val layerName: String = layers[i].displayName
            if (layerName == name) {
                return i
            }
        }
        return -1
    }

    fun indexOfLayerWithProperty(key: Any, value: Any?): Int {
        for (i in layers.indices) {
            val layer: Layer = layers[i]
            if (layer.hasUserProperty(key)) {
                val layerValue: Any? = layer.getUserProperty(key)
                if (if (layerValue == null) value == null else layerValue == value) {
                    return i
                }
            }
        }
        return -1
    }

    fun addLayer(layer: Layer?) {
        if (layer == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "missingLayer"))
        }
        layers.add(layer)
    }

    fun addLayer(index: Int, layer: Layer?) {
        if (index < 0 || index > layers.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "invalidIndex"))
        }
        if (layer == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "missingLayer"))
        }
        layers.add(index, layer)
    }

    fun addAllLayers(layers: Iterable<Layer?>?) {
        if (layers == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "addAllLayers", "missingList"))
        }
        for (layer in layers) {
            if (layer == null) {
                throw java.lang.IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "LayerList", "addAllLayers", "missingLayer"))
            }
            this.layers.add(layer)
        }
    }

    fun removeLayer(layer: Layer?): Boolean {
        if (layer == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "removeLayer", "missingLayer"))
        }
        return layers.remove(layer)
    }

    fun removeLayer(index: Int): Layer? {
        if (index < 0 || index >= layers.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "removeLayer", "invalidIndex"))
        }
        return layers.removeAt(index)
    }

    fun removeAllLayers(layers: Iterable<Layer?>?): Boolean {
        if (layers == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "removeAllLayers", "missingList"))
        }
        var removed = false
        for (layer in layers) {
            if (layer == null) {
                throw java.lang.IllegalArgumentException(
                        Logger.logMessage(Logger.ERROR, "LayerList", "removeAllLayers", "missingLayer"))
            }
            removed = removed or this.layers.remove(layer)
        }
        return removed
    }

    fun clearLayers() {
        layers.clear()
    }

    override fun iterator(): Iterator<Layer> {
        return layers.iterator()
    }
}