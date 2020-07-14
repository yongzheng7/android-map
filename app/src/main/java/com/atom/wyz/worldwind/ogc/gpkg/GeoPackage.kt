package com.atom.wyz.worldwind.ogc.gpkg

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.SparseArray
import android.util.SparseIntArray
import com.atom.wyz.worldwind.util.WWUtil
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class GeoPackage {
    protected var connection: SQLiteConnection

    protected var spatialReferenceSystems: MutableList<GpkgSpatialReferenceSystem> =
        ArrayList<GpkgSpatialReferenceSystem>()

    protected var contents: MutableList<GpkgContents> =
        ArrayList<GpkgContents>()

    protected var tileMatrixSets: MutableList<GpkgTileMatrixSet> =
        ArrayList<GpkgTileMatrixSet>()

    protected var tileMatrices: MutableList<GpkgTileMatrix> =
        ArrayList()

    protected var srsIdIndex = SparseIntArray()

    protected var tileMatrixSetIndex: HashMap<String, Int> =
        HashMap()

    protected var tileMatrixIndex: HashMap<String, SparseArray<GpkgTileMatrix>> =
        HashMap()


    constructor(pathName: String) {
        connection = SQLiteConnection(
            pathName,
            SQLiteDatabase.OPEN_READONLY,
            60,
            TimeUnit.SECONDS
        )
        // TODO verify its a GeoPackage container
        // TODO select specific columns
        // TODO parameterize table names and column names as constants
        this.readSpatialReferenceSystems()
        this.readContents()
        this.readTileMatrixSets()
        this.readTileMatrices()
    }

    fun getSpatialReferenceSystem(id: Int): GpkgSpatialReferenceSystem? {
        val index = srsIdIndex[id, -1]
        // -1 if not found; the default is 0, a valid index
        return if (index < 0) null else spatialReferenceSystems[index]
    }
    fun getTileMatrixSet(tableName: String?): GpkgTileMatrixSet? {
        val index = tileMatrixSetIndex[tableName]
        return if (index == null) null else tileMatrixSets[index]
    }

    fun getTileMatrices(tableName: String): SparseArray<GpkgTileMatrix>? {
        return tileMatrixIndex.get(tableName)
    }

    fun getTileUserData(
        tiles: GpkgContents?,
        zoomLevel: Int,
        tileColumn: Int,
        tileRow: Int
    ): GpkgTileUserData? {
        return if (tiles == null) null else this.readTileUserData(
            tiles.tableName!!,
            zoomLevel,
            tileColumn,
            tileRow
        )
    }

    protected fun readSpatialReferenceSystems() {
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            database = connection.openDatabase()
            cursor = database.rawQuery("SELECT * FROM gpkg_spatial_ref_sys", null /*selectionArgs*/)
            val srs_name = cursor.getColumnIndex("srs_name")
            val srs_id = cursor.getColumnIndex("srs_id")
            val organization = cursor.getColumnIndex("organization")
            val organization_coordsys_id = cursor.getColumnIndex("organization_coordsys_id")
            val definition = cursor.getColumnIndex("definition")
            val description = cursor.getColumnIndex("description")
            while (cursor.moveToNext()) {
                val srs = GpkgSpatialReferenceSystem()
                srs.container = (this)
                srs.srsName = (cursor.getString(srs_name))
                srs.srsId = (cursor.getInt(srs_id))
                srs.organization = (cursor.getString(organization))
                srs.organizationCoordSysId = (cursor.getInt(organization_coordsys_id))
                srs.definition = (cursor.getString(definition))
                srs.description = (cursor.getString(description))
                val index = spatialReferenceSystems.size
                spatialReferenceSystems.add(srs)
                srsIdIndex.put(srs.srsId, index)
            }
        } finally {
            WWUtil.closeSilently(cursor)
            WWUtil.closeSilently(database)
        }
    }

    protected fun readContents() {
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            database = connection.openDatabase()
            cursor = database.rawQuery("SELECT * FROM gpkg_contents", null /*selectionArgs*/)
            val table_name = cursor.getColumnIndex("table_name")
            val data_type = cursor.getColumnIndex("data_type")
            val identifier = cursor.getColumnIndex("identifier")
            val description = cursor.getColumnIndex("description")
            val last_change = cursor.getColumnIndex("last_change")
            val min_x = cursor.getColumnIndex("min_x")
            val min_y = cursor.getColumnIndex("min_y")
            val max_x = cursor.getColumnIndex("max_x")
            val max_y = cursor.getColumnIndex("max_y")
            val srs_id = cursor.getColumnIndex("srs_id")
            while (cursor.moveToNext()) {
                val contents = GpkgContents()
                contents.container = (this)
                contents.tableName = (cursor.getString(table_name))
                contents.dataType = (cursor.getString(data_type))
                contents.identifier = (cursor.getString(identifier))
                contents.description = (cursor.getString(description))
                contents.lastChange = (cursor.getString(last_change))
                contents.minX = (cursor.getDouble(min_x))
                contents.minY = (cursor.getDouble(min_y))
                contents.maxX = (cursor.getDouble(max_x))
                contents.maxY = (cursor.getDouble(max_y))
                contents.srsId = (cursor.getInt(srs_id))
                this.contents.add(contents)
            }
        } finally {
            WWUtil.closeSilently(cursor)
            WWUtil.closeSilently(database)
        }
    }

    protected fun readTileMatrixSets() {
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            database = connection.openDatabase()
            cursor = database.rawQuery("SELECT * FROM gpkg_tile_matrix_set", null /*selectionArgs*/)
            val table_name = cursor.getColumnIndex("table_name")
            val srs_id = cursor.getColumnIndex("srs_id")
            val min_x = cursor.getColumnIndex("min_x")
            val min_y = cursor.getColumnIndex("min_y")
            val max_x = cursor.getColumnIndex("max_x")
            val max_y = cursor.getColumnIndex("max_y")
            while (cursor.moveToNext()) {
                val tileMatrixSet = GpkgTileMatrixSet()
                tileMatrixSet.container = (this)
                tileMatrixSet.tableName = (cursor.getString(table_name))
                tileMatrixSet.srsId = (cursor.getInt(srs_id))
                tileMatrixSet.minX = (cursor.getDouble(min_x))
                tileMatrixSet.minY = (cursor.getDouble(min_y))
                tileMatrixSet.maxX = (cursor.getDouble(max_x))
                tileMatrixSet.maxY = (cursor.getDouble(max_y))
                val index = tileMatrixSets.size
                tileMatrixSets.add(tileMatrixSet)
                tileMatrixSetIndex[tileMatrixSet.tableName] = index
                tileMatrixIndex[tileMatrixSet.tableName] = SparseArray()
            }
        } finally {
            WWUtil.closeSilently(cursor)
            WWUtil.closeSilently(database)
        }
    }

    protected fun readTileMatrices() {
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            database = connection.openDatabase()
            cursor = database.rawQuery("SELECT * FROM gpkg_tile_matrix", null /*selectionArgs*/)
            val table_name = cursor.getColumnIndex("table_name")
            val zoom_level = cursor.getColumnIndex("zoom_level")
            val matrix_width = cursor.getColumnIndex("matrix_width")
            val matrix_height = cursor.getColumnIndex("matrix_height")
            val tile_width = cursor.getColumnIndex("tile_width")
            val tile_height = cursor.getColumnIndex("tile_height")
            val pixel_x_size = cursor.getColumnIndex("pixel_x_size")
            val pixel_y_size = cursor.getColumnIndex("pixel_y_size")
            while (cursor.moveToNext()) {
                val tileMatrix = GpkgTileMatrix()
                tileMatrix.container = (this)
                tileMatrix.tableName = (cursor.getString(table_name))
                tileMatrix.zoomLevel = (cursor.getInt(zoom_level))
                tileMatrix.matrixWidth = (cursor.getInt(matrix_width))
                tileMatrix.matrixHeight = (cursor.getInt(matrix_height))
                tileMatrix.tileWidth = (cursor.getInt(tile_width))
                tileMatrix.tileHeight = (cursor.getInt(tile_height))
                tileMatrix.pixelXSize = (cursor.getDouble(pixel_x_size))
                tileMatrix.pixelYSize = (cursor.getDouble(pixel_y_size))
                tileMatrices.add(tileMatrix)
                tileMatrixIndex[tileMatrix.tableName]?.put(tileMatrix.zoomLevel, tileMatrix)
            }
        } finally {
            WWUtil.closeSilently(cursor)
            WWUtil.closeSilently(database)
        }
    }

    protected fun readTileUserData(
        tableName: String,
        zoomLevel: Int,
        tileColumn: Int,
        tileRow: Int
    ): GpkgTileUserData? {
        // TODO SQLiteDatabase is ambiguous on whether the call to rawQuery and Cursor usage are thread safe
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        return try {
            val selectionArgs = arrayOf(
                zoomLevel.toString(),
                tileColumn.toString(),
                tileRow.toString()
            )
            database = connection.openDatabase()
            cursor = database.rawQuery(
                "SELECT * FROM $tableName WHERE zoom_level=? AND tile_column=? AND tile_row=? LIMIT 1",
                selectionArgs
            )
            val id = cursor.getColumnIndex("id")
            val zoom_level = cursor.getColumnIndex("zoom_level")
            val tile_column = cursor.getColumnIndex("tile_column")
            val tile_row = cursor.getColumnIndex("tile_row")
            val tile_data = cursor.getColumnIndex("tile_data")
            if (cursor.moveToNext()) {
                val userData = GpkgTileUserData()
                userData.container = (this)
                userData.id = (cursor.getInt(id))
                userData.zoomLevel = (cursor.getInt(zoom_level))
                userData.tileColumn = (cursor.getInt(tile_column))
                userData.tileRow = (cursor.getInt(tile_row))
                userData.tileData = (cursor.getBlob(tile_data))
                userData
            } else {
                null
            }
        } finally {
            WWUtil.closeSilently(cursor)
            WWUtil.closeSilently(database)
        }
    }

}