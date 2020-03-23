package com.atom.wyz.worldwind.globe

import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.util.Level

/**
 * Factory for delegating construction of {@link Tile} instances.
 * 用于委托构造{@link Tile}实例的工厂。
 */
interface TileFactory {

    /**
     * Returns a tile for a specified sector, level within a [LevelSet], and row and column within that level.
     *返回指定扇区，[LevelSet]内的级别以及该级别内的行和列的磁贴。
     * @param sector the sector spanned by the tile
     * @param level  the level at which the tile lies within a LevelSet
     * @param row    the row within the specified level
     * @param column the column within the specified level
     *
     * @return a tile constructed with the specified arguments
     *
     * @throws IllegalArgumentException if either the sector or the level is null
     */
    fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile
}