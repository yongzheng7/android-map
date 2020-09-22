package com.atom.wyz.worldwind

import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.navigator.Navigator

interface WorldHelper  {

    fun pixelSizeAtDistance(distance: Double): Double

    fun globe() : Globe

    fun navigator() : Navigator

    fun requestRedraw()

    fun distanceToViewGlobeExtents(): Double

    fun getWidth():Int

    fun getHeight():Int
}