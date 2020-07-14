package com.atom.wyz.worldwind.layer

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.DrawableCartesian
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CartesianLayer : AbstractLayer("CartesianLayer") {

    companion object {
        private val VERTEX_POINTS_KEY = CartesianLayer::class.java.name + ".vertexPoints"
        private val TRI_STRIP_ELEMENTS_KEY = CartesianLayer::class.java.name + ".triStripElements"
    }

    init {
        this.pickEnabled = false
    }

    protected var color: Color = Color.RED

    override fun doRender(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return
        }
        var program: BasicProgram? = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (program == null) {
            val resources = rc.resources ?: return
            program = rc.putProgram(BasicProgram.KEY, BasicProgram(resources)) as BasicProgram
        }

        val pool: Pool<DrawableCartesian> = rc.getDrawablePool(DrawableCartesian::class.java)
        val drawable = DrawableCartesian.obtain(pool).set(program, color)
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
        rc.offerSurfaceDrawable(drawable, 1.0)
    }

    private fun assembleVertexPoints(
        rc: RenderContext
    ): BufferObject {
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
        return BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer)
    }

    private fun assembleTriStripElements(): BufferObject {
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
        return BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer)
    }

}