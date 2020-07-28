package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.context.DrawContext
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.Vec3

interface DrawableTerrain : Drawable {

    var sector : Sector

    var vertexOrigin : Vec3

    fun useVertexPointAttrib(dc: DrawContext, attribLocation: Int) : Boolean

    fun useVertexTexCoordAttrib(dc: DrawContext, attribLocation: Int) : Boolean

    fun drawLines(dc: DrawContext) : Boolean

    fun drawTriangles(dc: DrawContext) : Boolean
}