package com.atom.wyz.worldwind.util

sealed class BooleanExt<out T>

object Otherwise : BooleanExt<Nothing>()

class TransferData<T>(val data: T) : BooleanExt<T>()

inline fun <T> Boolean.yes(block: () -> T): BooleanExt<T> = when {//T处于函数返回值位置
    this -> {
        TransferData(block.invoke())
    }
    else -> Otherwise//注意: 此处是编译不通过的
}

inline fun <T> BooleanExt<T>.otherwise(block: () -> T): T = when (this) {//T处于函数返回值位置
    is Otherwise -> block()
    is TransferData -> this.data
}