package com.atom.map.util

import kotlin.random.Random

class StringUtils {
    companion object{
        private const val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        fun getRandomString(length: Int): String {
            val random = Random.Default
            val sb = StringBuffer()
            for (i in 0 until length) {
                val number: Int = random.nextInt(62)
                sb.append(str[number])
            }
            return sb.toString()
        }
    }
}