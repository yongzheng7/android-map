package com.atom.wyz.worldwind.core.api

interface ICloseable {
    @Throws(Exception::class)
    fun close()
}