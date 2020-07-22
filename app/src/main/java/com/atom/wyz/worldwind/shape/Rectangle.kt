package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawShapeState
import com.atom.wyz.worldwind.draw.DrawableSurfaceShape
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Rectangle : AbstractShape {

    companion object {
        const val VERTEX_STRIDE = 6

        val SCRATCH = Location()

        val START = Location()

        val END = Location()

        fun nextCacheKey(): Any {
            return Any()
        }
    }

    /**
     * Then center position of the Rectangle
     */
    var center: Location? = null

    protected var height = 0.0
        set(value) {
            field = value
            reset()
        }

    protected var width = 0.0
        set(value) {
            field = value
            reset()
        }

    protected var headingDegrees = 0.0
        set(value) {
            field = value
            reset()
        }

    protected var numberEdgeIntervals = 16

    protected var heightSegments = 0

    protected var widthSegments = 0

    protected var vertexArray = SimpleFloatArray()

    protected var interiorElements = SimpleShortArray()

    protected var outlineElements = SimpleShortArray()

    protected var vertexBufferKey: Any = nextCacheKey()

    protected var elementBufferKey: Any = nextCacheKey()


    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(
        latitude: Double,
        longitude: Double,
        width: Double,
        height: Double,
        headingDegrees: Double,
        attributes: ShapeAttributes
    ) : super(attributes) {
        if (latitude > 90 || latitude < -90) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "constructor", "invalid latitude")
            )
        }
        if (longitude > 180 || longitude < -180) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "constructor", "invalid longitude")
            )
        }
        if (width < 0 || height < 0) {
            throw IllegalArgumentException(
                Logger.logMessage(
                    Logger.ERROR,
                    "Rectangle",
                    "constructor",
                    "width and height must be positive"
                )
            )
        }
        this.center = Location(latitude, longitude)
        this.width = width
        this.height = height
        this.headingDegrees = headingDegrees
    }

    fun setCenter(latitude: Double, longitude: Double) {
        if (center == null) {
            center = Location(latitude, longitude)
        } else {
            center!!.set(latitude, longitude)
        }
        reset()
    }


    override fun makeDrawable(rc: RenderContext) {
        if (center == null || height == 0.0 || width == 0.0) {
            return  // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            this.assembleElements(rc)
            vertexBufferKey = nextCacheKey()
            elementBufferKey = nextCacheKey()
        }

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
            ) as BasicProgram
        }
        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer = BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(vertexBufferKey, drawState.vertexBuffer!!)
        }
        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size = interiorElements.size() * 2 + outlineElements.size() * 2
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                    .asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            drawState.elementBuffer = BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(elementBufferKey, drawState.elementBuffer!!)
        }

        this.drawInterior(rc, drawState)
        this.drawOutline(rc, drawState)
        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexStride = VERTEX_STRIDE * 4 // stride in bytes

        drawState.enableCullFace = false
        drawState.enableDepthTest = activeAttributes!!.depthTest
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
        drawState.texCoordAttrib.offset = 20
        drawState.texCoordAttrib.size = 1
        drawState.drawElements(
            GLES20.GL_LINE_LOOP, outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 /*offset*/
        )
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        return vertexArray.size() == 0
    }

    protected fun assembleGeometry(rc: RenderContext) {

        // Clear the shape's vertex array. This array will accumulate values as the shapes's
        // geometry is assembled.
        vertexArray.clear()

        // Attempt to distribute the edge intervals based on the aspect ratio of the rectangle
        val ratio = height / width
        widthSegments = Math.min(
            numberEdgeIntervals - 2,
            Math.max(2, (numberEdgeIntervals / 2 / ratio).toInt())
        )
        heightSegments = numberEdgeIntervals - widthSegments

        // Calculate the width into radians
        val widthRadians =
            width / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)
        val heightRadians =
            height / rc.globe.getRadiusAt(center!!.latitude, center!!.longitude)

        // Start in the top left corner and work left to right and then down.
        val widthStep = 1.0 / widthSegments
        val heightStep = 1.0 / heightSegments
        for (j in 0..heightSegments) {
            // Transit from the center vertically for each pass
            center!!.greatCircleLocation(
                headingDegrees,
                heightRadians / 2 - j * heightStep * heightRadians,
                SCRATCH
            )
            // Start from the "negative" side (furtherest west if no heading is applied)
            SCRATCH.greatCircleLocation(
                headingDegrees - 90.0,
                widthRadians / 2,
                START
            )
            // End at the "positive" side
            SCRATCH.greatCircleLocation(
                headingDegrees + 90.0,
                widthRadians / 2,
                END
            )
            // Sample from negative to positive along the path
            for (i in 0..widthSegments) {
                START.interpolateAlongPath(
                    WorldWind.GREAT_CIRCLE,
                    i * widthStep,
                    END,
                    SCRATCH
                )
                addVertex(
                    rc,
                    SCRATCH.latitude,
                    SCRATCH.longitude,
                    0.0
                )
            }
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

    protected fun assembleElements(rc: RenderContext?) {
        // Generate interior element buffer
        for (j in 0 until heightSegments) {
            for (i in 0..widthSegments) {
                interiorElements.add((j * (widthSegments + 1) + i).toShort())
                interiorElements.add(((j + 1) * (widthSegments + 1) + i).toShort())
            }
            // add the degenerate triangles to wrap the strips
            if (j != heightSegments - 1) {
                interiorElements.add(((j + 1) * (widthSegments + 1) + widthSegments).toShort())
                interiorElements.add(((j + 1) * (widthSegments + 1)).toShort())
            }
        }

        // Generate outline element buffer
        // Top - left to right
        for (i in 0 until widthSegments) {
            outlineElements.add(i.toShort())
        }
        // Right - top to bottom
        for (i in 0 until heightSegments) {
            outlineElements.add((i * (widthSegments + 1) + widthSegments).toShort())
        }
        // Bottom - right to left
        var elements = (heightSegments + 1) * (widthSegments + 1)
        for (i in 0 until widthSegments) {
            outlineElements.add((--elements).toShort())
        }
        // Left - bottom to top
        for (i in heightSegments downTo 1) {
            outlineElements.add((i * (widthSegments + 1)).toShort())
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