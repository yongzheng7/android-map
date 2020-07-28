package com.atom.wyz.worldwind

import android.content.res.Resources
import android.graphics.Typeface
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.draw.DrawableTerrain
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.globe.Tessellator
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.pick.PickedObjectList
import com.atom.wyz.worldwind.render.*
import com.atom.wyz.worldwind.attribute.TextAttributes
import com.atom.wyz.worldwind.shader.BufferObject
import com.atom.wyz.worldwind.shader.GpuProgram
import com.atom.wyz.worldwind.shader.GpuTexture
import com.atom.wyz.worldwind.util.RenderResourceCache
import com.atom.wyz.worldwind.util.glu.GLU
import com.atom.wyz.worldwind.util.glu.GLUtessellator
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.SynchronizedPool
import kotlin.math.tan

/**
 * 绘画环境
 */
open class RenderContext {

    companion object {
        private const val MAX_PICKED_OBJECT_ID = 0xFFFFFF
    }

    lateinit var globe: Globe

    lateinit var resources: Resources

    var terrainTessellator: Tessellator? = null

    var terrain: Terrain? = null

    var layers: LayerList? = null

    var currentLayer: Layer? = null

    var verticalExaggeration = 1.0

    var fieldOfView = 0.0

    // 摄像机视线和地球相切，摄像机到切点的距离
    var horizonDistance = 0.0

    var camera = Camera()

    var cameraPoint = Vec3()

    var viewport = Viewport()
        set(value) {
            field.set(value)
        }

    var modelview: Matrix4 = Matrix4()

    var projection: Matrix4 = Matrix4()

    var modelviewProjection: Matrix4 = Matrix4()

    var frustum: Frustum = Frustum()

    var renderResourceCache: RenderResourceCache? = null

    var drawableQueue: DrawableQueue? = null

    var drawableTerrain: DrawableQueue? = null

    var redrawRequested = false

    var pickedObjects: PickedObjectList? = null

    var pickedObjectId = 0

    var pickMode = false

    var pickPoint: Vec2? = null

    var pickRay: Line? = null

    var pickViewport: Viewport? = null

    var pixelSizeFactor = 0.0

    var drawablePools = HashMap<Any, Pool<*>?>()

    var userProperties: HashMap<Any, Any> = HashMap()

    val textRenderer = TextRenderer()

    val scratchTextCacheKey = TextCacheKey()

    private var tessellator: GLUtessellator? = null

    val scratchVector = Vec3()

    open fun reset() {
        terrainTessellator = null
        terrain = null
        layers = null
        currentLayer = null
        verticalExaggeration = 1.0
        camera.set(
            0.0,
            0.0,
            0.0,
            WorldWind.ABSOLUTE /*lat, lon, alt*/,
            0.0,
            0.0,
            0.0  /*heading, tilt, roll*/
        )
        cameraPoint.set(0.0, 0.0, 0.0)
        fieldOfView = 0.0
        horizonDistance = 0.0

        viewport.setEmpty()
        modelview.setToIdentity()
        projection.setToIdentity()
        modelviewProjection.setToIdentity()
        frustum.setToUnitFrustum()

        renderResourceCache = null
        //resources = null

        drawableQueue = null
        drawableTerrain = null

        redrawRequested = false
        pickViewport = null
        pickedObjects = null
        pickPoint = null
        pickRay = null
        pickMode = false
        pixelSizeFactor = 0.0
        pickedObjectId = 0

        userProperties.clear()
    }

    open fun pixelSizeAtDistance(distance: Double): Double {
        if (pixelSizeFactor == 0.0) {
            // cache the scaling factor used to convert distances to pixel sizes
            val fovyDegrees = fieldOfView
            val tanfovy_2 = tan(Math.toRadians(fovyDegrees * 0.5))
            pixelSizeFactor = 2 * tanfovy_2 / viewport.height
        }
        return distance * pixelSizeFactor
    }

