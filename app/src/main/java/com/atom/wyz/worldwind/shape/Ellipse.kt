package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import android.util.SparseArray
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.attribute.ShapeAttributes
import com.atom.wyz.worldwind.draw.DrawShapeState
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableShape
import com.atom.wyz.worldwind.draw.DrawableSurfaceShape
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.GpuTexture
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Ellipse : AbstractShape {
    companion object {
        protected const val VERTEX_STRIDE = 6
        /**
         * The minimum number of intervals that will be used for geometry generation.
         */
        protected const val MIN_INTERVALS = 32

        /**
         * Key for Range object in the element buffer describing the top of the Ellipse.
         */
        protected const val TOP_RANGE = 0

        /**
         * Key for Range object in the element buffer describing the outline of the Ellipse.
         */
        protected const val OUTLINE_RANGE = 1

        /**
         * Key for Range object in the element buffer describing the extruded sides of the Ellipse.
         */
        protected const val SIDE_RANGE = 2

        protected val defaultInteriorImageOptions: ImageOptions = ImageOptions()

        protected val defaultOutlineImageOptions: ImageOptions = ImageOptions()

        protected var elementBufferKeys = SparseArray<Any>()

        private val scratchPosition: Position = Position()

        private val scratchPoint: Vec3 = Vec3()

        init {
            defaultInteriorImageOptions.wrapMode = WorldWind.REPEAT
            defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR
            defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT
        }

        protected fun computeNumberSpinePoints(intervals: Int): Int {
            return intervals / 2 - 1
        }

        protected fun computeIndexOffset(intervals: Int): Int {
            return intervals + computeNumberSpinePoints(intervals)
        }

        protected fun assembleElements(intervals: Int): BufferObject? {
            // Create temporary storage for elements
            val elements = SimpleShortArray()

            // Generate the top element buffer with spine
            var interiorIdx = intervals
            val offset: Int = Ellipse.computeIndexOffset(intervals)

            // Add the anchor leg
            elements.add(0.toShort())
            elements.add(1.toShort())
            // Tessellate the interior
            for (i in 2 until intervals) {
                // Add the corresponding interior spine point if this isn't the vertex following the last vertex for the
                // negative major axis
                if (i != intervals / 2 + 1) {
                    if (i > intervals / 2) {
                        elements.add((--interiorIdx).toShort())
                    } else {
                        elements.add(interiorIdx++.toShort())
                    }
                }
                // Add the degenerate triangle at the negative major axis in order to flip the triangle strip back towards
                // the positive axis
                if (i == intervals / 2) {
                    elements.add(i.toShort())
                }
                // Add the exterior vertex
                elements.add(i.toShort())
            }
            // Complete the strip
            elements.add((--interiorIdx).toShort())
            elements.add(0.toShort())
            val topRange = Range(0, elements.size())

            // Generate the outline element buffer
            for (i in 0 until intervals) {
                elements.add(i.toShort())
            }
            val outlineRange = Range(topRange.upper, elements.size())

            // Generate the side element buffer
            for (i in 0 until intervals) {
                elements.add(i.toShort())
                elements.add((i + offset).toShort())
            }
            elements.add(0.toShort())
            elements.add(offset.toShort())
            val sideRange = Range(outlineRange.upper, elements.size())

            // Generate a buffer for the element
            val size: Int = elements.size() * 2
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                    .asShortBuffer()
            buffer.put(elements.array(), 0, elements.size())
            val elementBuffer = BufferObject(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                size,
                buffer.rewind()
            )
            elementBuffer.ranges.put(TOP_RANGE, topRange)
            elementBuffer.ranges.put(OUTLINE_RANGE, outlineRange)
            elementBuffer.ranges.put(SIDE_RANGE, sideRange)
            return elementBuffer
        }
    }

    /**
     * The ellipse's geographic center position.
     */
     var center: Position = Position()
    set(value) {
        field.set(value)
        reset()
    }

    /**
     * The ellipse's radius perpendicular to it's heading, in meters.
     */
     var majorRadius = 0.0
        set(value) {
            field = (value)
            reset()
        }
    /**
     * The ellipse's radius parallel to it's heading, in meters.
     */
     var minorRadius = 0.0
        set(value) {
            field = (value)
            reset()
        }
    /**
     * The ellipse's heading in degrees clockwise from North.
     */
     var heading = 0.0
        set(value) {
            field = (value)
            reset()
        }
    /**
     * Draw sides of the ellipse which extend from the defined position and altitude to the ground.
     */
     var extrude = false
        set(value) {
            field = (value)
            reset()
        }
    /**
     * Determines whether this ellipse's geometry follows the terrain surface or is fixed at a constant altitude.
     */
     var followTerrain = false
        set(value) {
            field = (value)
            reset()
        }
    /**
     * The maximum pixels a single edge interval will span before the number of intevals is increased. Increasing this
     * value will make ellipses appear coarser.
     */
     var maximumPixelsPerInterval = 50.0

    /**
     * The maximum number of angular intervals that may be used to assemble the ellipse's geometry for rendering.
     */
     var maximumIntervals = 256
        set(value) {
            field = (value)
            reset()
        }
    /**
     * The number of intervals used for generating geometry. Clamped between MIN_INTERVALS and maximumIntervals.
     * Will always be even.
     */
    protected var activeIntervals = 0

    protected var vertexArray: FloatArray ?= null

    protected var vertexIndex = 0

    protected var vertexBufferKey = Any()

    protected var vertexOrigin: Vec3 = Vec3()

    protected var isSurfaceShape = false

    protected var texCoord1d = 0.0

    protected var texCoord2d: Vec3 = Vec3()

    protected var texCoordMatrix: Matrix3 = Matrix3()

    protected var modelToTexCoord: Matrix4 = Matrix4()

    protected var cameraDistance = 0.0

    protected var prevPoint: Vec3 = Vec3()

    constructor() {}

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(center: Position,
                majorRadius: Double,
                minorRadius: Double) {
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
        this.center = Position(center)
        this.majorRadius = majorRadius
        this.minorRadius = minorRadius
    }

    override fun makeDrawable(rc: RenderContext) {
        if (majorRadius == 0.0 && minorRadius == 0.0) {
            return  // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            vertexBufferKey = Any()
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
            cameraDistance = cameraDistanceGeographic(rc, boundingSector)
        } else {
            val pool: Pool<DrawableShape> = rc.getDrawablePool(DrawableShape::class.java)
            drawable = DrawableShape.obtain(pool)
            drawState = drawable.drawState
            cameraDistance = boundingBox.distanceTo(rc.cameraPoint)
        }
        // Use the basic GLSL program to draw the shape.
        drawState.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawState.program == null) {
            drawState.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }
        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray!!.size * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray, 0, vertexArray!!.size)
            drawState.vertexBuffer = BufferObject(
                GLES20.GL_ARRAY_BUFFER,
                size,
                buffer.rewind()
            )
            rc.putBufferObject(vertexBufferKey, drawState.vertexBuffer!!)
        }
        // Get the attributes of the element buffer
        var elementBufferKey = elementBufferKeys[activeIntervals]
        if (elementBufferKey == null) {
            elementBufferKey = Any()
            elementBufferKeys.put(activeIntervals, elementBufferKey)
        }

        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            drawState.elementBuffer = assembleElements(activeIntervals)
            rc.putBufferObject(elementBufferKey, drawState.elementBuffer!!)
        }

        if (isSurfaceShape) {
            this.drawInterior(rc, drawState)
            this.drawOutline(rc, drawState)
        } else {
            this.drawOutline(rc, drawState)
            this.drawInterior(rc, drawState)
        }
        // Configure the drawable according to the shape's attributes.
        drawState.vertexOrigin.set(vertexOrigin)
        drawState.vertexStride = VERTEX_STRIDE * 4 // stride in bytes

        drawState.enableCullFace = extrude
        drawState.enableDepthTest = activeAttributes!!.depthTest
        // Enqueue the drawable for processing on the OpenGL thread.
        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            rc.offerShapeDrawable(drawable, cameraDistance)
        }
    }
    protected fun drawInterior(rc: RenderContext, drawState: DrawShapeState) {
        if (!activeAttributes!!.drawInterior) {
            return
        }
        // Configure the drawable to use the interior texture when drawing the interior.
        if (activeAttributes!!.interiorImageSource != null) {
            var texture: GpuTexture? = rc.getTexture(activeAttributes!!.interiorImageSource!!)
            if (texture == null) {
                texture = rc.retrieveTexture(
                    activeAttributes!!.interiorImageSource!!,
                    defaultInteriorImageOptions
                )
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                computeRepeatingTexCoordTransform(texture, metersPerPixel, texCoordMatrix)
                drawState.texture=(texture)
                drawState.texCoordMatrix=(texCoordMatrix)
            }
        } else {
            drawState.texture=(null)
        }

        // Configure the drawable to display the shape's interior.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor)
        drawState.texCoordAttrib.size = 2
        drawState.texCoordAttrib.offset = 12
        val top: Range = drawState.elementBuffer!!.ranges.get(TOP_RANGE)
        drawState.drawElements(
            GLES20.GL_TRIANGLE_STRIP, top.length(),
            GLES20.GL_UNSIGNED_SHORT, top.lower * 2 /*offset*/
        )
        if (extrude) {
            val side: Range = drawState.elementBuffer!!.ranges.get(SIDE_RANGE)
            drawState.texture = (null)
            drawState.drawElements(
                GLES20.GL_TRIANGLE_STRIP, side.length(),
                GLES20.GL_UNSIGNED_SHORT, side.lower * 2
            )
        }
    }

    protected fun drawOutline(rc: RenderContext, drawState: DrawShapeState) {
        if (!activeAttributes!!.drawOutline) {
            return
        }

        // Configure the drawable to use the outline texture when drawing the outline.
        if (activeAttributes!!.outlineImageSource != null) {
            var texture: GpuTexture? = rc.getTexture(activeAttributes!!.outlineImageSource!!)
            if (texture == null) {
                texture = rc.retrieveTexture(
                    activeAttributes!!.outlineImageSource!!,
                    defaultOutlineImageOptions
                )
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                computeRepeatingTexCoordTransform(texture, metersPerPixel, texCoordMatrix)
                drawState.texture = (texture)
                drawState.texCoordMatrix = (texCoordMatrix)
            }
        } else {
            drawState.texture = (null)
        }

        // Configure the drawable to display the shape's outline.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
        drawState.lineWidth(activeAttributes!!.outlineWidth)
        drawState.texCoordAttrib.size = 1
        drawState.texCoordAttrib.offset = 20
        val outline: Range = drawState.elementBuffer!!.ranges.get(OUTLINE_RANGE)
        drawState.drawElements(
            GLES20.GL_LINE_LOOP, outline.length(),
            GLES20.GL_UNSIGNED_SHORT, outline.lower * 2 /*offset*/
        )
        if (activeAttributes!!.drawVerticals && extrude) {
            val side: Range = drawState.elementBuffer!!.ranges.get(SIDE_RANGE)
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.texture = (null)
            drawState.drawElements(
                GLES20.GL_LINES, side.length(),
                GLES20.GL_UNSIGNED_SHORT, side.lower * 2
            )
        }
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        val calculatedIntervals = computeIntervals(rc)
        val sanitizedIntervals = sanitizeIntervals(calculatedIntervals)
        if (vertexArray == null || sanitizedIntervals != activeIntervals) {
            activeIntervals = sanitizedIntervals
            return true
        }
        return false
    }

    protected fun assembleGeometry(rc: RenderContext) {
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as goegraphic geometry.
        isSurfaceShape = altitudeMode == WorldWind.CLAMP_TO_GROUND && followTerrain

        // Compute a matrix that transforms from Cartesian coordinates to shape texture coordinates.
        determineModelToTexCoord(rc)

        // Use the ellipse's center position as the local origin for vertex positions.
        if (isSurfaceShape) {
            vertexOrigin.set(center.longitude, center.latitude, center.altitude)
        } else {
            rc.geographicToCartesian(
                center.latitude,
                center.longitude,
                center.altitude,
                altitudeMode,
                scratchPoint
            )
            vertexOrigin.set(
                scratchPoint.x,
                scratchPoint.y,
                scratchPoint.z
            )
        }

        // Determine the number of spine points
        val spineCount: Int = computeNumberSpinePoints(activeIntervals) // activeIntervals must be even

        // Clear the shape's vertex array. The array will accumulate values as the shapes's geometry is assembled.
        vertexIndex = 0
        if (extrude && !isSurfaceShape) {
            vertexArray =
                FloatArray((activeIntervals * 2 + spineCount) * VERTEX_STRIDE)
        } else {
            vertexArray =
                FloatArray((activeIntervals + spineCount) * VERTEX_STRIDE)
        }

        // Check if minor radius is less than major in which case we need to flip the definitions and change the phase
        val isStandardAxisOrientation = majorRadius > minorRadius
        val headingAdjustment: Double = if (isStandardAxisOrientation) 90.0 else 0.0

        // Vertex generation begins on the positive major axis and works ccs around the ellipse. The spine points are
        // then appended from positive major axis to negative major axis.
        val deltaRadians = 2 * Math.PI / activeIntervals
        val majorArcRadians: Double
        val minorArcRadians: Double
        val globeRadius =
            Math.max(rc.globe.getEquatorialRadius(), rc.globe.getPolarRadius())
        if (isStandardAxisOrientation) {
            majorArcRadians = majorRadius / globeRadius
            minorArcRadians = minorRadius / globeRadius
        } else {
            majorArcRadians = minorRadius / globeRadius
            minorArcRadians = majorRadius / globeRadius
        }

        // Determine the offset from the top and extruded vertices
        val arrayOffset: Int = computeIndexOffset(activeIntervals) * VERTEX_STRIDE
        // Setup spine radius values
        var spineIdx = 0
        val spineRadius = DoubleArray(spineCount)

        // Iterate around the ellipse to add vertices
        for (i in 0 until activeIntervals) {
            val radians = deltaRadians * i
            val x = Math.cos(radians) * majorArcRadians
            val y = Math.sin(radians) * minorArcRadians
            val azimuthDegrees =
                Math.toDegrees(-Math.atan2(y, x))
            val arcRadius = Math.sqrt(x * x + y * y)
            // Calculate the great circle location given this activeIntervals step (azimuthDegrees) a correction value to
            // start from an east-west aligned major axis (90.0) and the user specified user heading value
            val azimuth = azimuthDegrees + headingAdjustment + heading
            val loc: Location =
                center.greatCircleLocation(azimuth, arcRadius, scratchPosition)
            addVertex(
                rc,
                loc.latitude,
                loc.longitude,
                center.altitude,
                arrayOffset,
                this.extrude
            )
            // Add the major arc radius for the spine points. Spine points are vertically coincident with exterior
            // points. The first and middle most point do not have corresponding spine points.
            if (i > 0 && i < activeIntervals / 2) {
                spineRadius[spineIdx++] = x
            }
        }

        // Add the interior spine point vertices
        for (i in 0 until spineCount) {
            center.greatCircleLocation(
                0 + headingAdjustment + heading,
                spineRadius[i],
                scratchPosition
            )
            addVertex(
                rc,
                scratchPosition.latitude,
                scratchPosition.longitude,
                center.altitude,
                arrayOffset,
                false
            )
        }

        // Compute the shape's bounding sector from its assembled coordinates.
        if (isSurfaceShape) {
            boundingSector.setEmpty()
            boundingSector.union(
                vertexArray!!,
                vertexArray!!.size,
                VERTEX_STRIDE
            )
            boundingSector.translate(vertexOrigin.y /*lat*/, vertexOrigin.x /*lon*/)
            boundingBox.setToUnitBox() // Surface/geographic shape bounding box is unused
        } else {
            boundingBox.setToPoints(
                vertexArray!!,
                vertexArray!!.size,
                VERTEX_STRIDE
            )
            boundingBox.translate(
                vertexOrigin.x,
                vertexOrigin.y,
                vertexOrigin.z
            )
            boundingSector.setEmpty()
        }
    }



    protected fun addVertex(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        offset: Int,
        isExtrudedSkirt: Boolean
    ) {
        var offsetVertexIndex = vertexIndex + offset
        var point = rc.geographicToCartesian(
            latitude,
            longitude,
            altitude,
            altitudeMode,
            scratchPoint
        )
        val texCoord2d = texCoord2d.set(point).multiplyByMatrix(modelToTexCoord)
        if (vertexIndex == 0) {
            texCoord1d = 0.0
            prevPoint.set(point)
        } else {
            texCoord1d += point.distanceTo(prevPoint)
            prevPoint.set(point)
        }
        if (isSurfaceShape) {
            vertexArray!![vertexIndex++] =
                (longitude - vertexOrigin.x).toFloat()
            vertexArray!![vertexIndex++] =
                (latitude - vertexOrigin.y).toFloat()
            vertexArray!![vertexIndex++] =
                (altitude - vertexOrigin.z).toFloat()
            // reserved for future texture coordinate use
            // reserved for future texture coordinate use
            vertexArray!![vertexIndex++] = texCoord2d.x.toFloat()
            vertexArray!![vertexIndex++] = texCoord2d.y.toFloat()
            vertexArray!![vertexIndex++] = texCoord1d.toFloat()
        } else {
            vertexArray!![vertexIndex++] =
                (point.x - vertexOrigin.x).toFloat()
            vertexArray!![vertexIndex++] =
                (point.y - vertexOrigin.y).toFloat()
            vertexArray!![vertexIndex++] =
                (point.z - vertexOrigin.z).toFloat()
            vertexArray!![vertexIndex++] = texCoord2d.x.toFloat()
            vertexArray!![vertexIndex++] = texCoord2d.y.toFloat()
            vertexArray!![vertexIndex++] = texCoord1d.toFloat()
            if (isExtrudedSkirt) {
                point = rc.geographicToCartesian(
                    latitude,
                    longitude,
                    0.0,
                    WorldWind.CLAMP_TO_GROUND,
                    scratchPoint
                )
                vertexArray!![offsetVertexIndex++] = (point.x - vertexOrigin.x).toFloat()
                vertexArray!![offsetVertexIndex++] = (point.y - vertexOrigin.y).toFloat()
                vertexArray!![offsetVertexIndex++] = (point.z - vertexOrigin.z).toFloat()
                vertexArray!![offsetVertexIndex++] = 0f //unused
                vertexArray!![offsetVertexIndex++] = 0f //unused
                vertexArray!![offsetVertexIndex++] = 0f //unused
            }
        }
    }

    protected fun determineModelToTexCoord(rc: RenderContext) {
        val point = rc.geographicToCartesian(
            center.latitude,
            center.longitude,
            center.altitude,
            altitudeMode,
            scratchPoint
        )
        modelToTexCoord =
            rc.globe.cartesianToLocalTransform(point.x, point.y, point.z, modelToTexCoord)
        modelToTexCoord.invertOrthonormal()
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
            center.latitude,
            center.longitude,
            center.altitude,
            altitudeMode,
            scratchPoint
        )
        val maxRadius = Math.max(majorRadius, minorRadius)
        val cameraDistance = centerPoint.distanceTo(rc.cameraPoint) - maxRadius
        if (cameraDistance <= 0) {
            return maximumIntervals // use the maximum number of intervals when the camera is very close
        }
        val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
        val circumferencePixels = computeCircumference() / metersPerPixel
        val circumferenceIntervals = circumferencePixels / maximumPixelsPerInterval
        val subdivisions = Math.log(circumferenceIntervals / intervals) / Math.log(2.0)
        val subdivisionCount = Math.max(0, Math.ceil(subdivisions).toInt())
        intervals = intervals shl subdivisionCount // subdivide the base intervals to achieve the desired number of intervals
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
    override fun reset() {
        this.vertexArray= null
    }
}