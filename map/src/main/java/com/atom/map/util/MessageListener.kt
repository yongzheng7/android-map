package com.atom.map.util

interface MessageListener {
    fun onMessage(name: String?, sender: Any?, userProperties: Map<Any?, Any?>?)
}