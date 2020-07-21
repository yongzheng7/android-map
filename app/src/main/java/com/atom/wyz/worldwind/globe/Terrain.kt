package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3

/**
 * 地面 地形 包含多个瓦片
 */
interface Terrain {

    var sector: Sector

    fun intersect(line: Line, result: Vec3): Boolean

    fun surfacePoint(latitude: Double, longitude: Double,  result: Vec3): Boolean
}