package com.atom.map.globe

import com.atom.map.geom.*

/**
 * 表示地理投影。经纬度和直角坐标系之间的投影
 */
interface GeographicProjection {

    fun getDisplayName(): String?

    /**
     * 将地理位置转换为笛卡尔坐标(直角坐标系 xyz)
     */
    fun geographicToCartesian(globe: Globe, latitude: Double, longitude: Double, altitude: Double, result: Vec3): Vec3

    fun geographicToCartesianNormal(globe: Globe, latitude: Double, longitude: Double, result: Vec3): Vec3

    fun geographicToCartesianTransform(globe: Globe, latitude: Double, longitude: Double, altitude: Double, result: Matrix4): Matrix4

    /**
     * 通过经纬度范围，以及分割线 进行转化出对应的顶点缓存
     */
    fun geographicToCartesianGrid(
        globe: Globe, sector: Sector,
        numLat: Int, numLon: Int, height: FloatArray?,
        verticalExaggeration :Float , origin: Vec3?,
        result: FloatArray, offset :Int, val_rowStride : Int ): FloatArray

    /**
     * 获取裙边
     */
    fun geographicToCartesianBorder(
        globe: Globe, sector: Sector,
        numLat: Int, numLon: Int, height: Float,
        origin: Vec3?, result: FloatArray
    ): FloatArray

    /**
     *笛卡尔地心坐标系转大地经纬度坐标系
     */
    fun cartesianToGeographic(globe: Globe, x: Double, y: Double, z: Double, result: Position): Position

    /**
     * 笛卡尔坐标系 到 本地的位移
     */
    fun cartesianToLocalTransform(globe: Globe, x: Double, y: Double, z: Double, result: Matrix4): Matrix4

    /**
     * 计算指定地球仪和直线的第一个交点。 该线被解释为射线。 线原点后的交点将被忽略。
     */
    fun intersect(globe: Globe, line: Line, result: Vec3): Boolean

}