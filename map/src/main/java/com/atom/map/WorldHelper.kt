package com.atom.map

import com.atom.map.globe.Globe
import com.atom.map.layer.LayerList
import com.atom.map.navigator.Navigator

interface WorldHelper  {

    fun pixelSizeAtDistance(distance: Double): Double

    fun globe() : Globe

    fun navigator() : Navigator

    fun requestRedraw()

    fun distanceToViewGlobeExtents(): Double

    fun getMapLayers() : LayerList

    fun getWidth():Int

    fun getHeight():Int
}