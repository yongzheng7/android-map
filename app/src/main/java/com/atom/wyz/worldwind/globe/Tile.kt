package com.atom.wyz.worldwind.globe

import android.util.DisplayMetrics
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.BoundingBox
import com.atom.wyz.worldwind.geom.Frustum
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.LruMemoryCache
import com.atom.wyz.worldwind.util.WWMath
import java.util.*

open class Tile {

    companion object {
        /**
         * 在给定图块纬度的级别内计算图块的  行号。
         * 浮点数向下取整
         */
        fun computeRow(tileDelta: Double, latitude: Double): Int {
            var row = Math.floor((latitude + 90) / tileDelta).toInt()
            if (latitude == 90.0) {
                row -= 1
            }
            return row
        }

        /**
         * 在给定图块的最大纬度的级别内，计算图块的最后一行编号。
         * 即对浮点数向上取整
         */
        fun computeLastRow(tileDelta: Double, maxLatitude: Double): Int {
            var row = Math.ceil((maxLatitude + 90) / tileDelta - 1).toInt()
            if (maxLatitude + 90 < tileDelta) {
                row = 0 // if max latitude is in the first row, set the max row to 0
            }
            return row
        }


        /**
         * 在给定图块纬度的级别内计算图块的  列号。
         */
        fun computeColumn(tileDelta: Double, longitude: Double): Int {
            var col = Math.floor((longitude + 180) / tileDelta).toInt()
            if (longitude == 180.0) {
                col -= 1 // if longitude is at the end of the grid, subtract 1 from the computed column to return the last column
            }
            return col
        }


        /**
         * 在给定图块的最大经度的级别内，计算图块的最后一列编号。
         */
        fun computeLastColumn(tileDelta: Double, maxLongitude: Double): Int {
            var col = Math.ceil((maxLongitude + 180) / tileDelta - 1).toInt()
            if (maxLongitude + 180 < tileDelta) {
                col = 0 // if max longitude is in the first column, set the max column to 0
            }
            return col
        }

        /**
         * Creates all tiles for a specified level within a [LevelSet].
         * 创建一个指定级别内的所有图块
         */
        fun assembleTilesForLevel(
            level: Level,
            tileFactory: TileFactory,
            result: MutableCollection<Tile>
        ): Collection<Tile> {
            val sector: Sector = level.parent.sector
            val tileDelta: Double = level.tileDelta
            // 0   1    2    3    4    5   6
            // 90  45
            // 8  32  128 512 2048  8192
            val firstRow: Int = computeRow(tileDelta, sector.minLatitude)
            val lastRow: Int = computeLastRow(tileDelta, sector.maxLatitude)
            val firstCol: Int = computeColumn(tileDelta, sector.minLongitude)
            val lastCol: Int = computeLastColumn(tileDelta, sector.maxLongitude)

            val firstRowLat = -90 + firstRow * tileDelta
            val firstRowLon = -180 + firstCol * tileDelta

            var lat = firstRowLat
            var lon: Double
            for (row in firstRow..lastRow) {
                lon = firstRowLon
                for (col in firstCol..lastCol) {
                    val tileSector = Sector(lat, lon, tileDelta, tileDelta)
                    val createTile = tileFactory.createTile(tileSector, level, row, col);
                    result.add(createTile)
                    lon += tileDelta
                }
                lat += tileDelta
            }
            return result
        }
    }

    var sector: Sector

    var level: Level

    var row = 0

    var column = 0

    var tileKey: String

    var extent: BoundingBox = BoundingBox()

    var heightLimits: FloatArray = FloatArray(2)

    var heightLimitsTimestamp: Long = 0

    var extentExaggeration: Double = 0.0

    var distanceToCamera = 0.0

    var texelSizeFactor = 0.0

    var nearestPoint: Vec3 = Vec3()

