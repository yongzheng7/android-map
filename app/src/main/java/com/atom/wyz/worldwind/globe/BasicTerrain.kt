package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Logger
import java.util.*

/**
 * 基本地形
 */
class BasicTerrain() : Terrain {

    protected var tiles = ArrayList<TerrainTile>()

    override var sector: Sector = Sector()

    fun addTile(tile: TerrainTile?) {
        if (tile == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile"))
        }
        tiles.add(tile)
        sector.union(tile.sector)
    }

    fun clearTiles() {
        tiles.clear()
        sector.setEmpty()
    }


    override fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double, altitudeMode: Int, result: Vec3?): Vec3? {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult"))
        }
        return null // TODO
    }

}