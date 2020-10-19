package com.atom.map.core.api

interface IObserver<T> {
    fun run(type: T)
}