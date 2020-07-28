package com.atom.wyz.worldwind.draw

import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.GpuTexture

class DrawShapeState {
    companion object {
        const val MAX_DRAW_ELEMENTS = 4
    }

    var program: BasicProgram? = null

    var vertexBuffer: BufferObject? = null

    var elementBuffer: BufferObject? = null

    var vertexOrigin: Vec3 = Vec3()

    var vertexStride = 0

    var enableCullFace = true

    var enableDepthTest = true

    var color: Color = Color()

    var lineWidth = 1f

    var depthOffset = 0.0

    var primCount = 0

    var texture: GpuTexture? = null

    var texCoordMatrix: Matrix3 = Matrix3()

    var texCoordAttrib: VertexAttrib = VertexAttrib()

    var prims: Array<DrawElements>

    constructor() {
        val tempList = mutableListOf<DrawElements>()
        for (idx in 0 until MAX_DRAW_ELEMENTS) {
            tempList.add(idx, DrawElements());
        }
        prims = tempList.toTypedArray()
    }

    fun reset() {
        program = null
        vertexBuffer = null
        elementBuffer = null
        vertexOrigin.set(0.0, 0.0, 0.0)
        vertexStride = 0
        color.set(1f, 1f, 1f, 1f)
        enableDepthTest = true
        enableCullFace = true
        depthOffset = 0.0
        lineWidth = 1f
        primCount = 0
        texture = null
        texCoordMatrix.setToIdentity()
        texCoordAttrib.size = 0
        texCoordAttrib.offset = 0
        for (idx in 0 until MAX_DRAW_ELEMENTS) {
            prims[idx].texture = null
        }
    }

    fun color(color: Color) {
        this.color.set(color)
    }

    fun lineWidth(width: Float) {
        lineWidth = width
    }

    fun drawElements(mode: Int, count: Int, type: Int, offset: Int) {
        val prim = prims[primCount++]
        prim.mode = mode
        prim.count = count
        prim.type = type
        prim.offset = offset
        prim.color.set(color)
        prim.lineWidth = lineWidth
        prim.texture = texture
        prim.texCoordMatrix.set(texCoordMatrix)
        prim.texCoordAttrib.size = texCoordAttrib.size
        prim.texCoordAttrib.offset = texCoordAttrib.offset
    }

    class DrawElements {
        var mode = 0
        var count = 0
        var type = 0
        var offset = 0
        var color = Color()
        var lineWidth = 0f
        var texture: GpuTexture? = null
        var texCoordMatrix = Matrix3()
        var texCoordAttrib = VertexAttrib()
    }


    class VertexAttrib {
        var size = 0
        var offset = 0
    }
}