    //
    open fun project(modelPoint: Vec3, result: Vec3): Boolean {
        val mx: Double = modelPoint.x
        val my: Double = modelPoint.y
        val mz: Double = modelPoint.z
        val m = modelviewProjection.m
        var x = m[0] * mx + m[1] * my + m[2] * mz + m[3]
        var y = m[4] * mx + m[5] * my + m[6] * mz + m[7]
        var z = m[8] * mx + m[9] * my + m[10] * mz + m[11]
        val w = m[12] * mx + m[13] * my + m[14] * mz + m[15]
        if (w == 0.0) { // 判断是否是方向
            return false
        }
        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1]. 透视除法
        x /= w
        y /= w
        z /= w
        // Clip the point against the near and far clip planes.
        if (z < -1 || z > 1) {
            return false
        }
        // Convert the point from clip coordinate to the range [0,1]. This enables the X and Y coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range[0,1].
        x = x * 0.5 + 0.5
        y = y * 0.5 + 0.5
        z = z * 0.5 + 0.5
        // Convert the X and Y coordinates from the range [0,1] to screen coordinates.
        x = x * viewport.width + viewport.x
        y = y * viewport.height + viewport.y
        result.x = x
        result.y = y
        result.z = z
        return true
    }

    open fun projectWithDepth(modelPoint: Vec3, depthOffset: Double, result: Vec3): Boolean {
        val mx: Double = modelPoint.x
        val my: Double = modelPoint.y
        val mz: Double = modelPoint.z

        val m = modelview.m
        val ex = m[0] * mx + m[1] * my + m[2] * mz + m[3]
        val ey = m[4] * mx + m[5] * my + m[6] * mz + m[7]
        val ez = m[8] * mx + m[9] * my + m[10] * mz + m[11]
        val ew = m[12] * mx + m[13] * my + m[14] * mz + m[15]

        // Transform the point from eye coordinates to clip coordinates.
        val p = projection.m
        var x = p[0] * ex + p[1] * ey + p[2] * ez + p[3] * ew
        var y = p[4] * ex + p[5] * ey + p[6] * ez + p[7] * ew
        var z = p[8] * ex + p[9] * ey + p[10] * ez + p[11] * ew
        val w = p[12] * ex + p[13] * ey + p[14] * ez + p[15] * ew

        if (w == 0.0) {
            return false
        }
        // 透视除法
        x /= w
        y /= w
        z /= w
        // 判断z 的大小是否在 视锥体内
        if (z < -1 || z > 1) {
            return false
        }
        //  -1 < z < 1
        //  再次计算z的深度 此时加上偏移 depthOffset
        z = p[8] * ex + p[9] * ey + p[10] * ez * (1 + depthOffset) + p[11] * ew
        z /= w

        z = if (z < -1) (-1.0) else if (z > 1) 1.0 else z

        x = x * 0.5 + 0.5
        y = y * 0.5 + 0.5
        z = z * 0.5 + 0.5  // 换算到 0 - 1 范围内

        // 计算在屏幕上的位置
        x = x * viewport.width + viewport.x
        y = y * viewport.height + viewport.y

        result.x = x
        result.y = y
        result.z = z
        return true
    }


    open fun getProgram(key: Any): GpuProgram? {
        return renderResourceCache?.let { it.get(key) as GpuProgram? }
    }

    open fun putProgram(key: Any, program: GpuProgram): GpuProgram {
        renderResourceCache?.put(key, program, program.programLength)
        return program
    }

    open fun getTessellator(): GLUtessellator {
        tessellator?.let { return it }
        val tess: GLUtessellator = GLU.gluNewTess()
        return tess.also { tessellator = it }
    }

    open fun getUserProperty(key: Any?): Any? {
        return userProperties[key]
    }

    open fun putUserProperty(key: Any, value: Any): Any? {
        return userProperties.put(key, value)
    }

    open fun removeUserProperty(key: Any?): Any? {
        return userProperties.remove(key)
    }

    open fun hasUserProperty(key: Any?): Boolean {
        return userProperties.containsKey(key)
    }


    open fun getTexture(imageSource: ImageSource): GpuTexture? {
        return renderResourceCache?.get(imageSource) as GpuTexture?
    }

    open fun putTexture(imageSource: ImageSource, texture: GpuTexture): GpuTexture? {
        renderResourceCache?.put(imageSource, texture, texture.textureByteCount)
        return texture
    }

    open fun retrieveTexture(imageSource: ImageSource, imageOptions: ImageOptions?): GpuTexture? {
        return renderResourceCache?.retrieveTexture(imageSource, imageOptions)
    }

    open fun offerDrawable(drawable: Drawable, groupId: Int, depth: Double) {
        drawableQueue?.offerDrawable(drawable, groupId, depth)
    }

    open fun offerSurfaceDrawable(drawable: Drawable, zOrder: Double) {
        drawableQueue?.offerDrawable(drawable, WorldWind.SURFACE_DRAWABLE, zOrder)
    }

    open fun offerShapeDrawable(drawable: Drawable, cameraDistance: Double) {
        drawableQueue?.offerDrawable(
            drawable,
            WorldWind.SHAPE_DRAWABLE,
            -cameraDistance
        ) // order by descending eye distance
    }

    open fun offerScreenDrawable(drawable: Drawable, distance: Double) {
        drawableQueue?.offerDrawable(
            drawable,
            WorldWind.SCREEN_DRAWABLE,
            distance
        )
    }

    open fun sortDrawables() {
        drawableQueue?.sortDrawables()
        drawableTerrain?.sortDrawables()
    }

    open fun offerDrawableTerrain(drawable: DrawableTerrain, cameraDistance: Double) {
        drawableTerrain?.offerDrawable(drawable, WorldWind.SURFACE_DRAWABLE, cameraDistance)
    }


    open fun <T : Drawable> getDrawablePool(key: Class<T>): Pool<T> {
        var pool = drawablePools.get(key) as Pool<T>?
        if (pool == null) {
            pool =
                SynchronizedPool() // use SynchronizedPool; acquire and are release may be called in separate threads
            drawablePools.put(key, pool)
        }
        return pool
    }


    open fun drawableCount(): Int {
        return drawableQueue?.let { return it.count() } ?: 0
    }

    open fun offerPickedObject(pickedObject: PickedObject?) {
        pickedObjects?.offerPickedObject(pickedObject)
    }

    open fun nextPickedObjectId(): Int {
        pickedObjectId++
        if (pickedObjectId > MAX_PICKED_OBJECT_ID) {
            pickedObjectId = 1
        }
        return pickedObjectId
    }

    open fun getBufferObject(key: Any): BufferObject? {
        return renderResourceCache?.get(key) as BufferObject?
    }

    open fun putBufferObject(key: Any, buffer: BufferObject): BufferObject {
        renderResourceCache?.put(key, buffer, buffer.bufferByteCount)
        return buffer
    }

    open fun getText(text: String, attributes: TextAttributes): GpuTexture? {
        val key = scratchTextCacheKey.set(text, attributes)
        return renderResourceCache?.get(key) as GpuTexture?
    }

    open fun renderText(text: String, attributes: TextAttributes): GpuTexture? {
        val key = TextCacheKey().set(text, attributes)
        textRenderer.textColor = (attributes.textColor)
        textRenderer.textSize = (attributes.textSize)
        attributes.typeface?.also { textRenderer.typeface = it }
        textRenderer.enableOutline = (attributes.enableOutline)
        textRenderer.outlineColor = (attributes.outlineColor)
        textRenderer.outlineWidth = (attributes.outlineWidth)
        val texture: GpuTexture? = textRenderer.renderText(text)
        renderResourceCache?.put(key, texture!!, texture.textureByteCount)
        return texture
    }

    open fun geographicToCartesian(
        latitude: Double, longitude: Double, altitude: Double,
        @WorldWind.AltitudeMode altitudeMode: Int, result: Vec3
    ): Vec3 {
        when (altitudeMode) {
            WorldWind.ABSOLUTE -> {
                // 绝对坐标,经纬度高度直接获取笛卡尔坐标系的位置
                return globe.geographicToCartesian(
                    latitude,
                    longitude,
                    altitude * verticalExaggeration,
                    result
                )
            }
            WorldWind.CLAMP_TO_GROUND -> {
                if (terrain != null && terrain!!.surfacePoint(latitude, longitude, result)) {
                    return result // found a point on the terrain
                } else {
                    //获取地球表面的点
                    return globe.geographicToCartesian(latitude, longitude, 0.0, result)
                }
            }
            WorldWind.RELATIVE_TO_GROUND -> {
                if (terrain != null && terrain!!.surfacePoint(latitude, longitude, result)) {
                    if (altitude != 0.0) { // Offset along the normal vector at the terrain surface point.
                        globe.geographicToCartesianNormal(
                            latitude,
                            longitude,
                            scratchVector
                        )
                        result.x += scratchVector.x * altitude
                        result.y += scratchVector.y * altitude
                        result.z += scratchVector.z * altitude
                    }
                    return result // found a point relative to the terrain
                } else { // TODO use elevation model height as a fallback
                    return globe.geographicToCartesian(latitude, longitude, altitude, result)
                }
            }
        }
        return result
    }


    class TextCacheKey {

        var text: String? = null

        var textSize: Float = 0f

        var textColor: Color? = null

        var outlineColor: Color? = null

        var typeface: Typeface? = null

        var enableOutline: Boolean = false

        var outlineWidth: Float = 0f

        fun set(text: String, attributes: TextAttributes): TextCacheKey {
            this.text = text
            this.textColor = attributes.textColor
            this.outlineColor = attributes.outlineColor
            this.textSize = attributes.textSize
            this.typeface = attributes.typeface
            this.enableOutline = attributes.enableOutline
            this.outlineWidth = attributes.outlineWidth
            return this;
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || this.javaClass != o.javaClass) {
                return false
            }

            val that = o as TextCacheKey
            return ((if (text == null) that.text == null else text == that.text)
                    && textColor === that.textColor && textSize == that.textSize && (if (typeface == null) that.typeface == null else typeface == that.typeface)
                    && enableOutline == that.enableOutline && outlineColor === that.outlineColor && outlineWidth == that.outlineWidth)
        }

        override fun hashCode(): Int {
            var result = if (text != null) text.hashCode() else 0
            result = 31 * result + if (textColor != null) textColor.hashCode() else 0
            result =
                31 * result + if (textSize != +0.0f) java.lang.Float.floatToIntBits(textSize) else 0
            result = 31 * result + if (typeface != null) typeface.hashCode() else 0
            result = 31 * result + if (enableOutline) 1 else 0
            result =
                31 * result + if (outlineColor != null) outlineColor.hashCode() else 0
            result =
                31 * result + if (outlineWidth != +0.0f) java.lang.Float.floatToIntBits(outlineWidth) else 0
            return result
        }
    }

}