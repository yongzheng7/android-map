package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawShapeState
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableShape
import com.atom.wyz.worldwind.draw.DrawableSurfaceShape
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Path : AbstractShape {
    companion object {
        private fun nextCacheKey() = Any()
    }

    var positions: MutableList<Position> = mutableListOf()
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

     var followTerrain = false
        get() = field
        set(value) {
            field = value
            reset()
        }

    protected var vertexArray: SimpleFloatArray = SimpleFloatArray()

    protected var interiorElements: SimpleShortArray = SimpleShortArray()

    protected var outlineElements: SimpleShortArray = SimpleShortArray()

    protected var verticalElements: SimpleShortArray = SimpleShortArray()

    protected var vertexBufferKey: Any = nextCacheKey()

    protected var elementBufferKey: Any = nextCacheKey()

    protected var vertexOrigin: Vec3 = Vec3()

    protected var isSurfaceShape = false

    private val loc: Location = Location()

    private val point: Vec3 = Vec3()

    constructor()

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(positions: MutableList<Position>) {
        this.positions = positions
    }

    constructor(positions: MutableList<Position>, attributes: ShapeAttributes) : super(attributes) {
        this.positions = positions
    }

    override fun reset() {
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
        verticalElements.clear()
    }

    override fun makeDrawable(rc: RenderContext) {
        if (positions.isEmpty()) {
            return  // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            vertexBufferKey = nextCacheKey()
            elementBufferKey = nextCacheKey()
        }
        // Obtain a drawable form the render context pool.
        // Obtain a drawable form the render context pool.
        val drawable: Drawable
        val drawState: DrawShapeState
        if (isSurfaceShape) {
            val pool: Pool<DrawableSurfaceShape> = rc.getDrawablePool(DrawableSurfaceShape::class.java)
            drawable = DrawableSurfaceShape.obtain(pool)
            drawState = drawable.drawState
            drawable.sector.set(boundingSector)
        } else {
            val pool: Pool<DrawableShape> = rc.getDrawablePool(DrawableShape::class.java)
            drawable = DrawableShape.obtain(pool)
            drawState = (drawable as DrawableShape).drawState
        }
        // Use the basic GLSL program to draw the shape.
        drawState.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawState.program == null) {
            drawState.program = rc.putProgram(BasicProgram.KEY, BasicProgram(rc.resources!!)) as BasicProgram
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer =
                rc.putBufferObject(vertexBufferKey, BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind()))
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size = interiorElements.size() * 2 + outlineElements.size() * 2 + verticalElements.size() * 2
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            buffer.put(verticalElements.array(), 0, verticalElements.size())
            val bufferObject = BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind())
            drawState.elementBuffer = rc.putBufferObject(elementBufferKey, bufferObject)
        }

        // Configure the drawable to display the path's outline.
        if (activeAttributes!!.drawOutline) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.drawElements(
                GLES20.GL_LINE_STRIP, outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2
            )
        }

        // Configure the drawable to display the path's extruded verticals.
        if (activeAttributes!!.drawOutline && activeAttributes!!.drawVerticals && extrude) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.drawElements(
                GLES20.GL_LINES, verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 + outlineElements.size() * 2
            )
        }

        // Configure the drawable to display the path's extruded interior.
        if (activeAttributes!!.drawInterior && extrude) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor)
            drawState.drawElements(
                GLES20.GL_TRIANGLE_STRIP, interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0
            )
        }

        // Configure the drawable according to the shape's attributes.
        drawState.vertexOrigin.set(vertexOrigin)
        drawState.enableCullFace = false
        drawState.enableDepthTest = activeAttributes!!.depthTest

        // Enqueue the drawable for processing on the OpenGL thread.
        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            val cameraDistance = boundingBox.distanceTo(rc.cameraPoint)
            rc.offerShapeDrawable(drawable, cameraDistance)
        }
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        return vertexArray.size() == 0
    }

    protected fun assembleGeometry(rc: RenderContext) {// Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        isSurfaceShape = altitudeMode == WorldWind.CLAMP_TO_GROUND && followTerrain


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
            this.addIntermediateVertices(rc, begin, end)
            addVertex(rc, end.latitude, end.longitude, end.altitude, false /*tessellated*/)
            begin = end
            idx++
        }
        // Compute the path's bounding box or bounding sector from its assembled coordinates.
        if (isSurfaceShape) {
            boundingSector.setEmpty()
            boundingSector.union(vertexArray.array(), vertexArray.size(), 2)
            boundingBox.setToUnitBox() // Surface/geographic shape bounding box is unused
        } else {
            boundingBox.setToPoints(vertexArray.array(), vertexArray.size(), 3)
            boundingBox.translate(vertexOrigin.x, vertexOrigin.y, vertexOrigin.z)
            boundingSector.setEmpty() // Cartesian shape bounding sector is unused
        }
    }


    protected fun addIntermediateVertices(rc: RenderContext, begin: Position, end: Position) {
        if (pathType == WorldWind.LINEAR) {
            return  // suppress edge vertices when the path type is linear
        }
        if (maximumIntermediatePoints <= 0) {
            return  // suppress intermediate vertices when configured to do so
        }
        var azimuth = 0.0
        var length = 0.0
        if (pathType == WorldWind.GREAT_CIRCLE) {
            azimuth = begin.greatCircleAzimuth(end)
            length = begin.greatCircleDistance(end)
        } else if (pathType == WorldWind.RHUMB_LINE) {
            azimuth = begin.rhumbAzimuth(end)
            length = begin.rhumbDistance(end)
        }
        if (length < NEAR_ZERO_THRESHOLD) {
            return  // suppress the next point when the segment length less than a millimeter (on Earth)
        }

        val numSubsegments = maximumIntermediatePoints + 1
        val deltaDist = length / numSubsegments
        val deltaAlt = (end.altitude - begin.altitude) / numSubsegments
        var dist = deltaDist
        var alt = begin.altitude + deltaAlt

        for (idx in 1 until numSubsegments) {
            if (pathType == WorldWind.GREAT_CIRCLE) {
                begin.greatCircleLocation(azimuth, dist, loc)
            } else if (pathType == WorldWind.RHUMB_LINE) {
                begin.rhumbLocation(azimuth, dist, loc)
            }
            addVertex(rc, loc.latitude, loc.longitude, alt, true /*tessellated*/)
            dist += deltaDist
            alt += deltaAlt
        }
    }

    protected fun addVertex(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        tessellated: Boolean
    ) {
        if (isSurfaceShape) {
            addVertexGeographic(rc, latitude, longitude, altitude, tessellated)
        } else {
            addVertexCartesian(rc, latitude, longitude, altitude, tessellated)
        }
    }

    protected fun addVertexGeographic(
        rc: RenderContext?,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        tessellated: Boolean
    ) {
        val vertex = vertexArray.size() / 2
        vertexArray.add(longitude.toFloat())
        vertexArray.add(latitude.toFloat())
        outlineElements.add(vertex.toShort())
    }

    protected fun addVertexCartesian(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        tessellated: Boolean
    ) {
        rc.geographicToCartesian(latitude, longitude, altitude, altitudeMode, point)
        val vertex = vertexArray.size() / 3
        vertexArray.add((point.x - vertexOrigin.x).toFloat())
        vertexArray.add((point.y - vertexOrigin.y).toFloat())
        vertexArray.add((point.z - vertexOrigin.z).toFloat())
        outlineElements.add(vertex.toShort())
        if (extrude) {
            rc.geographicToCartesian(latitude, longitude, 0.0, WorldWind.CLAMP_TO_GROUND, point)
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())
            interiorElements.add(vertex.toShort())
            interiorElements.add((vertex + 1).toShort())
            if (!tessellated) {
                verticalElements.add(vertex.toShort())
                verticalElements.add((vertex + 1).toShort())
            }
        }
    }
}