package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawableElements
import com.atom.wyz.worldwind.draw.DrawableShape
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Path : AbstractShape {
    companion object {
        protected const val NEAR_ZERO_THRESHOLD = 1.0e-10
        private var cacheKeySequence: Long = 0
        private fun nextCacheKey(): String {
            return Path::class.java.getName() + ".cacheKey." + java.lang.Long.toString(++cacheKeySequence)
        }
    }

    var positions: MutableList<Position>
        get() = field
        set(value) {
            field = value
            reset()
        }

    var extrude = false
        get() = field
        set(value) {
            field = value
            reset()
        }

    protected var vertexArray: SimpleFloatArray = SimpleFloatArray()

    protected var interiorElements: SimpleShortArray = SimpleShortArray()

    protected var outlineElements: SimpleShortArray = SimpleShortArray()

    protected var verticalElements: SimpleShortArray = SimpleShortArray()

    protected var vertexBufferKey: String = nextCacheKey()

    protected var elementBufferKey: String = nextCacheKey()

    protected var vertexOrigin: Vec3 = Vec3()

    private val numSubsegments = 10

    private val loc: Location = Location()

    private val point: Vec3 = Vec3()

    constructor(positions: MutableList<Position>) {
        this.positions = positions
    }

    constructor(positions: MutableList<Position>, attributes: ShapeAttributes) : super(attributes) {
        this.positions = positions
    }

    override fun reset() {
        vertexArray.clear()
    }

    override fun makeDrawable(rc: RenderContext) {
        if (positions.isEmpty()) {
            return  // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            //rc.renderResourceCache.remove(this.vertexBufferKey);
            vertexBufferKey = nextCacheKey()
            elementBufferKey = nextCacheKey()
        }
        // Obtain a drawable form the render context pool.
        val drawable: DrawableShape = DrawableShape.obtain(rc.getDrawablePool(DrawableShape::class.java))
        // Use the basic GLSL program to draw the shape.
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(BasicProgram.KEY, BasicProgram(rc.resources!!)) as BasicProgram
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawable.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawable.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawable.vertexBuffer =
                rc.putBufferObject(vertexBufferKey, BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind()))
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawable.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawable.elementBuffer == null) {
            val size = interiorElements.size() * 2 + outlineElements.size() * 2 + verticalElements.size() * 2
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            buffer.put(verticalElements.array(), 0, verticalElements.size())
            val bufferObject = BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind())
            drawable.elementBuffer = rc.putBufferObject(elementBufferKey, bufferObject)
        }

        // 配置可绘制对象以显示路径的轮廓。
        if (activeAttributes!!.drawOutline) {
            val prim: DrawableElements = drawable.addDrawElements(
                GLES20.GL_LINE_STRIP, outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2
            )
            prim.color.set(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor!!)
            prim.lineWidth = activeAttributes!!.outlineWidth
        }
        // 配置可绘制对象以显示路径的拉伸垂直线。
        if (activeAttributes!!.drawOutline && activeAttributes!!.drawVerticals && extrude) {
            val prim: DrawableElements = drawable.addDrawElements(
                GLES20.GL_LINES, verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 + outlineElements.size() * 2
            )
            prim.color.set(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor!!)
            prim.lineWidth = activeAttributes!!.outlineWidth
        }
        // 配置可绘制对象以显示路径的拉伸内部。
        if (activeAttributes!!.drawInterior && extrude) {
            val prim: DrawableElements = drawable.addDrawElements(
                GLES20.GL_TRIANGLE_STRIP, interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0
            )
            prim.color.set(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor!!)
        }
        // 根据形状的属性配置可绘制对象。
        drawable.vertexOrigin.set(vertexOrigin)
        drawable.enableDepthTest = activeAttributes!!.depthTest
        // 使drawable排队以便在OpenGL线程上进行处理。
        val cameraDistance = boundingBox.distanceTo(rc.cameraPoint)
        rc.offerShapeDrawable(drawable, cameraDistance)
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        return vertexArray.size() == 0
    }

    protected fun assembleGeometry(rc: RenderContext) {
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
        verticalElements.clear()
        // Compute the path's local Cartesian coordinate origin and add the first vertex.
        // 取出点1
        var begin = positions[0]
        // 进行转换为笛卡尔3维坐标系
        rc.geographicToCartesian(begin.latitude, begin.longitude, begin.altitude, altitudeMode, vertexOrigin)
        // 转换好 添加
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, false /*tessellated*/)
        // Add the remaining path vertices, tessellating each segment as indicated by the path's properties.
        var idx = 1
        val len = positions.size
        while (idx < len) {
            val end = positions[idx]
            this.addSegment(rc, begin, end)
            begin = end
            idx++
        }
        // Compute the path's Cartesian bounding box from its Cartesian coordinates.
        boundingBox.setToPoints(vertexArray.array(), vertexArray.size(), 3)
        boundingBox.translate(vertexOrigin.x, vertexOrigin.y, vertexOrigin.z)
    }


    protected fun addSegment(rc: RenderContext, begin: Position, end: Position) {
        var azimuth = 0.0
        var length = 0.0
        when (pathType) {
            WorldWind.GREAT_CIRCLE -> {
                azimuth = begin.greatCircleAzimuth(end)
                length = begin.greatCircleDistance(end)
            }
            WorldWind.LINEAR -> {
                azimuth = begin.linearAzimuth(end)
                length = begin.linearDistance(end)
            }
            WorldWind.RHUMB_LINE -> {
                azimuth = begin.rhumbAzimuth(end)
                length = begin.rhumbDistance(end)
            }
        }
        if (length < NEAR_ZERO_THRESHOLD) {
            return  // suppress the next point when the segment length less than a millimeter (on Earth)
        }
        if (numSubsegments > 0) {
            val deltaDist = length / numSubsegments
            val deltaAlt = (end.altitude - begin.altitude) / numSubsegments
            var dist = deltaDist
            var alt = begin.altitude + deltaAlt
            for (idx in 1 until numSubsegments) {
                when (pathType) {
                    WorldWind.GREAT_CIRCLE -> begin.greatCircleLocation(azimuth, dist, loc)
                    WorldWind.LINEAR -> begin.linearLocation(azimuth, dist, loc)
                    WorldWind.RHUMB_LINE -> begin.rhumbLocation(azimuth, dist, loc)
                }
                addVertex(rc, loc.latitude, loc.longitude, alt, true /*tessellated*/)
                dist += deltaDist
                alt += deltaAlt
            }
        }
        // Explicitly add the endpoint to ensure alignment.
        addVertex(rc, end.latitude, end.longitude, end.altitude, false /*tessellated*/)
    }

    protected fun addVertex(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        tessellated: Boolean
    ) {
        rc.geographicToCartesian(latitude, longitude, altitude, altitudeMode, point)
        val topVertex = vertexArray.size() / 3
        vertexArray.add((point.x - vertexOrigin.x).toFloat())
        vertexArray.add((point.y - vertexOrigin.y).toFloat())
        vertexArray.add((point.z - vertexOrigin.z).toFloat())
        outlineElements.add(topVertex.toShort())
        if (extrude) {
            rc.geographicToCartesian(latitude, longitude, 0.0, WorldWind.CLAMP_TO_GROUND, point)
            val bottomVertex = vertexArray.size() / 3
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())
            interiorElements.add(topVertex.toShort())
            interiorElements.add(bottomVertex.toShort())
            if (!tessellated) {
                verticalElements.add(topVertex.toShort())
                verticalElements.add(bottomVertex.toShort())
            }
        }
    }
}