package com.atom.map.globe

import com.atom.map.geom.Line
import com.atom.map.geom.Sector
import com.atom.map.geom.Vec3

/**
 * 地面 地形 包含多个瓦片
 */
interface Terrain {

    var sector: Sector

    fun intersect(line: Line, result: Vec3): Boolean

    fun surfacePoint(latitude: Double, longitude: Double,  result: Vec3): Boolean
}