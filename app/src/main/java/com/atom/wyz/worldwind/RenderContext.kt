package com.atom.wyz.worldwind

import android.content.res.Resources
import android.graphics.Rect
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableList
import com.atom.wyz.worldwind.draw.DrawableQueue
import com.atom.wyz.worldwind.draw.DrawableTerrain
import com.atom.wyz.worldwind.geom.Frustum
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.render.GpuProgram
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.RenderResourceCache
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.pool.Pool
import com.atom.wyz.worldwind.util.pool.SynchronizedPool

/**
 * 绘画环境
 */
open class RenderContext {

    var resources: Resources? = null

    var globe: Globe? = null

    var terrain: Terrain? = null

    var layers = LayerList()
        set(value) {
            field.clearLayers()
            field.addAllLayers(value)
        }

    var currentLayer: Layer? = null

    var verticalExaggeration = 1.0

    var viewport = Rect()
        set(value) {
            field.set(value)
        }

    var modelview: Matrix4 = Matrix4()

    var horizonDistance = 0.0

    var projection: Matrix4 = Matrix4()

    var modelviewProjection: Matrix4 = Matrix4()

    var eyePoint: Vec3 = Vec3()

    var frustum: Frustum = Frustum()

    var pixelSizeFactor = 0.0

    var eyePosition: Position = Position()
        set(value) {
            field.set(value)
        }
    /**
     * 眼睛正方向 和 N 的角度
     */
    var heading = 0.0
    /**
     * 眼睛的倾斜度
     */
    var tilt = 0.0

    var roll = 0.0

    var fieldOfView = 0.0

    var pickingMode = false

    var renderRequested = false

    var renderResourceCache: RenderResourceCache? = null

    protected var drawablePools = HashMap<Any, Pool<*>?>()

    private var userProperties: HashMap<Any, Any> = HashMap<Any, Any>()

    var drawableQueue: DrawableQueue? = null

    var drawableTerrain: DrawableList? = null

    open fun reset() {
        pickingMode = false
        globe = null
        terrain = null
        layers.clearLayers()
        currentLayer = null
        verticalExaggeration = 1.0
        eyePosition.set(0.0, 0.0, 0.0)
        heading = 0.0
        tilt = 0.0
        roll = 0.0
        fieldOfView = 0.0
        horizonDistance = 0.0
        viewport.setEmpty()
        modelview.setToIdentity()
        projection.setToIdentity()
        modelviewProjection.setToIdentity()
        eyePoint.set(0.0, 0.0, 0.0)
        frustum.setToUnitFrustum()
        renderResourceCache = null
        resources = null
        renderRequested = false
        pixelSizeFactor = 0.0
        drawableQueue = null
        drawableTerrain = null
        userProperties.clear()
    }

    open fun pixelSizeAtDistance(distance: Double): Double {
        if (pixelSizeFactor == 0.0) { // cache the scaling factor used to convert distances to pixel sizes
            val fovyDegrees = fieldOfView
            val tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5))
            pixelSizeFactor = 2 * tanfovy_2 / viewport.height()
        }
        return distance * pixelSizeFactor
    }

    open fun project(modelPoint: Vec3?, result: Vec3?): Boolean {
        if (modelPoint == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "project", "missingPoint")
            )
        }
        if (result == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "project", "missingResult")
            )
        }
        // Transform the model point from model coordinates to eye coordinates then to clip coordinates. This
        // inverts the Z axis and stores the negative of the eye coordinate Z value in the W coordinate.
        val mx: Double = modelPoint.x
        val my: Double = modelPoint.y
        val mz: Double = modelPoint.z
        val m = modelviewProjection.m
        var x = m[0] * mx + m[1] * my + m[2] * mz + m[3]
        var y = m[4] * mx + m[5] * my + m[6] * mz + m[7]
        var z = m[8] * mx + m[9] * my + m[10] * mz + m[11]
        val w = m[12] * mx + m[13] * my + m[14] * mz + m[15]
        if (w == 0.0) {
            return false
        }
        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
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
        x = x * viewport.width() + viewport.left
        y = y * viewport.height() + viewport.top
        result.x = x
        result.y = y
        result.z = z
        return true
    }

    open fun projectWithDepth(modelPoint: Vec3?, depthOffset: Double, result: Vec3?): Boolean {
        if (modelPoint == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "projectWithDepth", "missingPoint")
            )
        }
        if (result == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "projectWithDepth", "missingResult")
            )
        }

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
        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
        x /= w
        y /= w
        z /= w
        // Clip the point against the near and far clip planes.
        if (z < -1 || z > 1) {
            return false
        }

        z = p[8] * ex + p[9] * ey + p[10] * ez * (1 + depthOffset) + p[11] * ew
        z /= w

        z = WWMath.clamp(z, -1.0, 1.0)


        x = x * 0.5 + 0.5
        y = y * 0.5 + 0.5
        z = z * 0.5 + 0.5

        x = x * viewport.width() + viewport.left
        y = y * viewport.height() + viewport.top

        result.x = x
        result.y = y
        result.z = z
        return true
    }


    open fun getProgram(key: Any): GpuProgram? {
        return renderResourceCache?.let { it[key] as GpuProgram? }
    }

    open fun putProgram(key: Any, program: GpuProgram): GpuProgram {
        renderResourceCache?.put(key, program, program.programLength)
        return program
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
        renderResourceCache?.put(imageSource, texture, texture.imageByteCount)
        return texture
    }

    open fun retrieveTexture(imageSource: ImageSource?): GpuTexture? {
        return renderResourceCache?.retrieveTexture(imageSource)
    }

    open fun offerDrawable(drawable: Drawable, groupId: Int, depth: Double) {
        drawableQueue?.offerDrawable(drawable, groupId, depth)
    }

    open fun offerSurfaceDrawable(drawable: Drawable, zOrder: Double) {
        drawableQueue?.offerDrawable(drawable, WorldWind.SURFACE_DRAWABLE, zOrder)
    }

    open fun offerShapeDrawable(drawable: Drawable, eyeDistance: Double) {
        drawableQueue?.offerDrawable(
            drawable,
            WorldWind.SHAPE_DRAWABLE,
            -eyeDistance
        ) // order by descending eye distance
    }
    open fun sortDrawables() {
        drawableQueue?.sortDrawables()
    }

    open fun offerDrawableTerrain(drawable: DrawableTerrain?) {
        drawableTerrain?.offerDrawable(drawable)
    }


    open fun <T : Drawable> getDrawablePool(key: Class<T>): Pool<T> {
        var pool = drawablePools.get(key) as Pool<T>?
        if (pool == null) {
            pool = SynchronizedPool() // use SynchronizedPool; acquire and are release may be called in separate threads
            drawablePools.put(key, pool)
        }
        return pool
    }


}