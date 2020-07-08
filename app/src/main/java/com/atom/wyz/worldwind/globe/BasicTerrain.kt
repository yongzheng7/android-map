package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Logger
import java.util.*

/**
 * 基本地形
 */
class BasicTerrain() : Terrain {

    var tiles = ArrayList<TerrainTile>()

    var triStripElements: ShortArray? = null

    val intersectPoint = Vec3()

    val normal = Vec3()

    override var sector: Sector = Sector()

    override var globe: Globe? = null

    override var verticalExaggeration: Double = 1.0

    fun addTile(tile: TerrainTile) {
        tiles.add(tile)
        sector.union(tile.sector)
    }

    fun clear() {
        tiles.clear()
        sector.setEmpty()
        triStripElements = null
        globe = null
        verticalExaggeration = 1.0
    }

    override fun geographicToCartesian(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        altitudeMode: Int,
        result: Vec3
    ): Vec3 {
        return result // TODO
    }

    override fun intersect(line: Line, result: Vec3): Boolean {
        var minDist2 = Double.POSITIVE_INFINITY
        var idx = 0
        val len = tiles.size
        while (idx < len) {
            val tile = tiles[idx]
            line.origin.subtract(tile.vertexOrigin)
            if (line.triStripIntersection(tile.vertexPoints, 3, triStripElements,
                    triStripElements!!.size, intersectPoint)) {
                val dist2 = line.origin.distanceToSquared(intersectPoint)
                if (minDist2 > dist2) {
                    minDist2 = dist2
                    result.set(intersectPoint)!!.add(tile.vertexOrigin)
                }
            }
            line.origin.add(tile.vertexOrigin)
            idx++
        }

        return minDist2 != Double.POSITIVE_INFINITY
    }

    override fun surfacePoint(
        latitude: Double,
        longitude: Double,
        offset: Double,
        result: Vec3
    ): Boolean {
        var idx = 0
        val len = tiles.size
        while (idx < len) {
            val tile = tiles[idx]
            val sector = tile.sector
            // Find the first tile that contains the specified location.
            if (sector.contains(latitude, longitude)) {
                // Compute the location's parameterized coordinates (s, t) within the tile grid, along with the
                // fractional component (sf, tf) and integral component (si, ti).
                val tileWidth = tile.level.tileWidth
                val tileHeight = tile.level.tileHeight
                val s: Double = (longitude - sector.minLongitude) / sector.deltaLongitude() * (tileWidth - 1)
                val t: Double = (latitude - sector.minLatitude) / sector.deltaLatitude() * (tileHeight - 1)
                val sf: Double = if (s < tileWidth - 1) s - s.toInt() else 1.0
                val tf: Double = if (t < tileHeight - 1) t - t.toInt() else 1.0
                val si = if (s < tileWidth - 1) s.toInt() else tileWidth - 2
                val ti = if (t < tileHeight - 1) t.toInt() else tileHeight - 2
                // Compute the location in the tile's local coordinate system. Perform a bilinear interpolation of
                // the cell's four points based on the fractional portion of the location's parameterized coordinates.
                // Tile coordinates are organized in the vertexPoints array in row major order, starting at the tile's
                // Southwest corner.
                val i00 = (si + ti * tileWidth) * 3 // lower left coordinate
                val i10 = i00 + 3 // lower right coordinate
                val i01 = (si + (ti + 1) * tileWidth) * 3 // upper left coordinate
                val i11 = i01 + 3 // upper right coordinate

                val f00 = (1 - sf) * (1 - tf)
                val f10 = sf * (1 - tf)
                val f01 = (1 - sf) * tf
                val f11 = sf * tf

                val points = tile.vertexPoints
                result.x = points!![i00] * f00 + points[i10] * f10 + points[i01] * f01 + points[i11] * f11
                result.y = points[i00 + 1] * f00 + points[i10 + 1] * f10 + points[i01 + 1] * f01 + points[i11 + 1] * f11
                result.z = points[i00 + 2] * f00 + points[i10 + 2] * f10 + points[i01 + 2] * f01 + points[i11 + 2] * f11
                // Translate the point along a the vector 'offset' meters relative to the tile's surface.
                if (offset != 0.0) {
                    globe!!.geographicToCartesianNormal(latitude, longitude, normal)
                    result.x += normal.x * offset
                    result.y += normal.y * offset
                    result.z += normal.z * offset
                }
                // Translate the surface point from the tile's local coordinate system to Cartesian coordinates.
                result.x += tile.vertexOrigin.x
                result.y += tile.vertexOrigin.y
                result.z += tile.vertexOrigin.z
                return true
            }
            idx++
        }
        // No tile was found that contains the location.
        return false
    }
}