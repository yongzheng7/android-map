package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Line
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3

/**
 * 地面 地形 包含多个瓦片
 */
interface Terrain {

    var sector: Sector

    var globe: Globe?

    var verticalExaggeration : Double

    fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double, @WorldWind.AltitudeMode altitudeMode: Int, result: Vec3): Vec3

    fun intersect(line: Line, result: Vec3): Boolean

    fun surfacePoint(latitude: Double, longitude: Double, offset: Double, result: Vec3): Boolean

}