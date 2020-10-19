package com.atom.map.layer.render.pick

import com.atom.map.geom.Position
import com.atom.map.geom.SimpleColor
import com.atom.map.layer.Layer
import com.atom.map.layer.render.Renderable

class PickedObject {

    companion object {
        fun fromRenderable(identifier : Int, renderable: Renderable, layer: Layer): PickedObject {
            val po = PickedObject()
            po.identifier = identifier
            po.userObject = if (renderable.pickDelegate != null) renderable.pickDelegate else renderable
            po.layer = layer
            return po
        }

        fun fromTerrain( identifier: Int , position: Position): PickedObject {
            val po = PickedObject()
            po.terrainPosition = Position(position)
            po.userObject = po.terrainPosition
            po.identifier = identifier
            return po
        }

        fun identifierToUniqueColor(identifier: Int, result: SimpleColor): SimpleColor {
            val r8 = identifier shr 16 and 0xFF
            val g8 = identifier shr 8 and 0xFF
            val b8 = identifier and 0xFF
            result.red = r8 / 0xFF.toFloat()
            result.green = g8 / 0xFF.toFloat()
            result.blue = b8 / 0xFF.toFloat()
            result.alpha = 1f
            return result
        }

        fun uniqueColorToIdentifier(color: SimpleColor): Int {
            val r8 = Math.round(color.red * 0xFF)
            val g8 = Math.round(color.green * 0xFF)
            val b8 = Math.round(color.blue * 0xFF)
            return r8 shl 16 or (g8 shl 8) or b8
        }
    }

    var isOnTop = false

    var identifier = 0

    var userObject: Any? = null

    var layer: Layer? = null

    var terrainPosition: Position? = null


    fun markOnTop() {
        isOnTop = true
    }

    fun isTerrain(): Boolean {
        return terrainPosition != null
    }

    override fun toString(): String {
        return "PickedObject{" +
                "isOnTop=" + isOnTop +
                ", identifier=" + identifier +
                ", userObject=" + userObject +
                ", layer=" + layer +
                ", terrainPosition=" + terrainPosition +
                '}'
    }
}