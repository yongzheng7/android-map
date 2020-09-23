package com.atom.wyz.worldwind.layer.render

import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.layer.draw.DrawContext

interface SurfaceTexture {

    var sector : Sector

    var texCoordTransform: Matrix3

    fun bindTexture(dc: DrawContext): Boolean

}