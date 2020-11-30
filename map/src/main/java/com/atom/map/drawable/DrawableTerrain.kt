package com.atom.map.drawable

import com.atom.map.geom.Sector
import com.atom.map.geom.Vec3

interface DrawableTerrain : Drawable {

    var sector : Sector

    var vertexOrigin : Vec3

    fun useVertexPointAttrib(dc: DrawContext, attribLocation: Int) : Boolean

    fun useVertexTexCoordAttrib(dc: DrawContext, attribLocation: Int) : Boolean

    fun drawLines(dc: DrawContext) : Boolean

    fun drawTriangles(dc: DrawContext) : Boolean
}