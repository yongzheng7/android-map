package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.DrawShapeState
import com.atom.wyz.worldwind.draw.DrawableSurfaceShape
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Ellipse : AbstractShape {
    companion object {
        const val VERTEX_STRIDE = 6
        const val MIN_INTERVALS = 32
        val POSITION = Position()
        val POINT: Vec3 = Vec3()
        protected fun nextCacheKey(): Any {
            return Any()
        }

    }

    var center: Position? = null

    var majorRadius = 0.0
        set(value) {
            field = value
            reset()
        }
    var minorRadius = 0.0
        set(value) {
            field = value
            reset()
        }
    var heading = 0.0
        set(value) {
            field = value
            reset()
        }
    var maximumIntervals = 256
        set(value) {
            field = value
            reset()
        }
    var maximumPixelsPerInterval = 50.0


    var intervals = 0

    var followTerrain = false
        set(value) {
            field = value
            reset()
        }
    var vertexArray = SimpleFloatArray()

    var interiorElements = SimpleShortArray()

    var outlineElements = SimpleShortArray()

    var vertexBufferKey = nextCacheKey()

    var elementBufferKey = nextCacheKey()

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(
        center: Position,
        majorRadius: Double,
        minorRadius: Double
    ) {
        if (majorRadius < 0 || minorRadius < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius")
            )
        }
        this.center = Position(center)
        this.majorRadius = majorRadius
        this.minorRadius = minorRadius
    }

    constructor(
        center: Position,
        majorRadius: Double,
        minorRadius: Double,
        attributes: ShapeAttributes
    ) : super(attributes) {

        if (majorRadius < 0 || minorRadius < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius")
            )
        }
        this.center = Position(center)
        this.majorRadius = majorRadius
        this.minorRadius = minorRadius
    }

    fun setCenter(position: Position?): Ellipse {
        if (position == null) {
            center = null
        } else if (center == null) {
            center = Position(position)
        } else {
            center!!.set(position)
        }
        reset()
        return this
    }

    override fun makeDrawable(rc: RenderContext) {
        if (center == null) {
            return  // nothing to draw
        }

        if (majorRadius == 0.0 && minorRadius == 0.0) {
            return  // nothing to draw
        }
        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            this.assembleElements(rc)
            vertexBufferKey = nextCacheKey()
            elementBufferKey = nextCacheKey()
        }

        // Obtain a drawable form the render context pool.

        // Obtain a drawable form the render context pool.
        val drawable: DrawableSurfaceShape
        val drawState: DrawShapeState
        val pool: Pool<DrawableSurfaceShape> = rc.getDrawablePool(DrawableSurfaceShape::class.java)
        drawable = DrawableSurfaceShape.obtain(pool)
        drawState = drawable.drawState
        drawable.sector.set(boundingSector)


        // Use the basic GLSL program to draw the shape.


        // Use the basic GLSL program to draw the shape.
        drawState.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawState.program == null) {
            drawState.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources!!)
            ) as BasicProgram?
        }

        // Assemble the drawable's OpenGL vertex buffer object.

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer = BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(vertexBufferKey, drawState.vertexBuffer!!)
        }

        // Assemble the drawable's OpenGL element buffer object.

        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size = interiorElements.size() * 2 + outlineElements.size() * 2
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                    .asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            drawState.elementBuffer =
                BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(elementBufferKey, drawState.elementBuffer!!)
        }

        this.drawInterior(rc, drawState)
        this.drawOutline(rc, drawState)

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexStride = VERTEX_STRIDE * 4 // stride in bytes

        drawState.enableCullFace = false
        drawState.enableDepthTest = activeAttributes!!.depthTest

        // Enqueue the drawable for processing on the OpenGL thread.

        // Enqueue the drawable for processing on the OpenGL thread.
        rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
    }

    protected fun drawInterior(rc: RenderContext, drawState: DrawShapeState) {
        if (!activeAttributes!!.drawInterior) {
            return
        }
        drawState.texture = (null)
        // Configure the drawable to display the shape's interior.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor)
        drawState.texCoordAttrib.size = 2
        drawState.texCoordAttrib.offset = 12
        drawState.drawElements(
            GLES20.GL_TRIANGLE_STRIP, interiorElements.size(),
            GLES20.GL_UNSIGNED_SHORT, 0 /*offset*/
        )
    }

    protected fun drawOutline(rc: RenderContext, drawState: DrawShapeState) {
        if (!activeAttributes!!.drawOutline) {
            return
        }
        drawState.texture = (null)

        // Configure the drawable to display the shape's outline.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
        drawState.lineWidth(activeAttributes!!.outlineWidth)
        drawState.texCoordAttrib.size = 1
        drawState.texCoordAttrib.offset = 20
        drawState.drawElements(
            GLES20.GL_LINE_LOOP, outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 /*offset*/
        )
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        val calculatedIntervals: Int = this.computeIntervals(rc)
        val sanitzedIntervals: Int = this.sanitizeIntervals(calculatedIntervals)
        if (vertexArray.size() == 0 || sanitzedIntervals != intervals) {
            intervals = sanitzedIntervals
            return true
        }
        return false
    }

    protected fun assembleGeometry(rc: RenderContext) {

        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
        // geometry is assembled.
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()

        // Determine the number of spine points and construct radius value holding array
        val spinePoints = intervals / 2 - 1 // intervals must be even
        var spineIdx = 0
        val spineRadius = DoubleArray(spinePoints)

        // Check if minor radius is less than major in which case we need to flip the definitions and change the phase
        val isStandardAxisOrientation = majorRadius > minorRadius
        val headingAdjustment: Double = if (isStandardAxisOrientation) 90.0 else 0.0

        // Vertex generation begins on the positive major axis and works ccs around the ellipse. The spine points are
        // then appended from positive major axis to negative major axis.
        val deltaRadians = 2 * Math.PI / intervals
        val majorArcRadians: Double
        val minorArcRadians: Double
        if (isStandardAxisOrientation) {
            majorArcRadians =
                majorRadius / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)
            minorArcRadians =
                minorRadius / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)
        } else {
            majorArcRadians =
                minorRadius / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)
            minorArcRadians =
                majorRadius / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)
        }
        for (i in 0 until intervals) {
            val radians = deltaRadians * i
            val x = Math.cos(radians) * majorArcRadians
            val y = Math.sin(radians) * minorArcRadians
            val azimuthDegrees =
                Math.toDegrees(-Math.atan2(y, x))
            val arcRadius = Math.sqrt(x * x + y * y)
            // Calculate the great circle location given this intervals step (azimuthDegrees) a correction value to
            // start from an east-west aligned major axis (90.0) and the user specified user heading value
            center!!.greatCircleLocation(
                azimuthDegrees + headingAdjustment + heading,
                arcRadius,
                POSITION
            )
            addVertex(rc, POSITION.latitude, POSITION.longitude, 0.0)
            // Add the major arc radius for the spine points. Spine points are vertically coincident with exterior
            // points. The first and middle most point do not have corresponding spine points.
            if (i > 0 && i < intervals / 2) {
                spineRadius[spineIdx++] = x
            }
        }

        // Add the interior spine point vertices
        for (i in 0 until spinePoints) {
            center!!.greatCircleLocation(
                0 + headingAdjustment + heading,
                spineRadius[i],
                POSITION
            )
            addVertex(rc, POSITION.latitude, POSITION.longitude, 0.0)
        }


        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        boundingSector.setEmpty()
        boundingSector.union(
            vertexArray.array(),
            vertexArray.size(),
            VERTEX_STRIDE
        )
        boundingBox.setToUnitBox() // Surface/geographic shape bounding box is unused
    }


    /**
     * Calculate the number of times to split the edges of the shape for geometry assembly.
     *
     * @param rc current RenderContext
     *
     * @return an even number of intervals
     */
    protected fun computeIntervals(rc: RenderContext): Int {
        var intervals = MIN_INTERVALS
        if (intervals >= maximumIntervals) {
            return intervals // use at least the minimum number of intervals
        }
        val centerPoint = rc.geographicToCartesian(
            center!!.latitude,
            center!!.longitude,
            center!!.altitude,
            altitudeMode,
            POINT
        )
        val maxRadius = Math.max(majorRadius, minorRadius)
        val cameraDistance = centerPoint.distanceTo(rc.cameraPoint) - maxRadius
        if (cameraDistance <= 0) {
            return maximumIntervals // use the maximum number of intervals when the camera is very close
        }
        val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
        val circumferencePixels = computeCircumference() / metersPerPixel
        val circumferenceIntervals =
            circumferencePixels / maximumPixelsPerInterval
        val subdivisions =
            Math.log(circumferenceIntervals / intervals) / Math.log(2.0)
        val subdivisonCount = Math.max(0, Math.ceil(subdivisions).toInt())
        intervals =
            intervals shl subdivisonCount // subdivide the base intervals to achieve the desired number of intervals
        return Math.min(
            intervals,
            maximumIntervals
        ) // don't exceed the maximum number of intervals
    }

    protected fun sanitizeIntervals(intervals: Int): Int {
        return if (intervals % 2 == 0) intervals else intervals - 1
    }

    protected fun computeCircumference(): Double {
        val a = majorRadius
        val b = minorRadius
        return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)))
    }

    protected fun assembleElements(rc: RenderContext) {
        // Generate the interior element buffer with spine
        var interiorIdx = intervals
        // Add the anchor leg
        interiorElements.add(0.toShort())
        interiorElements.add(1.toShort())
        // Tessellate the interior
        for (i in 2 until intervals) {
            // Add the corresponding interior spine point if this isn't the vertex following the last vertex for the
            // negative major axis
            if (i != intervals / 2 + 1) {
                if (i > intervals / 2) {
                    interiorElements.add((--interiorIdx).toShort())
                } else {
                    interiorElements.add(interiorIdx++.toShort())
                }
            }
            // Add the degenerate triangle at the negative major axis in order to flip the triangle strip back towards
            // the positive axis
            if (i == intervals / 2) {
                interiorElements.add(i.toShort())
            }
            // Add the exterior vertex
            interiorElements.add(i.toShort())
        }
        // Complete the strip
        interiorElements.add((--interiorIdx).toShort())
        interiorElements.add(0.toShort())

        // Generate the outline element buffer
        for (i in 0 until intervals) {
            outlineElements.add(i.toShort())
        }
    }

    protected fun addVertex(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double
    ) {
        vertexArray.add(longitude.toFloat())
        vertexArray.add(latitude.toFloat())
        vertexArray.add(altitude.toFloat())
        // reserved for future texture coordinate use
        vertexArray.add(0f)
        vertexArray.add(0f)
        vertexArray.add(0f)
    }

    override fun reset() {
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
    }
}