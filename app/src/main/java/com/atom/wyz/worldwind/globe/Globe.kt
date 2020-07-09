package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.*
import java.nio.FloatBuffer

/**
 * 地球
 */
interface Globe {
    /**
     * 指示赤道上地球椭球的半径（以米为单位）
     */
    var equatorialRadius: Double
    /**
     * 指示地球两极的椭球半径（以米为单位）
     */
    var polarRadius: Double
    /**
     * 偏心率平方
     */
    var eccentricitySquared: Double
    /**
     * 地理投影
     */
    var projection: GeographicProjection

    /**
     * 镶嵌器
     */
    var tessellator: Tessellator
    /**
     * 指示某个位置的地球椭球的半径（以米为单位）
     */
    fun getRadiusAt(latitude: Double, longitude: Double): Double


    /**
     * 地理学位置转笛卡尔坐标 vec3
     */
    fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double, result: Vec3): Vec3

    /**
     *
     * @param latitude
     * @param longitude
     * @param result
     * @return
     */
    fun geographicToCartesianNormal(latitude: Double, longitude: Double, result: Vec3): Vec3

    /**
     * 区域到笛卡尔变换
     * 输入经纬度 和高度 转笛卡尔矩阵
     */
    fun geographicToCartesianTransform(latitude: Double, longitude: Double, altitude: Double, result: Matrix4): Matrix4

    /**
     * 通过经纬度范围，以及分割线 进行转化出对应的顶点
     * 区域经纬度 转笛卡尔网络
     */
    fun geographicToCartesianGrid(sector: Sector, numLat: Int, numLon: Int, elevations: DoubleArray?,
                                  origin: Vec3?, result: FloatArray , stride : Int , pos : Int ): FloatArray

    /**
     * 笛卡尔转地域
     */
    fun cartesianToGeographic(x: Double, y: Double, z: Double, result: Position): Position


    fun cartesianToLocalTransform(x: Double, y: Double, z: Double, result: Matrix4): Matrix4?

    fun cameraToCartesianTransform(camera: Camera, result: Matrix4): Matrix4

    fun cameraToLookAt(camera: Camera, result: LookAt): LookAt

    fun lookAtToCartesianTransform(lookAt: LookAt, result: Matrix4): Matrix4

    fun lookAtToCamera(lookAt: LookAt, result: Camera): Camera

    /**
     * 指示在指定位置的水平距离，以米为单位。
     */
    fun horizonDistance(eyeAltitude: Double): Double
    fun horizonDistance(eyeAltitude: Double, objectAltitude: Double): Double

    /**
     * 用指定的线计算地球仪的第一个交点。 该线被解释为射线。 线原点后的交点将被忽略。
     */
    fun intersect(line: Line, result: Vec3): Boolean
}