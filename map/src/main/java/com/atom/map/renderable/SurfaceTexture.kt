package com.atom.map.renderable

import com.atom.map.geom.Matrix3
import com.atom.map.geom.Sector
import com.atom.map.drawable.DrawContext

interface SurfaceTexture {

    var sector : Sector

    var texCoordTransform: Matrix3

    fun bindTexture(dc: DrawContext): Boolean

}