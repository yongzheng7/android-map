package com.atom.wyz.worldwind.layer

import android.graphics.Color
import android.opengl.GLES20
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.context.RenderContext
import com.atom.wyz.worldwind.draw.DrawableCartesian
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.geom.SimpleColor
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
        var drawable = DrawableCartesian.obtain(pool).set(program, color)
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

        drawable = DrawableCartesian.obtain(pool).set(program, color)
        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_F_KEY)
        if (drawable.vertexPoints == null) {
            drawable.vertexPoints =
                rc.putBufferObject(
                    VERTEX_POINTS_KEY,
                    this.assembleVertexPoints2(rc)
                )
        }
        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_F_KEY)
        if (drawable.triStripElements == null) {
            drawable.triStripElements =
                rc.putBufferObject(
                    TRI_STRIP_ELEMENTS_KEY,
                    this.assembleTriStripElements2()
                )
        }
        rc.offerDrawable(drawable, WorldWind.SCREEN_DRAWABLE, 10.0)
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


    private fun assembleVertexPoints2(rc: RenderContext)
            : BufferObject {
        val frustum = rc.frustum
        val left = frustum.left
        val right = frustum.right
        val bottom = frustum.bottom
        val top = frustum.top
        val near = frustum.near
        val far = frustum.far

        val points = FloatArray(6 * 4)
        // left
        points[0] = (left.normal.x * left.distance).toFloat()
        points[1] = (left.normal.y * left.distance).toFloat()
        points[2] = (left.normal.z * left.distance).toFloat()
        points[3] = 1f
        //right
        points[4] = (right.normal.x * right.distance).toFloat()
        points[5] = (right.normal.y * right.distance).toFloat()
        points[6] = (right.normal.z * right.distance).toFloat()
        points[7] = 1f
        //bottom
        points[8] = (bottom.normal.x * bottom.distance).toFloat()
        points[9] = (bottom.normal.y * bottom.distance).toFloat()
        points[10] = (bottom.normal.z * bottom.distance).toFloat()
        points[11] = 1f
        // top
        points[12] = (top.normal.x * top.distance).toFloat()
        points[13] = (top.normal.y * top.distance).toFloat()
        points[14] = (top.normal.z * top.distance).toFloat()
        points[15] = 1f
        // near
        points[16] = (near.normal.x * near.distance).toFloat()
        points[17] = (near.normal.y * near.distance).toFloat()
        points[18] = (near.normal.z * near.distance).toFloat()
        points[19] = 1f
        // far
        points[20] = (far.normal.x * far.distance).toFloat()
        points[21] = (far.normal.y * far.distance).toFloat()
        points[22] = (far.normal.z * far.distance).toFloat()
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
        val elements = ShortArray(18)
        elements[0] = 0
        elements[1] = 4
        elements[2] = 1

        elements[3] = 0
        elements[4] = 1
        elements[5] = 5

        elements[6] = 3
        elements[7] = 4
        elements[8] = 2

        elements[9] = 3
        elements[10] = 2
        elements[11] = 5

        elements[12] = 0
        elements[13] = 2
        elements[14] = 5

        elements[15] = 0
        elements[16] = 5
        elements[17] = 3
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