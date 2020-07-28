package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector

interface SurfaceTexture {

    var sector : Sector

    var texCoordTransform: Matrix3

    fun bindTexture(dc: DrawContext): Boolean

}