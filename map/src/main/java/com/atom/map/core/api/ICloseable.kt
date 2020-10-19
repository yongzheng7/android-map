package com.atom.map.core.api

interface ICloseable {
    @Throws(Exception::class)
    fun close()
}