    constructor(sector: Sector?, level: Level?, row: Int, column: Int) {
        if (sector == null) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Tile",
                    "constructor",
                    "missingSector"
                )
            )
        }

        if (level == null) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Tile",
                    "constructor",
                    "missingLevel"
                )
            )
        }

        this.sector = sector
        this.level = level
        this.row = row
        this.column = column
        this.tileKey = level.levelNumber.toString() + "." + row + "." + column
        this.texelSizeFactor = Math.toRadians(level.tileDelta / level.tileWidth) * Math.cos(Math.toRadians(sector.centroidLatitude()))

    }

    /**
     * 指示此图块是否与指定的视锥相交。
     */
    open fun intersectsFrustum(rc: RenderContext, frustum: Frustum?): Boolean {
        if (frustum == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "intersectsFrustum", "missingFrustum")
            )
        }
        return this.getExtent(rc).intersectsFrustum(frustum);
    }

    open fun intersectsSector(sector: Sector?): Boolean {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "intersectsSector", "missingSector")
            )
        }
        return this.sector.intersects(sector)
    }

    /**
     * 根据眼睛和图块的距离判断是否需要细分
     */
    open fun mustSubdivide(rc: RenderContext, detailFactor: Double): Boolean {
        this.distanceToCamera = this.distanceToCamera(rc); //获取此图块和眼睛的距离 笛卡尔
        val texelSize: Double = texelSizeFactor * rc.globe.getEquatorialRadius()
        val pixelSize = rc.pixelSizeAtDistance(this.distanceToCamera)

        var densityFactor = 1.0
        // Adjust the subdivision factory when the display density is low. Values of detailFactor have been calibrated
        // against high density devices. Low density devices need roughly half the detailFactor.
        if (rc.resources!!.displayMetrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM) {
            densityFactor = 0.5
        }

        return texelSize > pixelSize * detailFactor * densityFactor
    }

    /**
     * 返回通过细分此图块而形成的四个子级。
     * 此图块的扇区分为四个象限，如下所示：西南； 东南; 西北; 东北。
     * 然后，为每个象限构造一个新的图块，并使用该图块的LevelSet中的下一个级别及其在该级别内对应的行和列进行配置。
     * 如果此图块的级别是其{@link LevelSet}中的最后一个级别，则返回null。
     */
    open fun subdivide(tileFactory: TileFactory?): Array<Tile?>? {
        if (tileFactory == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivide", "missingTileFactory")
            )
        }
        val childLevel = level.nextLevel() ?: return null
        val children: Array<Tile?> = arrayOfNulls<Tile>(4)

        /**
         * 1        1
         *   2   3
         *     y
         *   0   1
         * x        1
         *
         * x = latMin lonMin
         * y = latMid lonMid
         */
        val latMin: Double = sector.minLatitude
        val lonMin: Double = sector.minLongitude

        val latMid = sector.centroidLatitude()
        val lonMid = sector.centroidLongitude()

        val childDelta = level.tileDelta * 0.5

        var childsubRow = 2 * row
        var childsubCol = 2 * column
        var childSector: Sector = Sector(latMin, lonMin, childDelta, childDelta)
        children[0] = tileFactory.createTile(
            childSector,
            childLevel,
            childsubRow,
            childsubCol
        ) // Southwest 西南

        childsubRow = 2 * row
        childsubCol = 2 * column + 1
        childSector = Sector(latMin, lonMid, childDelta, childDelta)
        children[1] =
            tileFactory.createTile(childSector, childLevel, childsubRow, childsubCol) // Southeast东南

        childsubRow = 2 * row + 1
        childsubCol = 2 * column
        childSector = Sector(latMid, lonMin, childDelta, childDelta)
        children[2] =
            tileFactory.createTile(childSector, childLevel, childsubRow, childsubCol) // Northwest

        childsubRow = 2 * row + 1
        childsubCol = 2 * column + 1
        childSector = Sector(latMid, lonMid, childDelta, childDelta)
        children[3] =
            tileFactory.createTile(childSector, childLevel, childsubRow, childsubCol) // Northeast
        return children
    }

    /**
     * 返回通过细分此图块形成的四个子代，从指定的缓存中提取这些子代。
     * 在细分之前，会检查缓存中是否有子集合。
     * 如果缓存中存在一个，则将其返回，而不是创建一个新的子代集合。
     * 如果以与{@link #subdivide（TileFactory）}相同的方式创建新集合并将其添加到缓存中。
     */
    open fun subdivideToCache(
        tileFactory: TileFactory?,
        cache: LruMemoryCache<String?, Array<Tile?>?>?,
        cacheSize: Int
    ): Array<Tile?>? {
        if (tileFactory == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivideToCache", "missingTileFactory")
            )
        }
        if (cache == null) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivideToCache", "missingCache")
            )
        }

        var children: Array<Tile?>? = cache.get(tileKey)
        if (children == null) {
            children = subdivide(tileFactory)
            if (children != null) {
                cache.put(tileKey, children, cacheSize)
            }
        }
        return children
    }

    /**
     * 获取范围  获取该图块的 边界框
     */
    open fun getExtent(rc: RenderContext): BoundingBox {
        val elevationTimestamp: Long = rc.globe.elevationModel.getTimestamp()
        if (elevationTimestamp != heightLimitsTimestamp) {
            // initialize the heights for elevation model scan
            heightLimits[0] = Float.MAX_VALUE
            heightLimits[1] = -Float.MAX_VALUE
            rc.globe.elevationModel.getHeightLimits(sector, heightLimits)
            // check for valid height limits
            if (heightLimits[0] > heightLimits[1]) {
                Arrays.fill(heightLimits, 0f)
            }
        }

        val verticalExaggeration = rc.verticalExaggeration
        if (verticalExaggeration != extentExaggeration || elevationTimestamp != heightLimitsTimestamp) {
            val minHeight = (heightLimits[0] * verticalExaggeration).toFloat()
            val maxHeight = (heightLimits[1] * verticalExaggeration).toFloat()
            extent.setToSector(sector, rc.globe, minHeight, maxHeight)
        }
        heightLimitsTimestamp = elevationTimestamp
        extentExaggeration = verticalExaggeration
        return extent
    }

    override fun toString(): String {
        return "Tile(sector=$sector, level=$level, row=$row, column=$column, tileKey=$tileKey, extent=$extent)"
    }

    protected open fun distanceToCamera(rc: RenderContext): Double {
        // determine the nearest latitude
        val nearestLat: Double = WWMath.clamp(rc.camera.latitude, sector.minLatitude, sector.maxLatitude)
        // determine the nearest longitude and account for the antimeridian discontinuity
        val nearestLon: Double
        val lonDifference = rc.camera.longitude - sector.centroidLongitude()
        if (lonDifference < -180.0) {
            nearestLon = sector.maxLongitude
        } else if (lonDifference > 180.0) {
            nearestLon = sector.minLongitude
        } else {
            nearestLon = WWMath.clamp(rc.camera.longitude, sector.minLongitude, sector.maxLongitude)
        }

        val minHeight = (heightLimits[0] * rc.verticalExaggeration)
        rc.geographicToCartesian(
            nearestLat,
            nearestLon,
            minHeight,
            WorldWind.ABSOLUTE,
            nearestPoint
        )

        return rc.cameraPoint.distanceTo(nearestPoint)
    }
}