package com.atom.wyz.worldwind.util

class Level {

    var parent: LevelSet

    var levelNumber = 0

    var tileDelta = 0.0

    var tileWidth = 0

    var tileHeight = 0

    /**
     * 此级别内像素或高程单元的大小，以每像素（或每个单元）的弧度为单位。
     */
    var texelHeight = 0.0


    constructor(parent: LevelSet?, levelNumber: Int, tileDelta: Double) {
        if (parent == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Level", "constructor", "The parent level set is null"))
        }
        if (tileDelta <= 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Level", "constructor", "The tile delta is zero"))
        }
        this.parent = parent
        this.levelNumber = levelNumber
        this.tileDelta = tileDelta
        tileWidth = parent.tileWidth
        tileHeight = parent.tileHeight
        texelHeight = Math.toRadians(tileDelta) / parent.tileHeight
    }

    override fun toString(): String {
        return "Level( levelNumber=$levelNumber, tileDelta=$tileDelta, tileWidth=$tileWidth, tileHeight=$tileHeight, texelHeight=$texelHeight)"
    }

    /**
     * 指示此级别是否是父级别集中的最低分辨率级别（级别0）。
     */
    fun isFirstLevel(): Boolean {
        return levelNumber == 0
    }

    /**
     * 指示此级别是否是父级别集内的最高分辨率。
     */
    fun isLastLevel(): Boolean {
        return levelNumber == parent.numLevels() - 1
    }

    /**
     * 返回其序号紧接在父级集中的该级别序号之前的级别；如果这是第一级，则返回null。
     */
    fun previousLevel(): Level? {
        return parent.level(levelNumber - 1)
    }

    /**
     * 返回其级别在父级别集中的该级别的序号之后立即发生的级别；如果这是最后一个级别，则返回null。
     */
    fun nextLevel(): Level? {
        return parent.level(levelNumber + 1)
    }



}