package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.DrawContext

interface SurfaceTile {

    var sector : Sector

    fun bindTexture(dc: DrawContext ): Boolean

    fun applyTexCoordTransform(dc: DrawContext, result: Matrix3): Boolean
}