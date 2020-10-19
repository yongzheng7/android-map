package com.atom.map.ogc.gpkg

import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.atom.map.util.Logger
import com.atom.map.util.WWUtil
import java.util.concurrent.TimeUnit

class SQLiteConnection {

    companion object{
        protected const val CONNECTION_TIMEOUT = 1
    }
    protected var pathName: String

    protected var flags = 0

    protected var keepAliveTime: Long = 0

    protected var handler: Handler

    protected var database: SQLiteDatabase? = null

    protected val lock = Any()

    constructor(
        pathName: String,
        flags: Int,
        keepAliveTime: Long,
        unit: TimeUnit
    ) {

        this.pathName = pathName
        this.flags = flags
        this.keepAliveTime = unit.toMillis(keepAliveTime)
        this.handler = Handler(Looper.getMainLooper(),
                Handler.Callback { msg -> this@SQLiteConnection.handleMessage(msg) })
    }

    fun setKeepAliveTime(time: Long, unit: TimeUnit) {
        keepAliveTime = unit.toMillis(time)
        handler.removeMessages(CONNECTION_TIMEOUT)
        handler.sendEmptyMessageDelayed(
            CONNECTION_TIMEOUT,
            keepAliveTime
        )
    }

    fun openDatabase(): SQLiteDatabase {
        synchronized(lock) {
            if (database == null) {
                database = SQLiteDatabase.openDatabase(pathName, null, flags)
                Logger.logMessage(
                    Logger.INFO, "SQLiteConnection", "openDatabase",
                    "SQLite connection opened " + pathName
                )
            }
            database!!.acquireReference()
            handler.removeMessages(CONNECTION_TIMEOUT)
            handler.sendEmptyMessageDelayed(
                CONNECTION_TIMEOUT,
                keepAliveTime
            )
            return database!!
        }
    }

    protected fun onConnectionTimeout() {
        synchronized(lock) {
            WWUtil.closeSilently(database)
            Logger.logMessage(
                Logger.INFO, "SQLiteConnection", "onConnectionTimeout",
                "SQLite connection keep alive timeout " + pathName
            )
            if (database!!.isOpen) {
                Logger.logMessage(
                    Logger.WARN, "SQLiteConnection", "onConnectionTimeout",
                    "SQLite connection open after timeout " + pathName
                )
            }
            database = null
        }
    }

    protected fun handleMessage(msg: Message): Boolean {
        if (msg.what == CONNECTION_TIMEOUT) {
            onConnectionTimeout()
        }
        return false
    }

}