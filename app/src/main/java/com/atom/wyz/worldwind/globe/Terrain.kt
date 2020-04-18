package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3

/**
 * 地面 地形 包含多个瓦片
 */
interface Terrain {
    /**
     * 获取地形区域
     */
    var sector: Sector

    /**
     * 地理坐标到笛卡尔积
     */
    fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double,
                              @WorldWind.AltitudeMode altitudeMode: Int, result: Vec3?): Vec3?

}