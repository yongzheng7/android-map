package com.atom.wyz.worldwind.render

import android.content.res.Resources
import android.graphics.Rect
import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Frustum
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.globe.Terrain
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.util.RenderResourceCache
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import java.util.*

/**
 * 绘画环境
 */
open class DrawContext {

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
            this.screenProjection.setToScreenProjection(value.width().toDouble(), value.height().toDouble())
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

    protected var userProperties: HashMap<Any, Any> = HashMap<Any, Any>()

    var surfaceTileRenderer: SurfaceTileRenderer? = null

    var programId = 0

    var textureUnit = GLES20.GL_TEXTURE0

    var textureId = IntArray(32)

    protected var orderedRenderables = OrderedRenderableQueue(1000)

    var screenProjection: Matrix4 = Matrix4()


    private var orderedRenderablesIndex = 0

    open fun pixelSizeAtDistance(distance: Double): Double {
        if (pixelSizeFactor == 0.0) { // cache the scaling factor used to convert distances to pixel sizes
            val fovyDegrees = fieldOfView
            val tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5))
            pixelSizeFactor = 2 * tanfovy_2 / viewport.height()
        }
        return distance * pixelSizeFactor
    }

    /**
     * Projects a Cartesian point to screen coordinates. The resultant screen point is in OpenGL screen coordinates,
     * with the origin in the bottom-left corner and axes that extend up and to the right from the origin.
     *
     *
     * This stores the projected point in the result argument, and returns a boolean value indicating whether or not the
     * projection is successful. This returns false if the Cartesian point is clipped by the near clipping plane or the
     * far clipping plane.
     *
     * 将笛卡尔点投影到屏幕坐标。
     * 最终的屏幕点在OpenGL屏幕坐标中，原点在左下角，轴从原点向上延伸到右侧。
     * 这会将投影点存储在result参数中，并返回一个布尔值，该布尔值指示投影是否成功。
     * 如果笛卡尔点被近裁剪平面或远裁剪平面裁剪，则返回false。
     * @param modelPoint the Cartesian point to project
     * @param result     a pre-allocated [Vec3] in which to return the projected point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    open fun project(modelPoint: Vec3?, result: Vec3?): Boolean {
        if (modelPoint == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "DrawContext", "project", "missingPoint"))
        }
        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "DrawContext", "project", "missingResult"))
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

    /**
     * Projects a Cartesian point to screen coordinates, applying an offset to the point's projected depth value. The
     * resultant screen point is in OpenGL screen coordinates, with the origin in the bottom-left corner and axes that
     * extend up and to the right from the origin.
     *
     *
     * This stores the projected point in the result argument, and returns a boolean value indicating whether or not the
     * projection is successful. This returns false if the Cartesian point is clipped by the near clipping plane or the
     * far clipping plane.
     *
     *
     * The depth offset may be any real number and is typically used to move the screenPoint slightly closer to the
     * user's eye in order to give it visual priority over nearby objects or terrain. An offset of zero has no effect.
     * An offset less than zero brings the screenPoint closer to the eye, while an offset greater than zero pushes the
     * projected screen point away from the eye.
     *
     *
     * Applying a non-zero depth offset has no effect on whether the model point is clipped by this method or by WebGL.
     * Clipping is performed on the original model point, ignoring the depth offset. The final depth value after
     * applying the offset is clamped to the range [0,1].
     * 将笛卡尔点投影到屏幕坐标，将偏移量应用于该点的投影深度值。
     * 最终的屏幕点在OpenGL屏幕坐标中，原点在左下角，轴从原点向上延伸到右侧。
     * 这会将投影点存储在result参数中，并返回一个布尔值，该布尔值指示投影是否成功。
     * 如果笛卡尔点被近裁剪平面或远裁剪平面裁剪，则返回false。
     * 深度偏移可以是任何实数，通常用于将screenPoint稍微移近用户的眼睛，以使其在视觉上优先于附近的对象或地形。
     * 零偏移量无效。小于零的偏移会使screenPoint靠近眼睛，而大于零的偏移会使投影的屏幕点远离眼睛。
     * 应用非零深度偏移对通过此方法还是通过WebGL剪切模型点没有影响。剪切是在原始模型点上执行的，忽略了深度偏移。
     * 应用偏移量后的最终深度值将被限制在[0,1]范围内。
     *
     * @param modelPoint  the Cartesian point to project
     * @param depthOffset the amount of depth offset to apply
     * @param result      a pre-allocated [Vec3] in which to return the projected point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    open fun projectWithDepth(modelPoint: Vec3?, depthOffset: Double, result: Vec3?): Boolean {
        if (modelPoint == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "DrawContext", "projectWithDepth", "missingPoint"))
        }
        if (result == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "DrawContext", "projectWithDepth", "missingResult"))
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

    open fun resetFrameProperties() {
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
        viewport.setEmpty()
        modelview.setToIdentity()
        screenProjection.setToIdentity()
        projection.setToIdentity()
        horizonDistance = 0.0

        modelviewProjection.setToIdentity()

        frustum.setToUnitFrustum()

        resources = null

        eyePoint.set(0.0, 0.0, 0.0)

        renderRequested = false
        userProperties.clear()

        renderResourceCache = null
        orderedRenderables.clearRenderables()

        pixelSizeFactor = 0.0

    }

    open fun getProgram(key: Any): GpuProgram? {
        return renderResourceCache?.let { it[key] as GpuProgram? }
    }

    open fun putProgram(key: Any, program: GpuProgram): GpuProgram? {
        renderResourceCache ?.put(key, program, program.programLength )
        return program
    }

    open fun currentProgram(): Int {
        return programId
    }
    open fun useProgram(programId: Int) {
        if (this.programId != programId) {
            this.programId = programId
            GLES20.glUseProgram(programId)
        }
    }

    open fun currentTextureUnit(): Int {
        return textureUnit
    }

    open fun activeTextureUnit(textureUnit: Int) {
        if (this.textureUnit != textureUnit) {
            this.textureUnit = textureUnit
            GLES20.glActiveTexture(textureUnit)
        }
    }
    open fun currentTexture(): Int {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        return textureId[textureUnitIndex]
    }
    open fun currentTexture(textureUnit: Int): Int {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        return textureId[textureUnitIndex]
    }

    open fun bindTexture(textureId: Int) {
        val textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0
        if (this.textureId[textureUnitIndex] != textureId) {
            this.textureId[textureUnitIndex] = textureId
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        }
    }

    open fun contextLost() {
        programId = 0
        textureUnit = GLES20.GL_TEXTURE0
        Arrays.fill(textureId, 0)
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
        return renderResourceCache ?.get(imageSource) as GpuTexture?
    }

    open fun putTexture(imageSource: ImageSource, texture: GpuTexture): GpuTexture? {
        renderResourceCache ?.put(imageSource, texture, texture.imageByteCount)
        return texture
    }

    open fun retrieveTexture(imageSource: ImageSource?): GpuTexture? {
        return renderResourceCache ?.retrieveTexture(imageSource)
    }

    open fun offerOrderedRenderable(renderable: OrderedRenderable?, eyeDistance: Double) {
        orderedRenderables.offerRenderable(renderable!!, eyeDistance)
    }

    open fun peekOrderedRenderble(): OrderedRenderable? {
        return orderedRenderables.peekRenderable()
    }

    open fun pollOrderedRenderable(): OrderedRenderable? {
        return orderedRenderables.pollRenderable()
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 垃圾箱




//    open fun pixelSizeAtDistance(distance: Double): Double {
//
//        return pixelSizeScale * distance + pixelSizeOffset
//    }


//    open fun setModelviewProjection(modelview: Matrix4?, projection: Matrix4?) {
//        if (modelview == null || projection == null) {
//            throw IllegalArgumentException(
//                    Logger.logMessage(Logger.ERROR, "DrawContext", "setModelview", "missingMatrix"))
//        }
//
//        this.modelview.set(modelview)
//        this.modelviewTranspose.transposeMatrix(modelview)
//
//        this.projection.set(projection)
//        this.projectionInv.invertMatrix(projection)
//
//        this.modelviewProjection.setToMultiply(projection, modelview)
//        this.modelviewProjectionInv.invertMatrix(modelviewProjection)
//
//        this.modelview.extractEyePoint(eyePoint)
//
//        this.frustum.setToProjectionMatrix(this.projection)
//        this.frustum.transformByMatrix(modelviewTranspose)
//        this.frustum.normalize()
//
//        val nbl = Vec3(-1.0, -1.0, -1.0)
//        val ntr = Vec3(+1.0, +1.0, -1.0)
//        val fbl = Vec3(-1.0, -1.0, +1.0)
//        val ftr = Vec3(+1.0, +1.0, +1.0)
//
//        nbl.multiplyByMatrix(projectionInv)
//        ntr.multiplyByMatrix(projectionInv)
//        fbl.multiplyByMatrix(projectionInv)
//        ftr.multiplyByMatrix(projectionInv)
//
//        val nrRectWidth: Double = Math.abs(ntr.x - nbl.x)
//        val frRectWidth: Double = Math.abs(ftr.x - fbl.x)
//        val nrDistance: Double = -nbl.z
//        val frDistance: Double = -fbl.z
//
//        val frustumWidthScale = (frRectWidth - nrRectWidth) / (frDistance - nrDistance)
//        val frustumWidthOffset = nrRectWidth - frustumWidthScale * nrDistance
//
//        pixelSizeScale = frustumWidthScale / viewport.width()
//        pixelSizeOffset = frustumWidthOffset / viewport.height()
//
//    }

}