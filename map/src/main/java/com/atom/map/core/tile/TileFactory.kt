package com.atom.map.core.tile

import com.atom.map.geom.Sector
import com.atom.map.util.Level

/**
 * Factory for delegating construction of {@link Tile} instances.
 * 用于委托构造{@link Tile}实例的工厂。
 */
interface TileFactory {
    fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile
}