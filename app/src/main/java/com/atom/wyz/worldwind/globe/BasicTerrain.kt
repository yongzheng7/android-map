package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Logger
import java.nio.ShortBuffer
import java.util.*

/**
 * 基本地形
 */
class BasicTerrain() : Terrain {

    var tiles = ArrayList<TerrainTile>()

    override var sector: Sector = Sector()

    var triStripElements: ShortArray? = null

    val intersectPoint = Vec3()

    fun addTile(tile: TerrainTile?) {
        if (tile == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile")
            )
        }
        tiles.add(tile)
        sector.union(tile.sector)
    }

    fun clear() {
        tiles.clear()
        sector.setEmpty()
        triStripElements = null
    }


    override fun geographicToCartesian(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        altitudeMode: Int,
        result: Vec3?
    ): Vec3? {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult")
            )
        }
        return null // TODO
    }

    override fun intersect(line: Line?, result: Vec3?): Boolean {
        requireNotNull(line) { Logger.logMessage(Logger.ERROR, "BasicTerrain", "intersect", "missingLine") }
        requireNotNull(result) { Logger.logMessage(Logger.ERROR, "BasicTerrain", "intersect", "missingResult") }

        var minDist2 = Double.POSITIVE_INFINITY
        var idx = 0
        val len = tiles.size
        while (idx < len) {
            val tile = tiles[idx]
            line.origin.subtract(tile.vertexOrigin)
            if (line.triStripIntersection(tile.vertexPoints, 3, triStripElements, intersectPoint)) {
                val dist2 = line.origin.distanceToSquared(intersectPoint)
                if (minDist2 > dist2) {
                    minDist2 = dist2
                    result.set(intersectPoint)!!.add(tile.vertexOrigin)
                }
            }
            line.origin.add(tile.vertexOrigin)
            idx++
        }

        return minDist2 != Double.POSITIVE_INFINITY    }

}