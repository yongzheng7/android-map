package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.DrawContext

/**
 * 地面 地形 包含多个瓦片
 */
interface Terrain {
    /**
     * 获取地形区域
     */
    var sector: Sector

    /**
     * 获取瓦片总数
     */
    fun getTileCount(): Int

    /**
     * 获取指定瓦片的区域
     */
    fun getTileSector(index: Int): Sector?

    /**
     * 获取指定瓦片的笛卡尔数 笛卡尔中心
     */
    fun getTileVertexOrigin(index: Int): Vec3?

    /**
     * 申请Texcoord进行变换
     */
    fun applyTexCoordTransform(index: Int, dst: Sector?, result: Matrix3?)

    /**
     * 加载绘制顶点缓存
     */
    fun useVertexPointAttrib(dc: DrawContext?, index: Int, attribLocation: Int)
    /**
     * 加载纹理顶点缓存
     */
    fun useVertexTexCoordAttrib(dc: DrawContext?, attribLocation: Int)
    /**
     * 绘制瓦片的三角形
     */
    fun drawTileTriangles(dc: DrawContext?, index: Int)

    /**
     * 绘制瓦片的线
     */
    fun drawTileLines(dc: DrawContext?, index: Int)

    /**
     * 地理坐标到笛卡尔积
     */
    fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double,
                              @WorldWind.AltitudeMode altitudeMode: Int, result: Vec3?): Vec3?

}