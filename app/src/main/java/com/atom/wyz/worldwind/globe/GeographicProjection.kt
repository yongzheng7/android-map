package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.*
import java.nio.FloatBuffer

/**
 * 表示地理投影。经纬度和直角坐标系之间的投影
 */
interface GeographicProjection {
    /**
     *
     * @return
     */
    fun getDisplayName(): String?

    /**
     * 将地理位置转换为笛卡尔坐标(直角坐标系 xyz)
     *
     * @param globe     the globe this projection is applied to
     * @param latitude  the position's latitude in degrees
     * @param longitude the position's longitude in degrees
     * @param altitude  the position's altitude in meters
     * @param offset    an offset to apply to the Cartesian output. Typically only projections that are continuous apply
     * to this offset. Others ignore it. May be null to indicate no offset is applied.
     * @param result    a pre-allocated Vec3 in which to store the converted coordinates
     *
     * @return the result argument set to the convertex coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    fun geographicToCartesian(globe: Globe?, latitude: Double, longitude: Double, altitude: Double, offset: Vec3?, result: Vec3?): Vec3


    /**
     *
     * @param globe
     * @param latitude
     * @param longitude
     * @param result
     * @return
     */
    fun geographicToCartesianNormal(globe: Globe?, latitude: Double, longitude: Double, result: Vec3?): Vec3?

    /**
     *
     * @param globe
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     * @return
     */
    fun geographicToCartesianTransform(globe: Globe?, latitude: Double, longitude: Double, altitude: Double, offset: Vec3?, result: Matrix4?): Matrix4?

    /**
     * 通过经纬度范围，以及分割线 进行转化出对应的顶点缓存
     * @param globe
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param origin
     * @param offset
     * @param result
     * @return
     */
    fun geographicToCartesianGrid(globe: Globe?, sector: Sector?, numLat: Int, numLon: Int, elevations: DoubleArray?,
                                  origin: Vec3?, offset: Vec3?, result: FloatBuffer? , stride :Int ): FloatBuffer

    /**
     *笛卡尔地心坐标系转大地经纬度坐标系
     */
    fun cartesianToGeographic(globe: Globe?, x: Double, y: Double, z: Double, offset: Vec3?, result: Position?): Position?

    /**
     * 笛卡尔坐标系 到 本地的位移
     */
    fun cartesianToLocalTransform(globe: Globe?, x: Double, y: Double, z: Double, offset: Vec3?, result: Matrix4?): Matrix4?

    /**
     * 计算指定地球仪和直线的第一个交点。 该线被解释为射线。 线原点后的交点将被忽略。
     */
    fun intersect(globe: Globe?, line: Line?, offset: Vec3?, result: Vec3?): Boolean

}