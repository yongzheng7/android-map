package com.atom.wyz.worldwind.shape

import android.opengl.GLES20
import com.atom.wyz.worldwind.context.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.attribute.ShapeAttributes
import com.atom.wyz.worldwind.draw.DrawShapeState
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableShape
import com.atom.wyz.worldwind.draw.DrawableSurfaceShape
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.GpuTexture
import com.atom.wyz.worldwind.util.SimpleFloatArray
import com.atom.wyz.worldwind.util.SimpleShortArray
import com.atom.wyz.worldwind.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SimplePolygon : AbstractShape {

    companion object {
        protected const val VERTEX_ORIGINAL = 0

        protected const val VERTEX_INTERMEDIATE = 1

        protected const val VERTEX_STRIDE = 6

        protected val defaultInteriorImageOptions: ImageOptions = ImageOptions()

        protected val defaultOutlineImageOptions: ImageOptions = ImageOptions()

        protected fun nextCacheKey(): Any {
            return Any()
        }

        init {
            defaultInteriorImageOptions.wrapMode = WorldWind.REPEAT
            defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR
            defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT
        }
    }

    var boundaries = mutableListOf<Position>()

    var extrude = false
        set(value) {
            field = value
            reset()
        }

    var followTerrain = false
        set(value) {
            field = value
            reset()
        }

    var vertexArray: SimpleFloatArray = SimpleFloatArray()

    var topElements: SimpleShortArray = SimpleShortArray()

    var sideElements: SimpleShortArray = SimpleShortArray()

    var outlineElements: SimpleShortArray = SimpleShortArray()

    var verticalElements: SimpleShortArray = SimpleShortArray()

    var vertexBufferKey: Any = nextCacheKey()

    var elementBufferKey: Any = nextCacheKey()

    var vertexOrigin: Vec3 = Vec3()

    var isSurfaceShape = false

    var cameraDistance = 0.0

    var texCoord1d = 0.0

    val point = Vec3()

    val prevPoint = Vec3()

    val texCoord2d = Vec3()

    val texCoordMatrix = Matrix3()

    var modelToTexCoord = Matrix4()

    val intermediateLocation: Location = Location()

    constructor() {}

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(positions: List<Position>) {
        boundaries.addAll(positions)
    }

    constructor(positions: List<Position>, attributes: ShapeAttributes) : super(attributes) {
        boundaries.addAll(positions)
    }

    override fun reset() {
        vertexArray.clear()
        topElements.clear()
        sideElements.clear()
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
        val drawable: Drawable
        val drawState: DrawShapeState
        if (isSurfaceShape) {
            val pool: Pool<DrawableSurfaceShape> = rc.getDrawablePool(DrawableSurfaceShape::class.java)
            drawable = DrawableSurfaceShape.obtain(pool)
            drawState = drawable.drawState
            cameraDistance = cameraDistanceGeographic(rc, boundingSector)
            drawable.sector.set(boundingSector)
        } else {
            val pool: Pool<DrawableShape> = rc.getDrawablePool(DrawableShape::class.java)
            drawable = DrawableShape.obtain(pool)
            drawState = drawable.drawState
            cameraDistance = cameraDistanceCartesian(rc, vertexArray.array(), vertexArray.size(), VERTEX_STRIDE, vertexOrigin)
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
            val size = vertexArray.size() * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer = BufferObject(
                GLES20.GL_ARRAY_BUFFER,
                size,
                buffer.rewind()
            )
            rc.putBufferObject(vertexBufferKey, drawState.vertexBuffer!!)
        }
        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size = topElements.size() * 2 + sideElements.size() * 2 + outlineElements.size() * 2 + verticalElements.size() * 2
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
            buffer.put(topElements.array(), 0, topElements.size())
            buffer.put(sideElements.array(), 0, sideElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            buffer.put(verticalElements.array(), 0, verticalElements.size())
            drawState.elementBuffer = BufferObject(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                size,
                buffer.rewind()
            )
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
        drawState.vertexStride = VERTEX_STRIDE * 4
        drawState.enableCullFace = extrude
        drawState.enableDepthTest = activeAttributes!!.depthTest

        // Enqueue the drawable for processing on the OpenGL thread.
        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
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
        val shapeAttributes = activeAttributes ?: return
        if (!shapeAttributes.drawInterior) {
            return
        }
        // Configure the drawable to use the interior texture when drawing the interior.
        shapeAttributes.interiorImageSource?.let {
            var texture: GpuTexture? = rc.getTexture(it)
            if (texture == null) {
                texture = rc.retrieveTexture(it, defaultInteriorImageOptions)
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                computeRepeatingTexCoordTransform(texture, metersPerPixel, texCoordMatrix)
                drawState.texture = texture
                drawState.texCoordMatrix = texCoordMatrix
            }
        } ?: let {
            drawState.texture = null
        }

        // Configure the drawable to display the shape's interior top.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.interiorColor)
        drawState.texCoordAttrib.size = (2 /*size*/)
        drawState.texCoordAttrib.offset = (12 /*offset in bytes*/)
        drawState.drawElements(
            GLES20.GL_TRIANGLES, topElements.size(),
            GLES20.GL_UNSIGNED_SHORT, 0 /*offset*/
        )

        // Configure the drawable to display the shape's interior sides.
        if (extrude) {
            drawState.texture = (null)
            drawState.drawElements(
                GLES20.GL_TRIANGLES, sideElements.size(),
                GLES20.GL_UNSIGNED_SHORT, topElements.size() * 2 /*offset*/
            )
        }
    }

    protected fun drawOutline(rc: RenderContext, drawState: DrawShapeState) {
        val shapeAttributes = activeAttributes ?: return
        if (!shapeAttributes.drawOutline) {
            return
        }
        // Configure the drawable to use the outline texture when drawing the outline.
        shapeAttributes.outlineImageSource?.let {
            var texture: GpuTexture? = rc.getTexture(it)
            if (texture == null) {
                texture = rc.retrieveTexture(it, defaultOutlineImageOptions)
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                val texCoordMatrix = texCoordMatrix.setToIdentity()
                texCoordMatrix.setScale(1.0 / (texture.textureWidth * metersPerPixel), 1.0)
                texCoordMatrix.multiplyByMatrix(texture.texCoordTransform)
                drawState.texture = (texture)
                drawState.texCoordMatrix = (texCoordMatrix)
            }
        } ?: let {
            drawState.texture = (null)
        }

        // Configure the drawable to display the shape's outline.
        // Configure the drawable to display the shape's outline.
        drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
        drawState.lineWidth(activeAttributes!!.outlineWidth)
        drawState.texCoordAttrib.size = (1 /*size*/)
        drawState.texCoordAttrib.offset = (20 /*offset in bytes*/)
        drawState.drawElements(
            GLES20.GL_LINES, outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, topElements.size() * 2 + sideElements.size() * 2 /*offset*/
        )

        // Configure the drawable to display the shape's extruded verticals.
        // Configure the drawable to display the shape's extruded verticals.
        if (activeAttributes!!.drawVerticals && extrude) {
            drawState.color(if (rc.pickMode) pickColor else activeAttributes!!.outlineColor)
            drawState.lineWidth(activeAttributes!!.outlineWidth)
            drawState.texture = (null)
            drawState.drawElements(
                GLES20.GL_LINES,
                verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT,
                topElements.size() * 2 + sideElements.size() * 2 + outlineElements.size() * 2 /*offset*/
            )
        }
    }

    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        return vertexArray.size() == 0
    }

    protected fun assembleGeometry(rc: RenderContext) {
        // Prepare the vertex origin to be re-computed.
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        isSurfaceShape = altitudeMode == WorldWind.CLAMP_TO_GROUND && followTerrain
        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
        // geometry is assembled.
        vertexArray.clear()
        topElements.clear()
        sideElements.clear()
        outlineElements.clear()
        verticalElements.clear()

        if (boundaries.isEmpty()) {
            return
        }

        this.determineModelToTexCoord(rc)

        // Assemble the vertices for each edge between boundary positions.
        var begin: Position = boundaries[0]
        addVertex(  // 添加第一个点到缓存中
            rc,
            begin.latitude,
            begin.longitude,
            begin.altitude,
            VERTEX_ORIGINAL
        )

        var idx = 1
        val len = boundaries.size
        while (idx < len) {
            val end = boundaries[idx]
            this.addIntermediateVertices(rc, begin, end)
            addVertex(
                rc,
                end.latitude,
                end.longitude,
                end.altitude,
                VERTEX_ORIGINAL
            )
            begin = end
            idx++
        }

        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        if (isSurfaceShape) {
            boundingSector.setEmpty()
            boundingSector.union(vertexArray.array(), vertexArray.size(), VERTEX_STRIDE)
            boundingSector.translate(vertexOrigin.y /*lat*/, vertexOrigin.x /*lon*/)
            boundingBox.setToUnitBox() // Surface/geographic shape bounding box is unused
        } else {
            boundingBox.setToPoints(vertexArray.array(), vertexArray.size(), VERTEX_STRIDE)
            boundingBox.translate(vertexOrigin.x, vertexOrigin.y, vertexOrigin.z)
            boundingSector.setEmpty() // Cartesian shape bounding sector is unused
        }
    }

    protected fun addIntermediateVertices(
        rc: RenderContext,
        begin: Position,
        end: Position
    ) { // Compute the segment's constant properties - the segment azimuth and the segment length.
        if (pathType == WorldWind.LINEAR) { // 如果为linear
            return  // suppress edge vertices when the path type is linear
        }
        if (maximumIntermediatePoints <= 0) { //线分割
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
            val loc = intermediateLocation
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
                VERTEX_INTERMEDIATE
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
        type: Int
    ): Int {
        val vertex: Int = vertexArray.size() / VERTEX_STRIDE // 获取顶点数组的
        var point = rc.geographicToCartesian(
            latitude,
            longitude,
            altitude,
            altitudeMode,
            point
        ) // 根据经纬度获取笛卡尔坐标
        val texCoord2d = texCoord2d.set(point).multiplyByMatrix(modelToTexCoord) // 根据平均得到的观察变换矩阵


        if (vertex == 0) {
            if (isSurfaceShape) {
                vertexOrigin.set(longitude, latitude, altitude) // 设置经纬度
            } else {
                vertexOrigin.set(point) // 设置笛卡尔
            }
            this.texCoord1d = 0.0 //
            prevPoint.set(point) // 起点
        } else {
            this.texCoord1d += point.distanceTo(prevPoint) // 其余点和前一个带你的距离
            prevPoint.set(point)
        }
        if (isSurfaceShape) {
            vertexArray.add((longitude - vertexOrigin.x).toFloat()) // 添加的是和点一之间的差值
            vertexArray.add((latitude - vertexOrigin.y).toFloat())
            vertexArray.add((altitude - vertexOrigin.z).toFloat())

            vertexArray.add((longitude - texCoord2d.x).toFloat())
            vertexArray.add((latitude - texCoord2d.y).toFloat())
            vertexArray.add(texCoord1d.toFloat())
        } else {
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())

            vertexArray.add((longitude - texCoord2d.x).toFloat())
            vertexArray.add((latitude - texCoord2d.y).toFloat())
            vertexArray.add(texCoord1d.toFloat())

            if (extrude) {
                point = rc.geographicToCartesian(
                    latitude,
                    longitude,
                    0.0,
                    WorldWind.CLAMP_TO_GROUND,
                    this.point
                )

                vertexArray.add((point.x - vertexOrigin.x).toFloat())
                vertexArray.add((point.y - vertexOrigin.y).toFloat())
                vertexArray.add((point.z - vertexOrigin.z).toFloat())
                vertexArray.add(0.toFloat() /*unused*/)
                vertexArray.add(0.toFloat() /*unused*/)
                vertexArray.add(0.toFloat() /*unused*/)
            }
            if (type == VERTEX_ORIGINAL) {
                verticalElements.add(vertex.toShort()) // 0
                verticalElements.add((vertex + 1).toShort())// 1
            }
        }
        return vertex
    }

    protected fun determineModelToTexCoord(rc: RenderContext) {
        var mx = 0.0
        var my = 0.0
        var mz = 0.0
        var numPoints = 0.0

        if (boundaries.isEmpty()) {
            return  // no boundary positions
        }
        var idx = 0
        val len = boundaries.size
        while (idx < len) {
            val pos = boundaries[idx]
            val point = rc.geographicToCartesian( //获取到点的笛卡尔坐标系
                pos.latitude,
                pos.longitude,
                pos.altitude,
                WorldWind.ABSOLUTE,
                point
            )
            mx += point.x
            my += point.y
            mz += point.z
            numPoints++
            idx++
        }
        mx /= numPoints // 获取到所有点的笛卡尔坐标系然后得到平均值
        my /= numPoints
        mz /= numPoints
        // 获取该点的观察矩阵
        modelToTexCoord = rc.globe.cartesianToLocalTransform(mx, my, mz, modelToTexCoord)
        // 进行逆转置
        modelToTexCoord.invertOrthonormal()
    }
}