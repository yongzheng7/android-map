package com.atom.map.layer

import android.graphics.Color
import android.opengl.GLES20
import com.atom.map.WorldWind
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.shader.CartesianProgram
import com.atom.map.geom.SimpleColor
import com.atom.map.geom.Vec3
import com.atom.map.drawable.DrawableCartesian
import com.atom.map.renderable.RenderContext
import com.atom.map.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CartesianLayer : AbstractLayer("CartesianLayer") {

    companion object {
        private val VERTEX_POINTS_KEY = CartesianLayer::class.java.name + ".vertexPoints"
        private val TRI_STRIP_ELEMENTS_KEY = CartesianLayer::class.java.name + ".triStripElements"

        private val VERTEX_POINTS_F_KEY = CartesianLayer::class.java.name + ".vertexPoints.f"
        private val TRI_STRIP_ELEMENTS_F_KEY = CartesianLayer::class.java.name + ".triStripElements.f"
    }

    init {
        this.pickEnabled = false
    }

    protected var color: SimpleColor = SimpleColor(Color.RED)

    override fun doRender(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return
        }
        var program: CartesianProgram? = rc.getProgram(
            CartesianProgram.KEY
        ) as CartesianProgram?
        if (program == null) {
            program = rc.putProgram(
                CartesianProgram.KEY,
                CartesianProgram(rc.resources)
            ) as CartesianProgram
        }

        val pool: Pool<DrawableCartesian> = rc.getDrawablePool(
            DrawableCartesian::class.java)
        var drawable = DrawableCartesian.obtain(pool).set(program, color, true)
        // 笛卡尔坐标系
        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_KEY)
        if (drawable.vertexPoints == null) {
            drawable.vertexPoints =
                rc.putBufferObject(
                    VERTEX_POINTS_KEY,
                    this.assembleVertexPoints(rc)
                )
        }

        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_KEY)
        if (drawable.triStripElements == null) {
            drawable.triStripElements =
                rc.putBufferObject(
                    TRI_STRIP_ELEMENTS_KEY,
                    this.assembleTriStripElements()
                )
        }
        rc.offerDrawable(drawable, WorldWind.SCREEN_DRAWABLE, 10.0)
        // 观察空间
