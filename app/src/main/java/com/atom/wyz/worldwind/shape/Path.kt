package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableShape
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Matrix3
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.render.BasicProgram
import com.atom.wyz.worldwind.render.BufferObject
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Path : AbstractShape {
    companion object {
        private fun nextCacheKey() = Any()
        protected const val VERTEX_STRIDE = 4
        protected const val CLAMP_TO_GROUND_DEPTH_OFFSET = -0.01
        protected const val FOLLOW_TERRAIN_SEGMENT_LENGTH = 1000.0
        protected val defaultOutlineImageOptions: ImageOptions = ImageOptions()

        init {
            defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR;
            defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT;
        }
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

    protected var texCoord1d = 0.0

    private val point = Vec3()

    private val prevPoint = Vec3()

    private val texCoordMatrix = Matrix3()

    private val intermediateLocation = Location()

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
        // Obtain a drawable form the render context pool, and compute distance to the render camera.
        val pool = rc.getDrawablePool(DrawableShape::class.java)
        val drawable: Drawable = DrawableShape.obtain(pool)
        val drawState = (drawable as DrawableShape).drawState
        val cameraDistance = cameraDistanceCartesian(
            rc,
            vertexArray.array(),
            vertexArray.size(),
            Path.VERTEX_STRIDE,
            vertexOrigin
        )

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

        // Configure the drawable's vertex texture coordinate attribute.
        drawState.texCoordAttrib.size = 1 /*size*/
        drawState.texCoordAttrib.offset = 12 /*offset*/
        // Configure the drawable to use the outline texture when drawing the outline.
        if (activeAttributes!!.drawOutline && activeAttributes!!.outlineImageSource != null) {
            var texture: GpuTexture? = rc.getTexture(activeAttributes!!.outlineImageSource!!)
            if (texture == null) {
                texture = rc.retrieveTexture(activeAttributes!!.outlineImageSource, defaultOutlineImageOptions)
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                val texCoordMatrix = texCoordMatrix.setToIdentity()
                texCoordMatrix.setScale(1.0 / (texture.textureWidth * metersPerPixel), 1.0)
                texCoordMatrix.multiplyByMatrix(texture.texCoordTransform)
                drawState.texture = (texture)
                drawState.texCoordMatrix = (texCoordMatrix)
            }
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

        // Disable texturing for the remaining drawable primitives.
        drawState.texture = (null)

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
        drawState.vertexStride = Path.VERTEX_STRIDE * 4
        drawState.enableCullFace = false
        drawState.enableDepthTest = activeAttributes!!.depthTest
        drawState.depthOffset = if (altitudeMode == WorldWind.CLAMP_TO_GROUND) CLAMP_TO_GROUND_DEPTH_OFFSET else 0.0

        // Enqueue the drawable for processing on the OpenGL thread.
        if (altitudeMode == WorldWind.CLAMP_TO_GROUND) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            rc.offerShapeDrawable(drawable, cameraDistance)
        }
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
        boundingBox.setToPoints(vertexArray.array(), vertexArray.size(), Path.VERTEX_STRIDE)
        boundingBox.translate(vertexOrigin.x, vertexOrigin.y, vertexOrigin.z)
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

        var numSubsegments = maximumIntermediatePoints + 1

        if (followTerrain) {
            val lengthMeters: Double = length * rc.globe!!.equatorialRadius
            val followTerrainNumSubsegments =
                (lengthMeters / Path.FOLLOW_TERRAIN_SEGMENT_LENGTH).toInt()
            if (numSubsegments < followTerrainNumSubsegments) {
                numSubsegments = followTerrainNumSubsegments
            }
        }

        val deltaDist = length / numSubsegments
        val deltaAlt = (end.altitude - begin.altitude) / numSubsegments
        var dist = deltaDist
        var alt = begin.altitude + deltaAlt

        for (idx in 1 until numSubsegments) {
            val loc = intermediateLocation

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
        intermediate: Boolean
    ) {
        var altitude_temp = altitude
        if (altitudeMode == WorldWind.CLAMP_TO_GROUND) {
            altitude_temp = 0.0
        }

        val vertex: Int = vertexArray.size() / Path.VERTEX_STRIDE
        var point = rc.geographicToCartesian(latitude, longitude, altitude_temp, WorldWind.ABSOLUTE, point)

        if (vertexArray.size() == 0) {
            vertexOrigin.set(point)
            prevPoint.set(point)
            texCoord1d = 0.0
        } else {
            texCoord1d += point.distanceTo(prevPoint)
            prevPoint.set(point)
        }

        vertexArray.add((point.x - vertexOrigin.x).toFloat())
        vertexArray.add((point.y - vertexOrigin.y).toFloat())
        vertexArray.add((point.z - vertexOrigin.z).toFloat())

        vertexArray.add(texCoord1d.toFloat())
        outlineElements.add(vertex.toShort())

        if (extrude) {
            // TODO clamp to ground points must be continually updated to reflect change in terrain
            // TODO use absolute altitude 0 as a temporary workaround while the globe has no terrain
            point = rc.geographicToCartesian(latitude, longitude, 0.0, WorldWind.ABSOLUTE, this.point)
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())
            vertexArray.add(0f)
            interiorElements.add(vertex.toShort())
            interiorElements.add((vertex + 1).toShort())
        }

        if (extrude && !intermediate) {
            verticalElements.add(vertex.toShort())
            verticalElements.add((vertex + 1).toShort())
        }
    }
}