package com.atom.map.drawable

import com.atom.map.geom.Matrix3
import com.atom.map.geom.SimpleColor
import com.atom.map.geom.Vec3
import com.atom.map.core.shader.BasicProgram
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.shader.GpuTexture

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

    var color: SimpleColor =
        SimpleColor()

    var lineWidth = 1f

    var depthOffset = 0.0

    var primCount = 0

    var texture: GpuTexture? = null

    var texCoordMatrix: Matrix3 = Matrix3()

    var texCoordAttrib: VertexAttrib =
        VertexAttrib()

    var prims: Array<DrawElements>

    constructor() {
        prims = arrayOf(
            DrawElements(),
            DrawElements(),
            DrawElements(),
            DrawElements()
        )
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

    fun color(color: SimpleColor) {
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
        var color = SimpleColor()
        var lineWidth = 0f
        var texture: GpuTexture? = null
        var texCoordMatrix = Matrix3()
        var texCoordAttrib =
            VertexAttrib()
        override fun toString(): String {
            return "DrawElements(mode=$mode, count=$count, type=$type, offset=$offset, color=$color, lineWidth=$lineWidth, texture=$texture, texCoordMatrix=$texCoordMatrix, texCoordAttrib=$texCoordAttrib)"
        }
    }


    class VertexAttrib {
        var size = 0
        var offset = 0
        override fun toString(): String {
            return "VertexAttrib(size=$size, offset=$offset)"
        }
    }
}