//        drawable = DrawableCartesian.obtain(pool).set(program, color, true)
//        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_F_KEY)
//        if (drawable.vertexPoints == null) {
//            drawable.vertexPoints =
//                rc.putBufferObject(
//                    VERTEX_POINTS_F_KEY,
//                    this.assembleVertexPoints2(rc)
//                )
//        }
//
//        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_F_KEY)
//        if (drawable.triStripElements == null) {
//            drawable.triStripElements =
//                rc.putBufferObject(
//                    TRI_STRIP_ELEMENTS_F_KEY,
//                    this.assembleTriStripElements2()
//                )
//        }
//
//        rc.offerDrawable(drawable, WorldWind.SCREEN_DRAWABLE, 11.0)
    }

    private fun assembleVertexPoints(rc: RenderContext)
            : BufferObject {
        val cameraPoint: Vec3 = rc.cameraPoint
        val magnitude = cameraPoint.magnitude()
        val points = FloatArray(5 * 4)
        points[0] = 0f
        points[1] = 0f
        points[2] = 0f
        points[3] = 1f

        points[4] = magnitude.toFloat()
        points[5] = 0f
        points[6] = 0f
        points[7] = 1f

        points[8] = 0f
        points[9] = magnitude.toFloat()
        points[10] = 0f
        points[11] = 1f

        points[12] = 0f
        points[13] = 0f
        points[14] = magnitude.toFloat()
        points[15] = 1f

        points[16] = cameraPoint.x.toFloat()
        points[17] = cameraPoint.y.toFloat()
        points[18] = cameraPoint.z.toFloat()
        points[19] = 1f

        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        return BufferObject(
            GLES20.GL_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleTriStripElements()
            : BufferObject {
        val elements = ShortArray(8)
        elements[0] = 0
        elements[1] = 1
        elements[2] = 0
        elements[3] = 2
        elements[4] = 0
        elements[5] = 3
        elements[6] = 0
        elements[7] = 4
        val size = elements.size * 2
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
        buffer.put(elements).rewind()
        return BufferObject(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleVertexPoints2(rc: RenderContext): BufferObject {
        val frustum = rc.frustum
        val left = frustum.left
        val right = frustum.right
        val bottom = frustum.bottom
        val top = frustum.top
        val near = frustum.near
        val far = frustum.far

        var leftPoint = Vec3(left.normal).also { it.multiply(-left.distance) }
        var rightPoint = Vec3(right.normal).also { it.multiply(-right.distance) }
        var topPoint = Vec3(top.normal).also { it.multiply(-top.distance) }
        var bottomPoint = Vec3(bottom.normal).also { it.multiply(-bottom.distance) }
        var nearPoint = Vec3(near.normal).also { it.multiply(-near.distance) }
        var farPoint = Vec3(far.normal).also { it.multiply(-far.distance) }


        val points = FloatArray(9 * 4)
        // origin
        points[0] = 0f
        points[1] = 0f
        points[2] = 0f
        points[3] = 1f
        // 1
        val temp: Vec3 = Vec3()
        temp.add(leftPoint).add(topPoint).add(nearPoint)
        points[4] = (temp.x).toFloat()
        points[5] = (temp.y).toFloat()
        points[6] = (temp.z).toFloat()
        points[7] = 1f
        //bottom 2
        temp.reset()
        temp.add(rightPoint).add(topPoint).add(nearPoint)
        points[8] = (temp.x).toFloat()
        points[9] = (temp.y).toFloat()
        points[10] = (temp.z).toFloat()
        points[11] = 1f
        // top 3
        temp.reset()
        temp.add(leftPoint).add(bottomPoint).add(nearPoint)
        points[12] = (temp.x).toFloat()
        points[13] = (temp.y).toFloat()
        points[14] = (temp.z).toFloat()
        points[15] = 1f
        // near 4
        temp.reset()
        temp.add(rightPoint).add(bottomPoint).add(nearPoint)
        points[16] = (temp.x).toFloat()
        points[17] = (temp.y).toFloat()
        points[18] = (temp.z).toFloat()
        points[19] = 1f
        // far 5
        temp.reset()
        temp.add(leftPoint).add(topPoint).add(farPoint)
        points[20] = (temp.x).toFloat()
        points[21] = (temp.y).toFloat()
        points[22] = (temp.z).toFloat()
        points[23] = 1f
        // far 6
        temp.reset()
        temp.add(rightPoint).add(topPoint).add(farPoint)
        points[24] = (temp.x).toFloat()
        points[25] = (temp.y).toFloat()
        points[26] = (temp.z).toFloat()
        points[27] = 1f
        // far 5
        temp.reset()
        temp.add(leftPoint).add(bottomPoint).add(farPoint)
        points[28] = (temp.x).toFloat()
        points[29] = (temp.y).toFloat()
        points[30] = (temp.z).toFloat()
        points[31] = 1f
        // far 5
        temp.reset()
        temp.add(rightPoint).add(bottomPoint).add(farPoint)
        points[32] = (temp.x).toFloat()
        points[33] = (temp.y).toFloat()
        points[34] = (temp.z).toFloat()
        points[35] = 1f

        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        return BufferObject(
            GLES20.GL_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleTriStripElements2(): BufferObject {
        val elements = ShortArray(16)
        // xz 面
        elements[0] = 0
        elements[1] = 1

        elements[2] = 0
        elements[3] = 2

        elements[4] = 0
        elements[5] = 3

        elements[6] = 0
        elements[7] = 4

        // xy 面
        elements[8] = 0
        elements[9] = 5

        elements[10] = 0
        elements[11] = 6

        elements[12] = 0
        elements[13] = 7

        elements[14] = 0
        elements[15] = 8
//        // yz
//        elements[16] = 4
//        elements[17] = 2
//
//        elements[18] = 2
//        elements[19] = 5
//
//        elements[20] = 5
//        elements[21] = 3
//
//        elements[22] = 3
//        elements[23] = 4

        val size = elements.size * 2
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
        buffer.put(elements).rewind()
        return BufferObject(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleVertexPoints3(rc: RenderContext): BufferObject {
        val frustum = rc.frustum
        val left = frustum.left
        val right = frustum.right
        val bottom = frustum.bottom
        val top = frustum.top
        val near = frustum.near
        val far = frustum.far
        var leftPoint = Vec3(left.normal).also { it.multiply(-left.distance) }
        var rightPoint = Vec3(right.normal).also { it.multiply(-right.distance) }
        var topPoint = Vec3(top.normal).also { it.multiply(-top.distance) }
        var bottomPoint = Vec3(bottom.normal).also { it.multiply(-bottom.distance) }
        var nearPoint = Vec3(near.normal).also { it.multiply(-near.distance) }
        var farPoint = Vec3(far.normal).also { it.multiply(-far.distance) }
        val origin = arrayOf(leftPoint, rightPoint, topPoint, bottomPoint, nearPoint, farPoint)
        val vec3List = mutableListOf<Vec3>()
        val ints = intArrayOf(
            0, 4, 2,
            4, 2, 1,
            0, 4, 3,
            3, 4, 1,
            0, 5, 2,
            1, 2, 5,
            0, 3, 5,
            3, 1, 5
        )
        var idx = 0
        while (idx < ints.size) {
            val temp = Vec3(origin[ints[idx++]])
            temp.add(origin[ints[idx++]])
            temp.add(origin[ints[idx++]])
            vec3List.add(temp)
        }
        val points = FloatArray(8 * 4)
        idx = 0
        while (idx < 32) {
            val id = idx % 4
            val get = vec3List.get(id)
            points[idx++] = get.x.toFloat()
            points[idx++] = get.y.toFloat()
            points[idx++] = get.z.toFloat()
            points[idx++] = 1f
        }
        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        return BufferObject(
            GLES20.GL_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleTriStripElements3()
            : BufferObject {
        //        val elements = shortArrayOf(
        //            0,2,2,6,6,4,4,0,
        //            1,3,3,7,7,5,5,1,
        //            0,1,2,3,6,7,4,5
        //        )
        val elements = shortArrayOf(
            0, 1, 0, 2, 0, 3, 0, 4,
            0, 5, 0, 0, 7
        )

        val size = elements.size * 2
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
        buffer.put(elements).rewind()
        return BufferObject(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            size,
            buffer
        )
    }


}