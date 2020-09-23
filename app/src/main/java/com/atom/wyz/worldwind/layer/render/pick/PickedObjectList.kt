package com.atom.wyz.worldwind.layer.render.pick

import android.util.SparseArray
import com.atom.wyz.worldwind.util.Logger
import java.util.*

class PickedObjectList {

    protected var entries: SparseArray<PickedObject?> = SparseArray();


    fun count(): Int {
        return entries.size()
    }

    fun offerPickedObject(pickedObject: PickedObject?) {
        pickedObject?.let { entries.put(it.identifier, it) }
    }

    fun pickedObjectAt(index: Int): PickedObject? {
        if (index < 0 || index >= entries.size()) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObjectList", "getPickedObject", "invalidIndex")
            )
        }
        return entries.valueAt(index)
    }

    fun pickedObjectWithId(identifier: Int): PickedObject? {
        return entries.get(identifier)
    }

    fun topPickedObject(): PickedObject? {
        var idx = 0
        val len = entries.size()
        while (idx < len) {
            entries.valueAt(idx)?.let {
                if (it.isOnTop) {
                    return it
                }
            }
            idx++
        }
        return null
    }

    fun terrainPickedObject(): PickedObject? {
        var idx = 0
        val len = entries.size()
        while (idx < len) {
            entries.valueAt(idx)?.let {
                if (it.isTerrain()) {
                    return it
                }
            }
            idx++
        }
        return null
    }

    fun hasNonTerrainObjects(): Boolean {
        var idx = 0
        val len = entries.size()
        while (idx < len) {
            entries.valueAt(idx)?.let {
                if (!it.isTerrain()) {
                    return true
                }
            }
            idx++
        }
        return false
    }

    fun clearPickedObjects() {
        entries.clear()
    }

    fun keepTopObjects() {
        val removalList = ArrayList<Int>()
        var idx = 0
        val len = this.entries.size()
        while (idx < len) {
            val po = this.entries.valueAt(idx)!!
            if (!po.isOnTop) {
                removalList.add(idx)
            }
            idx++
        }
        while (idx < len) {
            val indexToRemove = removalList[idx]
            entries.removeAt(indexToRemove)
            idx++
        }
    }

    override fun toString(): String {
        val sb = StringBuilder("PickedObjectList{")
        var idx = 0
        val len = entries.size()
        while (idx < len) {
            if (idx > 0) {
                sb.append(", ")
            }
            sb.append(entries.valueAt(idx).toString())
            idx++
        }
        sb.append("}")
        return sb.toString()
    }
}