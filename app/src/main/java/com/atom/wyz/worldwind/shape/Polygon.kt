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
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.glu.GLU
import com.atom.wyz.worldwind.util.glu.GLUtessellator
import com.atom.wyz.worldwind.util.glu.GLUtessellatorCallbackAdapter
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Polygon : AbstractShape {

    companion object {
        protected const val VERTEX_ORIGINAL = 0

        protected const val VERTEX_INTERMEDIATE = 1

        protected const val VERTEX_COMBINED = 2

        protected fun nextCacheKey(): Any {
            return Any()
        }
    }

    var boundaries =
        mutableListOf<List<Position>>()

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

    protected var tessCallback: GLUtessellatorCallbackAdapter = object : GLUtessellatorCallbackAdapter() {

        override fun combineData(
            coords: DoubleArray?,
            data: Array<Any>?,
            weight: FloatArray?,
            outData: Array<Any>?,
            polygonData: Any?
        ) {
            tessCombine(polygonData as RenderContext, coords!!, data, weight, outData)
        }


        override fun vertexData(vertexData: Any?, polygonData: Any?) {
            fun vertexData(vertexData: Any?, polygonData: Any?) {
                tessVertex(polygonData as RenderContext?, vertexData!!)
            }
        }

        override fun edgeFlagData(boundaryEdge: Boolean, polygonData: Any?) {
            fun edgeFlagData(boundaryEdge: Boolean, polygonData: Any?) {
                tessEdgeFlag(polygonData as RenderContext?, boundaryEdge)
            }
        }


        override fun errorData(errnum: Int, polygonData: Any?) {
            fun errorData(errnum: Int, polygonData: Any?) {
                tessError(polygonData as RenderContext?, errnum)
            }
        }
    }

    val point = Vec3()

    val loc: Location = Location()

    val tessCoords = DoubleArray(3)

    val tessVertices = IntArray(3)

    val tessEdgeFlags = BooleanArray(3)

    var tessEdgeFlag = true

    var tessVertexCount = 0

    constructor() {}

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(positions: List<Position>?) {
        if (positions == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "constructor", "missingList")
            )
        }
        boundaries.add(positions)
    }

    constructor(positions: List<Position>?, attributes: ShapeAttributes) : super(attributes) {
        if (positions == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "constructor", "missingList")
            )
        }
        boundaries.add(positions)
    }

    fun getBoundaryCount(): Int {
        return boundaries.size
    }

    fun getBoundary(index: Int): List<Position> {
        require(!(index < 0 || index >= boundaries.size)) {
            Logger.logMessage(
                Logger.ERROR,
                "Polygon",
                "getBoundary",
                "invalidIndex"
            )
        }
        return boundaries[index]
    }

    fun setBoundary(
        index: Int,
        positions: List<Position>?
    ): List<Position> {
        require(!(index < 0 || index >= boundaries.size)) {
            Logger.logMessage(
                Logger.ERROR,
                "Polygon",
                "setBoundary",
                "invalidIndex"
            )
        }
        requireNotNull(positions) { Logger.logMessage(Logger.ERROR, "Polygon", "setBoundary", "missingList") }
        reset()
        return boundaries.set(index, positions)
    }

    fun addBoundary(positions: List<Position>?) {
        requireNotNull(positions) { Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "missingList") }
        reset()
        boundaries.add(positions)
    }

    fun addBoundary(index: Int, positions: List<Position>?) {
        require(!(index < 0 || index > boundaries.size)) {
            Logger.logMessage(
                Logger.ERROR,
                "Polygon",
                "addBoundary",
                "invalidIndex"
            )
        }
        requireNotNull(positions) { Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "missingList") }
        reset()
        boundaries.add(index, positions)
    }

    fun removeBoundary(index: Int): List<Position> {
        require(!(index < 0 || index >= boundaries.size)) {
            Logger.logMessage(
                Logger.ERROR,
                "Polygon",
                "removeBoundary",
                "invalidIndex"
            )
        }
        reset()
        return boundaries.removeAt(index)
    }

    fun clearBoundaries() {
        reset()
        boundaries.clear()
    }

    override fun reset() {
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
        verticalElements.clear()
    }

    override fun makeDrawable(rc: RenderContext) {
        if (boundaries.isEmpty()) {
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
            drawState = (drawable as DrawableSurfaceShape).drawState
            (drawable as DrawableSurfaceShape).sector.set(boundingSector)
        } else {
            val pool: Pool<DrawableShape> = rc.getDrawablePool(DrawableShape::class.java)
            drawable = DrawableShape.obtain(pool)
            drawState = (drawable as DrawableShape).drawState
        }

        // Use the basic GLSL program to draw the shape.
        // Use the basic GLSL program to draw the shape.
        drawState.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawState.program == null) {
            drawState.program =
                rc.putProgram(BasicProgram.KEY, BasicProgram(rc.resources!!)) as BasicProgram
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer = BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(vertexBufferKey, drawState.vertexBuffer!!)
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size =
                interiorElements.size() * 2 + outlineElements.size() * 2 + verticalElements.size() * 2
            val buffer =
                ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            buffer.put(verticalElements.array(), 0, verticalElements.size())
            drawState.elementBuffer = BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind())
            rc.putBufferObject(elementBufferKey, drawState.elementBuffer!!)
        }

        if (isSurfaceShape || activeAttributes!!.interiorColor.alpha >= 1.0) {
            this.drawInterior(rc, drawState)
            this.drawOutline(rc, drawState)
        } else {
            this.drawOutline(rc, drawState)
            this.drawInterior(rc, drawState)
        }

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexOrigin.set(vertexOrigin)
        drawState.enableCullFace = extrude
        drawState.enableDepthTest = activeAttributes!!.depthTest

        // Enqueue the drawable for processing on the OpenGL thread.
        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            val cameraDistance = boundingBox.distanceTo(rc.cameraPoint)
            rc.offerShapeDrawable(drawable, cameraDistance)
        }


        // Configure the drawable according to the shape's attributes.
        drawState.vertexOrigin.set(vertexOrigin)
        drawState.enableCullFace = extrude
        drawState.enableDepthTest = activeAttributes!!.depthTest

        // Enqueue the drawable for processing on the OpenGL thread.
        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            val cameraDistance = boundingBox.distanceTo(rc.cameraPoint)
            rc.offerShapeDrawable(drawable, cameraDistance)
        }
    }

    protected fun drawInterior(
        rc: RenderContext,
        drawState: DrawShapeState
    ) {
        // Configure the drawable to display the shape's interior (and its optional extruded interior).
        if (activeAttributes!!.drawInterior) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor)
            drawState.drawElements(
                GLES20.GL_TRIANGLES, interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0
            )
        }
    }

    protected fun drawOutline(rc: RenderContext, drawState: DrawShapeState) {
        // Configure the drawable to display the shape's outline.
        if (activeAttributes!!.drawOutline) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.drawElements(
                GLES20.GL_LINES, outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2
            )
        }

        // Configure the drawable to display the shape's extruded verticals.
        if (activeAttributes!!.drawOutline && activeAttributes!!.drawVerticals && extrude) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.drawElements(
                GLES20.GL_LINES, verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 + outlineElements.size() * 2
            )
        }
    }

    protected fun mustAssembleGeometry(rc: RenderContext?): Boolean {
        return vertexArray.size() == 0
    }

    protected fun assembleGeometry(rc: RenderContext) { // Prepare the vertex origin to be re-computed.
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        isSurfaceShape = altitudeMode == WorldWind.CLAMP_TO_GROUND && followTerrain
        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
// geometry is assembled.
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
        verticalElements.clear()
        val tess: GLUtessellator = rc.getTessellator()
        GLU.gluTessNormal(tess, 0.0, 0.0, 1.0)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, tessCallback)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, tessCallback)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, tessCallback)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, tessCallback)
        GLU.gluTessBeginPolygon(tess, rc)
        var boundaryIdx = 0
        val boundaryCount = boundaries.size
        while (boundaryIdx < boundaryCount) {
            val positions = boundaries[boundaryIdx]
            if (positions.isEmpty()) {
                boundaryIdx++
                continue  // no boundary to tessellate
            }
            GLU.gluTessBeginContour(tess)
            // Assemble the vertices for each edge between boundary positions.
            var begin = positions[0]
            if (vertexArray.size() == 0) {
                rc.geographicToCartesian(
                    begin.latitude,
                    begin.longitude,
                    begin.altitude,
                    altitudeMode,
                    vertexOrigin
                )
            }
            addVertex(
                rc,
                begin.latitude,
                begin.longitude,
                begin.altitude,
                VERTEX_ORIGINAL /*type*/ /*type*/
            )

            var idx = 1
            val len = positions.size
            while (idx < len) {
                val end = positions[idx]
                this.addIntermediateVertices(rc, begin, end)
                addVertex(
                    rc,
                    end.latitude,
                    end.longitude,
                    end.altitude,
                    VERTEX_ORIGINAL /*type*/ /*type*/
                )
                begin = end
                idx++
            }
            // If the the boundary is not closed, add the vertices the edge between the last and first positions.
            if (!begin.equals(positions[0])) {
                this.addIntermediateVertices(rc, begin, positions[0])
            }
            GLU.gluTessEndContour(tess)
            boundaryIdx++
        }
        GLU.gluTessEndPolygon(tess)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, null)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, null)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, null)
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, null)
        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
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

    protected fun addIntermediateVertices(
        rc: RenderContext,
        begin: Position,
        end: Position
    ) { // Compute the segment's constant properties - the segment azimuth and the segment length.
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
            return  // suppress edge vertices when the edge length less than a millimeter (on Earth)
        }

        // Add additional segment vertices when specified, and the segment length is nonzero.
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
            this.addVertex(
                rc,
                loc.latitude,
                loc.longitude,
                alt,
                VERTEX_INTERMEDIATE /*type*/ /*type*/
            )
            dist += deltaDist
            alt += deltaAlt
        }
    }

    protected fun addVertex(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        intermediate: Int
    ): Int {
        return if (isSurfaceShape) {
            addVertexGeographic(rc, latitude, longitude, altitude, intermediate)
        } else {
            addVertexCartesian(rc, latitude, longitude, altitude, intermediate)
        }
    }

    protected fun addVertexGeographic(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        intermediate: Int
    ): Int {
        val vertex = vertexArray.size() / 2
        vertexArray.add(longitude.toFloat())
        vertexArray.add(latitude.toFloat())
        if (intermediate != VERTEX_COMBINED) {
            tessCoords[0] = longitude
            tessCoords[1] = latitude
            tessCoords[2] = altitude
            GLU.gluTessVertex(rc.getTessellator(), tessCoords, 0 /*coords_offset*/, vertex)
        }
        return vertex
    }

    protected fun addVertexCartesian(
        rc: RenderContext,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        intermediate: Int
    ): Int {
        rc.geographicToCartesian(latitude, longitude, altitude, altitudeMode, point)
        val vertex = vertexArray.size() / 3
        vertexArray.add((point.x - vertexOrigin.x).toFloat())
        vertexArray.add((point.y - vertexOrigin.y).toFloat())
        vertexArray.add((point.z - vertexOrigin.z).toFloat())
        if (extrude) {
            rc.geographicToCartesian(latitude, longitude, 0.0, WorldWind.CLAMP_TO_GROUND, point)
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())
        }
        if (extrude && intermediate == VERTEX_ORIGINAL) {
            verticalElements.add(vertex.toShort())
            verticalElements.add((vertex + 1).toShort())
        }
        if (intermediate != VERTEX_COMBINED) {
            tessCoords[0] = longitude
            tessCoords[1] = latitude
            tessCoords[2] = altitude
            GLU.gluTessVertex(rc.getTessellator(), tessCoords, 0 /*coords_offset*/, vertex)
        }
        return vertex
    }

    protected fun tessCombine(
        rc: RenderContext,
        coords: DoubleArray,
        data: Array<out Any?>?,
        weight: FloatArray?,
        outData: Array<Any>?
    ) {
        val vertex = addVertex(
            rc,
            coords[1] /*lat*/,
            coords[0] /*lon*/,
            coords[2] /*alt*/,
            VERTEX_COMBINED /*type*/
        )
        outData?.let {
            it[0] = vertex
        }
    }

    protected fun tessVertex(rc: RenderContext?, vertexData: Any) {
        tessVertices[tessVertexCount] = vertexData as Int
        tessEdgeFlags[tessVertexCount] = tessEdgeFlag
        if (tessVertexCount < 2) {
            tessVertexCount++ // increment the vertex count and wait for more vertices
            return
        } else {
            tessVertexCount = 0 // reset the vertex count and process one triangle
        }
        val v0 = tessVertices[0]
        val v1 = tessVertices[1]
        val v2 = tessVertices[2]
        interiorElements.add(v0.toShort()).add(v1.toShort()).add(v2.toShort())
        if (tessEdgeFlags[0] && extrude && !isSurfaceShape) {
            interiorElements.add(v0.toShort()).add((v0 + 1).toShort()).add(v1.toShort())
            interiorElements.add(v1.toShort()).add((v0 + 1).toShort()).add((v1 + 1).toShort())
        }
        if (tessEdgeFlags[1] && extrude && !isSurfaceShape) {
            interiorElements.add(v1.toShort()).add((v1 + 1).toShort()).add(v2.toShort())
            interiorElements.add(v2.toShort()).add((v1 + 1).toShort()).add((v2 + 1).toShort())
        }
        if (tessEdgeFlags[2] && extrude && !isSurfaceShape) {
            interiorElements.add(v2.toShort()).add((v2 + 1).toShort()).add(v0.toShort())
            interiorElements.add(v0.toShort()).add((v2 + 1).toShort()).add((v0 + 1).toShort())
        }
        if (tessEdgeFlags[0]) {
            outlineElements.add(v0.toShort())
            outlineElements.add(v1.toShort())
        }
        if (tessEdgeFlags[1]) {
            outlineElements.add(v1.toShort())
            outlineElements.add(v2.toShort())
        }
        if (tessEdgeFlags[2]) {
            outlineElements.add(v2.toShort())
            outlineElements.add(v0.toShort())
        }
    }

    protected fun tessEdgeFlag(rc: RenderContext?, boundaryEdge: Boolean) {
        tessEdgeFlag = boundaryEdge
    }

    protected fun tessError(rc: RenderContext?, errnum: Int) {
        val errstr = GLU.gluErrorString(errnum)
        Logger.logMessage(
            Logger.WARN,
            "Polygon",
            "assembleGeometry",
            "Error attempting to tessellate polygon \'$errstr\'"
        )
    }

}