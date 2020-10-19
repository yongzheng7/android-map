package com.atom.map.tiff

import com.atom.map.util.Logger

enum class Type {
     UBYTE,
     ASCII,
     USHORT,
     ULONG,
     RATIONAL,
     SBYTE,
     UNDEFINED,
     SSHORT,
     SLONG,
     SRATIONAL,
     FLOAT,
     DOUBLE ; 
    
    companion object{
        fun decode(type: Int): Type {
            return when (type) {
                1 -> UBYTE
                2 -> ASCII
                3 -> USHORT
                4 -> ULONG
                5 -> RATIONAL
                6 -> SBYTE
                7 -> UNDEFINED
                8 -> SSHORT
                9 -> SLONG
                10 -> SRATIONAL
                11 -> FLOAT
                12 -> DOUBLE
                else -> throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Type", "decode", "invalid type")
                )
            }
        }
    }

    open fun getSizeInBytes(): Int {
        return when (this) {
            UBYTE -> 1
            ASCII -> 1
            USHORT -> 2
            ULONG -> 4
            RATIONAL -> 8
            SBYTE -> 1
            UNDEFINED -> 1
            SSHORT -> 2
            SLONG -> 4
            SRATIONAL -> 8
            FLOAT -> 4
            DOUBLE -> 8
            else -> throw RuntimeException(
                Logger.logMessage(Logger.ERROR, "Type", "getSizeInBytes", "invalid type")
            )
        }
    }

    open fun getSpecificationTag(): Int {
        return when (this) {
            UBYTE -> 1
            ASCII -> 2
            USHORT -> 3
            ULONG -> 4
            RATIONAL -> 5
            SBYTE -> 6
            UNDEFINED -> 7
            SSHORT -> 8
            SLONG -> 9
            SRATIONAL -> 10
            FLOAT -> 11
            DOUBLE -> 12
            else -> throw RuntimeException(
                Logger.logMessage(Logger.ERROR, "Type", "getSizeInBytes", "invalid type")
            )
        }
    }
}