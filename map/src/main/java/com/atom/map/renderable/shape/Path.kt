package com.atom.map.renderable.shape

import android.opengl.GLES20
import com.atom.map.WorldWind
import com.atom.map.renderable.attribute.ShapeAttributes
import com.atom.map.renderable.RenderContext
import com.atom.map.drawable.DrawShapeState
import com.atom.map.drawable.Drawable
import com.atom.map.drawable.DrawableShape
import com.atom.map.drawable.DrawableSurfaceShape
import com.atom.map.geom.Location
import com.atom.map.geom.Matrix3
import com.atom.map.geom.Position
import com.atom.map.geom.Vec3
import com.atom.map.renderable.ImageOptions
import com.atom.map.core.shader.BasicProgram
import com.atom.map.core.shader.BufferObject
import com.atom.map.core.shader.GpuTexture
import com.atom.map.util.SimpleFloatArray
import com.atom.map.util.SimpleShortArray
import com.atom.map.util.pool.Pool
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Path : AbstractShape {
    companion object {
        private fun nextCacheKey() = Any()
        protected const val VERTEX_STRIDE = 4
        protected val defaultOutlineImageOptions: ImageOptions =
            ImageOptions()

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

    /**
     * 允许拉伸
     */
    var extrude = false
        get() = field
        set(value) {
            field = value
            reset()
        }

    /**
     * 经纬度在图块上
     */
    var followTerrain = false
        get() = field
        set(value) {
            field = value
            reset()
        }

    protected var vertexArray: SimpleFloatArray = SimpleFloatArray()

    /**
     * 顶点绘制索引
     */
    protected var outlineElements: SimpleShortArray = SimpleShortArray()

    /**
     * 向下拉伸的面
     */
    protected var interiorElements: SimpleShortArray = SimpleShortArray()

    /**
     * 向下拉伸的线
     */
    protected var verticalElements: SimpleShortArray = SimpleShortArray()

    protected var vertexBufferKey: Any =
        nextCacheKey()

    protected var elementBufferKey: Any =
        nextCacheKey()

    protected var vertexOrigin: Vec3 = Vec3()

    protected var texCoord1d = 0.0

    private val point = Vec3()

    private val prevPoint = Vec3()

    private val texCoordMatrix = Matrix3()

    private val intermediateLocation = Location()

    protected var isSurfaceShape = false

    constructor()

    constructor(attributes: ShapeAttributes) : super(attributes)

    constructor(positions: MutableList<Position>) {
        this.positions = positions
    }

    constructor(positions: MutableList<Position>, attributes: ShapeAttributes) : super(attributes) {
        this.positions = positions
    }

    /**
     * 顶点,顶点索引清除
     */
    override fun reset() {
        vertexArray.clear()
        interiorElements.clear()
        outlineElements.clear()
        verticalElements.clear()
    }

    /**
     * 制作线的 drawable
     */
    override fun makeDrawable(rc: RenderContext) {
        val shapeAttributes = activeAttributes ?: return
        if (positions.isEmpty()) {
            return  // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc)
            vertexBufferKey =
                nextCacheKey()
            elementBufferKey =
                nextCacheKey()
        }
        // Obtain a drawable form the render context pool, and compute distance to the render camera.
        val drawable: Drawable
        val drawState: DrawShapeState
        val cameraDistance: Double

        if (isSurfaceShape) { // 经纬度绘制
            val pool: Pool<DrawableSurfaceShape> = rc.getDrawablePool(
                DrawableSurfaceShape::class.java)
            drawable = DrawableSurfaceShape.obtain(pool)
            drawState = drawable.drawState
            cameraDistance = cameraDistanceGeographic(rc, boundingSector)
            drawable.sector.set(boundingSector)
        } else { // 笛卡尔绘制
            val pool: Pool<DrawableShape> = rc.getDrawablePool(
                DrawableShape::class.java)
            drawable = DrawableShape.obtain(pool)
            drawState = drawable.drawState
            cameraDistance = cameraDistanceCartesian(rc, vertexArray.array(), vertexArray.size(),
                VERTEX_STRIDE, vertexOrigin)
        }

        drawState.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawState.program == null) {
            drawState.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }
        // 初始化顶点缓冲
        drawState.vertexBuffer = rc.getBufferObject(vertexBufferKey)
        if (drawState.vertexBuffer == null) {
            val size = vertexArray.size() * 4
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
            buffer.put(vertexArray.array(), 0, vertexArray.size())
            drawState.vertexBuffer = rc.putBufferObject(vertexBufferKey,
                BufferObject(
                    GLES20.GL_ARRAY_BUFFER,
                    size,
                    buffer.rewind()
                )
            )
        }
        // 初始化顶点索引缓冲全部
        drawState.elementBuffer = rc.getBufferObject(elementBufferKey)
        if (drawState.elementBuffer == null) {
            val size = interiorElements.size() * 2 + outlineElements.size() * 2 + verticalElements.size() * 2
            val buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer()
            buffer.put(interiorElements.array(), 0, interiorElements.size())
            buffer.put(outlineElements.array(), 0, outlineElements.size())
            buffer.put(verticalElements.array(), 0, verticalElements.size())
            val bufferObject = BufferObject(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                size,
                buffer.rewind()
            )
            drawState.elementBuffer = rc.putBufferObject(elementBufferKey, bufferObject)
        }

        drawState.texCoordAttrib.size = 1 /*size*/
        drawState.texCoordAttrib.offset = 12 /*offset*/
        // 配置可绘制对象以在绘制轮廓时使用轮廓纹理。
        if (shapeAttributes.drawOutline && shapeAttributes.outlineImageSource != null) {
            var texture: GpuTexture? = rc.getTexture(shapeAttributes.outlineImageSource!!)
            if (texture == null) {
                texture = rc.retrieveTexture(shapeAttributes.outlineImageSource!!,
                    defaultOutlineImageOptions
                )
            }
            if (texture != null) {
                val metersPerPixel = rc.pixelSizeAtDistance(cameraDistance)
                computeRepeatingTexCoordTransform(texture, metersPerPixel, texCoordMatrix)
                drawState.texture = (texture)
                drawState.texCoordMatrix = (texCoordMatrix)
            }
        }
        //绘制线
        if (shapeAttributes.drawOutline) {
            drawState.color(if (rc.pickMode) pickColor else shapeAttributes.outlineColor)
            drawState.lineWidth(if (isSurfaceShape) shapeAttributes.outlineWidth + 0.5f else shapeAttributes.outlineWidth)
            drawState.drawElements(GLES20.GL_LINE_STRIP, outlineElements.size(), GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2)
        }
        // Disable texturing for the remaining drawable primitives.
        drawState.texture = null

        //绘制拉伸线
        if (shapeAttributes.drawOutline && shapeAttributes.drawVerticals && extrude) {
            drawState.color(if (rc.pickMode) pickColor else shapeAttributes.outlineColor)
            drawState.lineWidth(shapeAttributes.outlineWidth)
            drawState.drawElements(GLES20.GL_LINES, verticalElements.size(), GLES20.GL_UNSIGNED_SHORT, interiorElements.size() * 2 + outlineElements.size() * 2)
        }
        //绘制拉伸面
        if (shapeAttributes.drawInterior && extrude) {
            drawState.color(if (rc.pickMode) pickColor else shapeAttributes.interiorColor)
            drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, interiorElements.size(), GLES20.GL_UNSIGNED_SHORT, 0)
        }

        drawState.vertexOrigin.set(vertexOrigin)
        drawState.vertexStride = VERTEX_STRIDE * 4
        drawState.enableCullFace = false // 不允许背部裁剪
        drawState.enableDepthTest = shapeAttributes.depthTest // 深度测试

        if (isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0.0 /*zOrder*/)
        } else {
            rc.offerShapeDrawable(drawable, cameraDistance)
        }
    }

    /**
     * 是否需要组装
     */
    protected fun mustAssembleGeometry(rc: RenderContext): Boolean {
        return vertexArray.size() == 0
    }

    /**
     * 组装线的顶点和顶点索引
     */
    protected fun assembleGeometry(rc: RenderContext) {
        // 确定形状几何必须组装为笛卡尔几何还是地理几何。
        isSurfaceShape = altitudeMode == WorldWind.CLAMP_TO_GROUND && followTerrain
        // 初始化
        reset()
        // 计算路径的本地笛卡尔坐标原点并添加第一个顶点。
        // 取出点1
        var begin = positions[0]
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, false /*tessellated*/)
        // 添加剩余的路径顶点，按照路径属性指示的方式细分每个线段。
        var idx = 1
        val len = positions.size
        while (idx < len) {
            val end = positions[idx]
            this.addIntermediateVertices(rc, begin, end)
            addVertex(rc, end.latitude, end.longitude, end.altitude, false /*tessellated*/)
            begin = end
            idx++
        }
        // 根据其组合坐标计算路径的边界框或边界扇区。
        if (isSurfaceShape) { // 如果是经纬度 计算出该线所占的sector区域 最后再进行位移到真实的位置
            boundingSector.setEmpty()
            boundingSector.union(vertexArray.array(), vertexArray.size(),
                VERTEX_STRIDE
            )
            boundingSector.translate(vertexOrigin.y /*latitude*/, vertexOrigin.x /*longitude*/)
            boundingBox.setToUnitBox() // Surface/geographic shape bounding box is unused
        } else {
            boundingBox.setToPoints(vertexArray.array(), vertexArray.size(),
                VERTEX_STRIDE
            )
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
        val vertex: Int = vertexArray.size() / VERTEX_STRIDE
        var point = rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, point)

        if (vertex == 0) { // 起点
            if (isSurfaceShape) {
                // 起点设置为经纬度
                vertexOrigin.set(longitude, latitude, altitude)
            } else {
                // 起点设置为笛卡尔
                vertexOrigin.set(point)
            }
            texCoord1d = 0.0 // 距离
            prevPoint.set(point) // 上个点
        } else {
            texCoord1d += point.distanceTo(prevPoint) // 累计距离
            prevPoint.set(point) // 设置上个点
        }

        if (isSurfaceShape) { // 经纬度就不能加上下摆的拉伸
            vertexArray.add((longitude - vertexOrigin.x).toFloat()) // 设置点的和起点的差值 x
            vertexArray.add((latitude - vertexOrigin.y).toFloat())  // 设置点的和起点的差值 y
            vertexArray.add((altitude - vertexOrigin.z).toFloat())  // 设置点的和起点的差值 z
            vertexArray.add(texCoord1d.toFloat()) // 距离设置进去
            outlineElements.add(vertex.toShort()) // 设置点索引
        } else {
            vertexArray.add((point.x - vertexOrigin.x).toFloat())
            vertexArray.add((point.y - vertexOrigin.y).toFloat())
            vertexArray.add((point.z - vertexOrigin.z).toFloat())
            vertexArray.add(texCoord1d.toFloat())
            outlineElements.add(vertex.toShort())

            if (extrude) { // 拉伸是否需要拉伸
                point = rc.geographicToCartesian(latitude, longitude, 0.0, altitudeMode, this.point) // 改点的下摆点
                vertexArray.add((point.x - vertexOrigin.x).toFloat())
                vertexArray.add((point.y - vertexOrigin.y).toFloat())
                vertexArray.add((point.z - vertexOrigin.z).toFloat())
                vertexArray.add(0.toFloat() /*unused*/)

                interiorElements.add(vertex.toShort())
                interiorElements.add((vertex + 1).toShort())
            }
            if (extrude && !intermediate) { // 又需要拉伸, 又需要 拉伸线 红线
                verticalElements.add(vertex.toShort())
                verticalElements.add((vertex + 1).toShort())
            }
        }
    }
}