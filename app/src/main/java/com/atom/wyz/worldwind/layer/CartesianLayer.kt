package com.atom.wyz.worldwind.layer

import android.graphics.Color
import android.opengl.GLES20
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.context.RenderContext
import com.atom.wyz.worldwind.draw.DrawableCartesian
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.CartesianProgram
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CartesianLayer : AbstractLayer("CartesianLayer") {

    companion object {
        private val VERTEX_POINTS_KEY = CartesianLayer::class.java.name + ".vertexPoints"
        private val TRI_STRIP_ELEMENTS_KEY = CartesianLayer::class.java.name + ".triStripElements"

        private val VERTEX_POINTS_F_KEY = CartesianLayer::class.java.name + ".vertexPoints.f"
        private val TRI_STRIP_ELEMENTS_F_KEY =
            CartesianLayer::class.java.name + ".triStripElements.f"
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

        val pool: Pool<DrawableCartesian> = rc.getDrawablePool(DrawableCartesian::class.java)
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
        drawable = DrawableCartesian.obtain(pool).set(program, color, true)
        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_F_KEY)
        if (drawable.vertexPoints == null) {
            drawable.vertexPoints =
                rc.putBufferObject(
                    VERTEX_POINTS_F_KEY,
                    this.assembleVertexPoints2(rc)
                )
        }
        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_F_KEY)
        if (drawable.triStripElements == null) {
            drawable.triStripElements =
                rc.putBufferObject(
                    TRI_STRIP_ELEMENTS_F_KEY,
                    this.assembleTriStripElements2()
                )
        }
        rc.offerDrawable(drawable, WorldWind.SCREEN_DRAWABLE, 11.0)
    }

    private fun assembleVertexPoints(rc: RenderContext)
            : BufferObject {
        val cameraPoint: Vec3 = rc.cameraPoint
        val magnitude = cameraPoint.magnitude()
        val points = FloatArray(4 * 4)
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
        val elements = ShortArray(6)
        elements[0] = 0
        elements[1] = 1
        elements[2] = 0
        elements[3] = 2
        elements[4] = 0
        elements[5] = 3
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


        val points = FloatArray(6 * 4)
        // left 0
        points[0] = (leftPoint.x).toFloat()
        points[1] = (leftPoint.y).toFloat()
        points[2] = (leftPoint.z).toFloat()
        points[3] = 1f
        //right 1
        points[4] = (rightPoint.x).toFloat()
        points[5] = (rightPoint.y).toFloat()
        points[6] = (rightPoint.z).toFloat()
        points[7] = 1f
        //bottom 2
        points[8] = (bottomPoint.x).toFloat()
        points[9] = (bottomPoint.y).toFloat()
        points[10] = (bottomPoint.z).toFloat()
        points[11] = 1f
        // top 3
        points[12] = (topPoint.x).toFloat()
        points[13] = (topPoint.y).toFloat()
        points[14] = (topPoint.z).toFloat()
        points[15] = 1f
        // near 4
        points[16] = (nearPoint.x).toFloat()
        points[17] = (nearPoint.y).toFloat()
        points[18] = (nearPoint.z).toFloat()
        points[19] = 1f
        // far 5
        points[20] = (farPoint.x).toFloat()
        points[21] = (farPoint.y).toFloat()
        points[22] = (farPoint.z).toFloat()
        points[23] = 1f

        val size = points.size * 4
        val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(points).rewind()
        return BufferObject(
            GLES20.GL_ARRAY_BUFFER,
            size,
            buffer
        )
    }

    private fun assembleTriStripElements2()
            : BufferObject {
        val elements = ShortArray(24)
        // xz 面
        elements[0] = 0
        elements[1] = 4

        elements[2] = 4
        elements[3] = 1

        elements[4] = 1
        elements[5] = 5

        elements[6] = 5
        elements[7] = 0

        // xy 面
        elements[8] = 0
        elements[9] = 2

        elements[10] = 2
        elements[11] = 1

        elements[12] = 1
        elements[13] = 3

        elements[14] = 3
        elements[15] = 0
        // yz
        elements[16] = 4
        elements[17] = 2

        elements[18] = 2
        elements[19] = 5

        elements[20] = 5
        elements[21] = 3

        elements[22] = 3
        elements[23] = 4

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