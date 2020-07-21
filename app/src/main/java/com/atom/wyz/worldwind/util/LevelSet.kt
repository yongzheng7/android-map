package com.atom.wyz.worldwind.util

import com.atom.wyz.worldwind.geom.Sector
import kotlin.math.pow

class LevelSet {
    /**
     * 该级别集所跨越的扇区。
     */
    var sector: Sector = Sector()

    /**
     * 此级别集的第一个级别（最低分辨率）中图块的地理宽度和高度（以度为单位）。
     * 就是在第一个级别中图块的边长以度为单位
     */
    var firstLevelDelta = 0.0

    /**
     * 该图块tile 采样点宽度 16 宽度划分为16行
     */
    var tileWidth = 0

    /**
     * 高度划分16 列  一共 256个点
     */
    var tileHeight = 0

    /**
     * The hierarchical levels, sorted from lowest to highest resolution.
     */
    var levels: Array<Level?>

    constructor() {
        firstLevelDelta = 0.0
        tileWidth = 0
        tileHeight = 0
        levels = arrayOfNulls<Level>(0)
    }

    constructor(sector: Sector, firstLevelDelta: Double, numLevels: Int, tileWidth: Int, tileHeight: Int) {
        if (firstLevelDelta <= 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidTileDelta")
            )
        }

        if (numLevels < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidNumLevels")
            )
        }

        if (tileWidth < 1 || tileHeight < 1) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidWidthOrHeight")
            )
        }

        this.sector = sector
        this.firstLevelDelta = firstLevelDelta
        this.tileWidth = tileWidth
        this.tileHeight = tileHeight
        levels = arrayOfNulls(numLevels)
        this.assembleLevels()
    }


    constructor(config: LevelSetConfig) {
        if (config.firstLevelDelta <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidTileDelta")
            )
        }
        if (config.numLevels < 1) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidNumLevels")
            )
        }
        if (config.tileWidth < 1 || config.tileHeight < 1) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidWidthOrHeight")
            )
        }
        sector.set(config.sector)
        firstLevelDelta = config.firstLevelDelta
        tileWidth = config.tileWidth
        tileHeight = config.tileHeight
        levels = arrayOfNulls<Level>(config.numLevels)
        this.assembleLevels()
    }

    /**
     * 组装等级 及其分辨率 等
     */
    protected fun assembleLevels() {
        // 0   1    90/1 单位 度
        // 1   2    90/2
        // 2   4    90/4
        // 3   8    90/8
        // 4  16    90/16
        // ...
        for (idx in levels.indices) {
            val n = 2.0.pow(idx.toDouble())
            val delta = firstLevelDelta / n
            levels[idx] = Level(this, idx, delta)
        }
    }

    /**
     * Returns the number of levels in this level set.
     *
     * @return the number of levels
     */
    fun numLevels(): Int {
        return levels.size
    }

    /**
     * Returns the [Level] for a specified level number.
     *
     * @param levelNumber the number of the desired level
     *
     * @return the requested level, or null if the level does not exist.
     */
    fun level(levelNumber: Int): Level? {
        return if (levelNumber < 0 || levelNumber >= levels.size) {
            null
        } else {
            levels[levelNumber]
        }
    }

    /**
     * Returns the first (lowest resolution) level of this level set.
     *
     * @return the level of lowest resolution
     */
    fun firstLevel(): Level? {
        return if (levels.size > 0) levels[0] else null
    }

    /**
     * Returns the last (highest resolution) level of this level set.
     *
     * @return the level of highest resolution
     */
    fun lastLevel(): Level? {
        return if (levels.size > 0) levels[levels.size - 1] else null
    }

    /**
     * Returns the level that most closely approximates the specified resolution.
     * 返回最接近指定分辨率的级别。
     * @param resolution the desired resolution in pixels per degree
     *
     * @return the level for the specified resolution, or null if this level set is empty
     *
     * @throws IllegalArgumentException If the resolution is not positive
     */
    open fun levelForResolution(radiansPerPixel: Double): Level? {
        if (radiansPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSetConfig", "levelForResolution", "invalidResolution")
            )
        }
        if (levels.size == 0) {
            return null // this level set is empty
        }
        val degreesPerPixel = Math.toDegrees(radiansPerPixel)

        val firstLevelDegreesPerPixel = firstLevelDelta / Math.min(tileWidth, tileHeight)

        val level = Math.log(firstLevelDegreesPerPixel / degreesPerPixel) / Math.log(2.0) // fractional level address

        val levelNumber = Math.round(level).toInt() // nearest neighbor level

        return if (levelNumber < 0) {
            levels[0] // unable to match the resolution; return the first level
        } else if (levelNumber < levels.size) {
            levels[levelNumber] // nearest neighbor level is in this level set
        } else {
            levels[levels.size - 1] // unable to match the resolution; return the last level
        }
    }

    override fun toString(): String {
        return "LevelSet(sector=$sector, topLevelDelta=$firstLevelDelta, tileWidth=$tileWidth, tileHeight=$tileHeight, levels=${levels.contentToString()})"
    }


}