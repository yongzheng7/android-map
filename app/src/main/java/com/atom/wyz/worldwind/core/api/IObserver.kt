package com.atom.wyz.worldwind.core.api

interface IObserver<T> {
    fun run(type: T)
}