package com.atom.map.globe

import com.atom.map.geom.*
import com.atom.map.ogc.ElevationModel
import com.atom.map.util.Logger
import kotlin.math.sqrt

class Globe {
    /**
     * The globe's reference ellipsoid defining the globe's equatorial radius and polar radius.
     */
    var ellipsoid: Ellipsoid =
        Ellipsoid()
    set(value) {
        field.set(value)
    }
    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     */
    var projection: GeographicProjection

    val elevationModel = ElevationModel()

    constructor(ellipsoid: Ellipsoid, projection: GeographicProjection) {
        this.ellipsoid.set(ellipsoid)
        this.projection = projection
    }

    /**
     * 赤道半径
     */
    fun getEquatorialRadius(): Double {
        return ellipsoid.semiMajorAxis()
    }

    /**
     * 极半径
     */
    fun getPolarRadius(): Double {
        return ellipsoid.semiMinorAxis()
    }

    /**
     * 半径表示在指定位置的地球椭球的半径（以米为单位）。
     */
    fun getRadiusAt(latitude: Double, longitude: Double): Double {
        val sinLat = Math.sin(Math.toRadians(latitude))
        val ec2 = ellipsoid.eccentricitySquared() // 偏心率的平方
        val rpm = ellipsoid.semiMajorAxis() / Math.sqrt(1 - ec2 * sinLat * sinLat)
        return rpm * sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat)
    }

    /**
     * 指示地球仪椭球体的偏心率平方参数。 这等效于<code> 2 * f-f * f </ code>，其中<code> f </ code>是椭球的展平参数。
     */
    fun getEccentricitySquared(): Double {
        return ellipsoid.eccentricitySquared()
    }
    /**
     * 经纬度转笛卡尔坐标系
     * x 轴  经度 90 纬度 0
     * y 轴  纬度 90
     * z 轴  经度 0   纬度 0
     */
    fun geographicToCartesian(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        result: Vec3
    ): Vec3 {
        return projection.geographicToCartesian(this, latitude, longitude, altitude, result)
    }
    /**
     * 经纬度转笛卡尔坐标系中的法向量规格化
     * x 轴  经度 90 纬度 0
     * y 轴  纬度 90
     * z 轴  经度 0   纬度 0
     */
    fun geographicToCartesianNormal(
        latitude: Double,
        longitude: Double,
        result: Vec3
    ): Vec3 {
        return projection.geographicToCartesianNormal(this, latitude, longitude, result)
    }
    /**
     * 经纬度转笛卡尔坐标系中的观察矩阵,该矩阵的实际意义是笛卡尔坐标系中原点位移到该点的变换矩阵
     * x 轴  经度 90 纬度 0
     * y 轴  纬度 90
     * z 轴  经度 0   纬度 0
     */
    fun geographicToCartesianTransform(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        result: Matrix4
    ): Matrix4 {
        return projection.geographicToCartesianTransform(
            this,
            latitude,
            longitude,
            altitude,
            result
        )
    }

    /**
     * 通过经纬度区域以及划分的点数进行获取该区域内的笛卡尔坐标数组
     */
    fun geographicToCartesianGrid(
        sector: Sector, numLat: Int, numLon: Int, height: FloatArray?,verticalExaggeration:Float,
        origin: Vec3?, result: FloatArray, offset: Int, rowStride: Int
    ): FloatArray {
        require(!(numLat < 1 || numLon < 1)) {
            Logger.logMessage(
                Logger.ERROR,
                "Globe",
                "geographicToCartesianGrid",
                "Number of latitude or longitude locations is less than one"
            )
        }
        val numPoints = numLat * numLon
        require(!(height != null && height.size < numPoints)) {
            Logger.logMessage(
                Logger.ERROR, "Globe",
                "geographicToCartesianGrid", "missingArray"
            )
        }
        return projection.geographicToCartesianGrid(
            this, sector,
            numLat, numLon,
            height, verticalExaggeration,origin ,
            result, offset, rowStride)
    }

    fun geographicToCartesianBorder(
        sector: Sector, numLat: Int, numLon: Int, height: Float,
        origin: Vec3?, result: FloatArray
    ): FloatArray {
        require(!(numLat < 1 || numLon < 1)) {
            Logger.logMessage(
                Logger.ERROR,
                "Globe",
                "geographicToCartesianBorder",
                "Number of latitude or longitude locations is less than one"
            )
        }
        return projection.geographicToCartesianBorder(
            this, sector,
            numLat, numLon, height,
            origin, result
        )
    }

    /**
     * 笛卡尔坐标系转经纬度坐标系
     */
    fun cartesianToGeographic(
        x: Double,
        y: Double,
        z: Double,
        result: Position
    ): Position {
        return projection.cartesianToGeographic(this, x, y, z, result)
    }

    /**
     * 笛卡尔坐标系转本地变换矩阵
     */
    fun cartesianToLocalTransform(
        x: Double,
        y: Double,
        z: Double,
        result: Matrix4
    ): Matrix4 {
        return projection.cartesianToLocalTransform(this, x, y, z, result)
    }

    protected fun computeViewHeading(matrix: Matrix4, roll: Double): Double {
        val rad = Math.toRadians(roll)
        val cr = Math.cos(rad)
        val sr = Math.sin(rad)
        val m = matrix.m
        val ch = cr * m[0] - sr * m[4]
        val sh = sr * m[5] - cr * m[1]
        return Math.toDegrees(Math.atan2(sh, ch))
    }

    fun computeViewTilt(matrix: Matrix4): Double {
        val m = matrix.m
        val ct = m[10]
        val st = Math.sqrt(m[2] * m[2] + m[6] * m[6])
        return Math.toDegrees(Math.atan2(st, ct))
    }

    /**
     * 指示从地球椭球上方的指定高度到地球地平线的距离。 如果高度为负，则此方法的结果不确定。
     */
    fun horizonDistance(height: Double): Double {
        val r = ellipsoid.semiMajorAxis()
        return Math.sqrt(height * (2 * r + height))
    }

    fun horizonDistance(eyeAltitude: Double, objectAltitude: Double): Double {
        val eqr = getEquatorialRadius()
        val eyeDistance = Math.sqrt(eyeAltitude * (2 * eqr + eyeAltitude)) // distance from eye altitude to globe MSL horizon
        val horDistance = Math.sqrt(objectAltitude * (2 * eqr + objectAltitude)) // distance from object altitude to globe MSL horizon
        return eyeDistance + horDistance // desired distance is the sum of the two horizon distances
    }

    /**
     * 用指定的线计算地球仪的第一个交点。 这条线被解释为射线。 线原点后的交点将被忽略。
     */
    fun intersect(line: Line, result: Vec3): Boolean {
        return projection.intersect(this, line, result)
    }
}