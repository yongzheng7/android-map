package com.atom.wyz.worldwind.util

interface MessageListener {
    fun onMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?)
}