package com.atom.wyz.worldwind.ogc.gpkg

import java.util.*

class GpkgTileUserMetrics() : GpkgEntry() {
     var zoomLevels: IntArray ?= null
    set(value) {
        field = value
        Arrays.sort(field!!)
    }

    fun getMinZoomLevel(): Int {
        val len = zoomLevels ?.size ?: return -1
        return if (len == 0) -1 else zoomLevels!![0]
    }

    fun getMaxZoomLevel(): Int {
        val len = zoomLevels ?.size ?: return -1
        return if (len == 0) -1 else zoomLevels!![len - 1]
    }

    fun hasZoomLevel(zoomLevel: Int): Boolean {
        return Arrays.binarySearch(zoomLevels, zoomLevel) >= 0
    }
}