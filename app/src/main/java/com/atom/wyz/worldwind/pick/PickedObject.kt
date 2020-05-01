package com.atom.wyz.worldwind.pick

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.render.Renderable
import com.atom.wyz.worldwind.util.Logger

class PickedObject() {

    companion object {
        fun fromRenderable(
            renderable: Renderable?,
            position: Position?,
            layer: Layer?,
            identifier: Int
        ): PickedObject? {

            requireNotNull(renderable) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "fromRenderable",
                    "missingRenderable"
                )
            }

            requireNotNull(position) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "fromRenderable",
                    "missingPosition"
                )
            }

            requireNotNull(layer) { Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingLayer") }

            val po = PickedObject()
            po.userObject = if (renderable.pickDelegate != null) renderable.pickDelegate else renderable
            po.position = Position(position)
            po.layer = layer
            po.identifier = identifier
            return po
        }

        fun fromRenderable(renderable: Renderable?, layer: Layer?, identifier: Int): PickedObject? {
            requireNotNull(renderable) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "fromRenderable",
                    "missingRenderable"
                )
            }
            val po = PickedObject()
            po.userObject = if (renderable.pickDelegate != null) renderable.pickDelegate else renderable
            po.layer = layer
            po.identifier = identifier
            return po
        }

        fun fromTerrain(position: Position?, identifier: Int): PickedObject {
            requireNotNull(position) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "fromTerrain",
                    "missingPosition"
                )
            }
            val po = PickedObject()
            po.position = Position(position)
            po.userObject = po.position
            po.identifier = identifier
            return po
        }

        fun identifierToUniqueColor(identifier: Int, result: Color?): Color {
            requireNotNull(result) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "identifierToUniqueColor",
                    "missingResult"
                )
            }
            val r8 = identifier shr 16 and 0xFF
            val g8 = identifier shr 8 and 0xFF
            val b8 = identifier and 0xFF
            result.red = r8 / 0xFF.toFloat()
            result.green = g8 / 0xFF.toFloat()
            result.blue = b8 / 0xFF.toFloat()
            result.alpha = 1f
            return result
        }

        fun uniqueColorToIdentifier(color: Color?): Int {
            requireNotNull(color) {
                Logger.logMessage(
                    Logger.ERROR,
                    "PickedObject",
                    "uniqueColorToIdentifier",
                    "missingColor"
                )
            }
            val r8 = Math.round(color.red * 0xFF).toInt()
            val g8 = Math.round(color.green * 0xFF).toInt()
            val b8 = Math.round(color.blue * 0xFF).toInt()
            return r8 shl 16 or (g8 shl 8) or b8
        }
    }

    var isOnTop = false

    var userObject: Any? = null

    var position: Position? = null

    var layer: Layer? = null

    var identifier = 0

    fun markOnTop() {
        isOnTop = true
    }

    fun isTerrain(): Boolean {
        return userObject === position
    }

    override fun toString(): String {
        return "PickedObject{" +
                "isOnTop=" + isOnTop +
                ", userObject=" + userObject +
                ", position=" + position +
                ", layer=" + layer +
                ", identifier=" + identifier +
                '}'
    }
}