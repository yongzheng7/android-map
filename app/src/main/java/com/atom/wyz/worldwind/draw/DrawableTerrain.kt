package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3

interface DrawableTerrain : Drawable {

    var sector : Sector

    var vertexOrigin : Vec3

    fun useVertexPointAttrib(dc: DrawContext, attribLocation: Int)

    fun useVertexTexCoordAttrib(dc: DrawContext, attribLocation: Int)

    fun drawLines(dc: DrawContext)

    fun drawTriangles(dc: DrawContext)
}