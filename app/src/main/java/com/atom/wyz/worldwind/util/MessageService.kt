package com.atom.wyz.worldwind.util

import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.MessageListener
import java.lang.IllegalArgumentException
import java.util.*

class MessageService {
    protected var listenerList = ArrayList<MessageListener>()

    constructor()

    fun addListener(listener: MessageListener?) {
        if (listener == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "MessageService", "addListener", "missingListener"))
        }
        synchronized(this) { listenerList.add(listener) }
    }

    fun removeListener(listener: MessageListener?) {
        if (listener == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "MessageService", "removeListener", "missingListener"))
        }
        synchronized(this) { listenerList.remove(listener) }
    }

    fun postMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?) {
        if (name == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "MessageService", "postMessage", "missingName"))
        }
        synchronized(this) {
            for (listener in listenerList) {
                listener.onMessage(name, sender, userProperties)
            }
        }
    }
}