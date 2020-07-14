package com.atom.wyz.worldwind.util

import com.atom.wyz.worldwind.geom.Sector
import kotlin.math.floor
import kotlin.math.ln

class LevelSetConfig {

    /**
     * The sector spanned by the level set.
     */
    val sector: Sector? = Sector().setFullSphere()

    /**
     * The geographic width and height in degrees of tiles in the first level (lowest resolution) of the level set
     * 水平集的第一层（最低分辨率）中瓷砖的地理宽度和高度（以度为单位）
     */
    var firstLevelDelta = 90.0

    /**
     * The number of levels in the level set.
     */
    var numLevels = 1

    /**
     * The width in pixels of images associated with tiles in the level set, or the number of sample points in the
     * longitudinal direction of elevation tiles associated with the level set.
     */
    var tileWidth = 256

    /**
     * The height in pixels of images associated with tiles in the level set, or the number of sample points in the
     * latitudinal direction of elevation tiles associated with the level set.
     */
    var tileHeight = 256

    /**
     * Constructs a level set configuration with default values.
     * sector = -90 to +90 latitude and -180 to +180 longitude
     * firstLevelDelta = 90 degrees
     * numLevels = 1
     * tileWidth = 256
     * tileHeight = 256
     */
    constructor() {}

    constructor(sector: Sector?, firstLevelDelta: Double, numLevels: Int, tileWidth: Int, tileHeight: Int) {
        if (sector != null) {
            this.sector?.set(sector)
        }
        this.firstLevelDelta = firstLevelDelta
        this.numLevels = numLevels
        this.tileWidth = tileWidth
        this.tileHeight = tileHeight
    }


    /**
     * Returns the number of levels necessary to achieve the specified resolution. The result is correct for this
     * configuration's current firstLevelDelta, tileWidth and tileHeight, and is invalid if any of these values change.
     * 返回实现指定分辨率弧度为单位所需的级别数。
     * 结果对于此配置的当前firstLevelDelta，tileWidth和tileHeight是正确的，
     * 并且如果这些值中的任何一个发生更改，则该结果将无效。
     * @param resolution the desired resolution in pixels per degree
     *
     * @return the number of levels
     *
     * @throws IllegalArgumentException If the resolution is not positive
     */
    fun numLevelsForResolution(radiansPerPixel: Double): Int {
        if (radiansPerPixel <= 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LevelSetConfig", "setNumLevelsForResolution", "invalidResolution"))
        }

        val degreesPerPixel = Math.toDegrees(radiansPerPixel)

        val firstLevelDegreesPerPixel = firstLevelDelta / Math.min(tileWidth, tileHeight)

        val level = Math.log(firstLevelDegreesPerPixel / degreesPerPixel) / Math.log(2.0) // fractional level address

        var levelNumber = Math.ceil(level).toInt()

        if (levelNumber < 0) {
            levelNumber = 0 // need at least one level, even if it exceeds the desired resolution
        }
        return levelNumber + 1
    }

    fun numLevelsForMinResolution(radiansPerPixel: Double): Int {
        require(radiansPerPixel > 0) {
            Logger.logMessage(
                Logger.ERROR,
                "LevelSetConfig",
                "numLevelsForMinResolution",
                "invalidResolution"
            )
        }
        val degreesPerPixel = Math.toDegrees(radiansPerPixel)
        val firstLevelDegreesPerPixel = firstLevelDelta / tileHeight
        val level =
            ln(firstLevelDegreesPerPixel / degreesPerPixel) / ln(2.0) // fractional level address
        var levelNumber =
            floor(level).toInt() // floor prevents exceeding the min scale
        if (levelNumber < 0) {
            levelNumber = 0 // need at least one level, even if it exceeds the desired resolution
        }
        return levelNumber + 1 // convert level number to level count
    }
}