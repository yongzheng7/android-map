package com.atom.wyz.worldwind.globe

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.util.Logger
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

/**
 * 基本地形
 */
class BasicTerrain() : Terrain {

    protected var tiles = ArrayList<TerrainTile>()

    override var sector: Sector = Sector()

    var tileTexCoords: FloatBuffer? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var tileTriStripIndices: ShortBuffer? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var tileLineIndices: ShortBuffer? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    fun addTile(tile: TerrainTile?) {
        if (tile == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile"))
        }
        tiles.add(tile)
        sector.union(tile.sector)
    }

    fun clearTiles() {
        tiles.clear()
        sector.setEmpty()
    }

    override fun getTileCount(): Int = tiles.size

    override fun getTileSector(index: Int): Sector? {
        if (index < 0 || index >= tiles.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "getTileSector", "invalidIndex"))
        }

        return tiles[index].sector
    }

    override fun getTileVertexOrigin(index: Int): Vec3? {
        if (index < 0 || index >= tiles.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "getTileOrigin", "invalidIndex"))
        }

        return tiles[index].tileOrigin
    }

    override fun useVertexPointAttrib(dc: DrawContext?, index: Int, attribLocation: Int) {
        if (index < 0 || index >= tiles.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "useTileVertexPointAttrib", "invalidIndex"))
        }

        val buffer: Buffer? = tiles[index].tileVertices
        if (buffer != null) {
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, buffer)
        }
    }

    override fun useVertexTexCoordAttrib(dc: DrawContext?, attribLocation: Int) {
        val buffer: Buffer? = tileTexCoords
        if (buffer != null) {
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, buffer)
        }
    }

    override fun drawTileTriangles(dc: DrawContext?, index: Int) {
        if (index < 0 || index >= tiles.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "drawTileTriangles", "invalidIndex"))
        }

        val buffer: Buffer? = tileTriStripIndices
        if (buffer != null) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, buffer.remaining(), GLES20.GL_UNSIGNED_SHORT, buffer)
        }
    }

    override fun drawTileLines(dc: DrawContext?, index: Int) {
        if (index < 0 || index >= tiles.size) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "drawTileTriangles", "invalidIndex"))
        }

        val buffer: Buffer? = tileLineIndices
        if (buffer != null) {
            GLES20.glDrawElements(GLES20.GL_LINES, buffer.remaining(), GLES20.GL_UNSIGNED_SHORT, buffer)
        }
    }

    override fun geographicToCartesian(latitude: Double, longitude: Double, altitude: Double, altitudeMode: Int, result: Vec3?): Vec3? {
        if (result == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult"))
        }
        return null // TODO
    }

}