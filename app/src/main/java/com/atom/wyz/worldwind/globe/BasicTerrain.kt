package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.WWMath
import java.util.*

/**
 * 基本地形
 */
class BasicTerrain() : Terrain {

    var tiles = ArrayList<TerrainTile>()

    override var sector: Sector = Sector()

    var triStripElements: ShortArray? = null

    val intersectPoint = Vec3()

    val normal = Vec3()

    fun addTile(tile: TerrainTile) {
        tiles.add(tile)
        sector.union(tile.sector)
    }

    fun clear() {
        tiles.clear()
        sector.setEmpty()
        triStripElements = null
    }

    override fun intersect(line: Line, result: Vec3): Boolean {
        var minDist2 = Double.POSITIVE_INFINITY
        var idx = 0
        val len = tiles.size
        while (idx < len) {
            val tile = tiles[idx]
            line.origin.subtract(tile.origin)
            if (line.triStripIntersection(tile.points, 3, triStripElements,
                    triStripElements!!.size, intersectPoint)) {
                val dist2 = line.origin.distanceToSquared(intersectPoint)
                if (minDist2 > dist2) {
                    minDist2 = dist2
                    result.set(intersectPoint).add(tile.origin)
                }
            }
            line.origin.add(tile.origin)
            idx++
        }

        return minDist2 != Double.POSITIVE_INFINITY
    }

    override fun surfacePoint(
        latitude: Double,
        longitude: Double,
        result: Vec3
    ): Boolean {
        var idx = 0
        val len = tiles.size
        while (idx < len) {
            val tile = tiles[idx]
            val sector = tile.sector
            // 找到包含该坐标的图块
            if (sector.contains(latitude, longitude)) {
                // Compute the location's parameterized coordinates (s, t) within the tile grid, along with the
                // fractional component (sf, tf) and integral component (si, ti).
                val tileWidth = tile.level.tileWidth // 图块的宽
                val tileHeight = tile.level.tileHeight // 图块的高
                val tempW_1 = tileWidth - 1
                val tempH_1 = tileHeight - 1
                val s: Double   = (longitude - sector.minLongitude) / sector.deltaLongitude() * tempW_1 // 图块在该区域的相对左下角的宽度
                val t: Double   = (latitude - sector.minLatitude) / sector.deltaLatitude() * tempH_1 // 图块在该区域的相对左下角的高度
                val sf = if (s < tileWidth - 1) WWMath.fract(s) else 1.0
                val tf = if (t < tileHeight - 1) WWMath.fract(t) else 1.0
                val si = if (s < tileWidth - 1) (s + 1).toInt() else tileWidth - 1
                val ti = if (t < tileHeight - 1) (t + 1).toInt() else tileHeight - 1

                // Compute the location in the tile's local coordinate system. Perform a bilinear interpolation of
                // the cell's four points based on the fractional portion of the location's parameterized coordinates.
                // Tile coordinates are organized in the vertexPoints array in row major order, starting at the tile's
                // Southwest corner.

                // Compute the location in the tile's local coordinate system. Perform a bilinear interpolation of
                // the cell's four points based on the fractional portion of the location's parameterized coordinates.
                // Tile coordinates are organized in the points array in row major order, starting at the tile's
                // Southwest corner. Account for the tile's border vertices, which are embedded in the points array but
                // must be ignored for this computation.
                val tileRowStride = tileWidth + 2
                val i00 = (si + ti * tileRowStride) * 3 // lower left coordinate
                val i10 = i00 + 3 // lower right coordinate
                val i01 = (si + (ti + 1) * tileRowStride) * 3 // upper left coordinate
                val i11 = i01 + 3
                val f00 = (1 - sf) * (1 - tf)
                val f10 = sf * (1 - tf)
                val f01 = (1 - sf) * tf
                val f11 = sf * tf

                val points = tile.points !!
                result.x = points[i00] *     f00 + points[i10] *     f10 + points[i01] *     f01 + points[i11] *     f11
                result.y = points[i00 + 1] * f00 + points[i10 + 1] * f10 + points[i01 + 1] * f01 + points[i11 + 1] * f11
                result.z = points[i00 + 2] * f00 + points[i10 + 2] * f10 + points[i01 + 2] * f01 + points[i11 + 2] * f11
                // Translate the surface point from the tile's local coordinate system to Cartesian coordinates.
                result.x += tile.origin.x
                result.y += tile.origin.y
                result.z += tile.origin.z
                return true
            }
            idx++
        }
        // No tile was found that contains the location.
        return false
    }
}