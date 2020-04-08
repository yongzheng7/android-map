package com.atom.wyz.worldwind.frame

import android.opengl.GLES20
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.globe.Tessellator
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.render.OrderedRenderable
import com.atom.wyz.worldwind.util.Logger


class BasicFrameController: FrameController {

    protected var camera: Camera = Camera()

    protected var matrix: Matrix4 = Matrix4()

    override fun drawFrame(dc: DrawContext) {
        dc.sortDrawables()
        var next: Drawable?
        while (dc.pollDrawable().also { next = it } != null) {
            try {
                next?.draw(dc)
            } catch (e: java.lang.Exception) {
                Logger.logMessage(
                    Logger.ERROR, "BasicFrameController", "drawDrawables",
                    "Exception while drawing \'$next\'"
                )
            }
        }
    }

    override fun renderFrame(dc: DrawContext) {
        clearFrame(dc)
        createViewingState(dc)
        createTerrain(dc)
        renderLayers(dc)
    }
    protected fun clearFrame(dc: DrawContext) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }
    protected fun renderLayers(dc: DrawContext) {
        for (layer in dc.layers) {
            dc.currentLayer = layer
            try {
                layer.render(dc)
            } catch (e: java.lang.Exception) {
                Logger.logMessage(
                    Logger.ERROR,
                    "BasicFrameController",
                    "drawLayers",
                    "Exception while rendering layer \'" + layer.displayName + "\'",
                    e
                )
            }
        }
        dc.currentLayer = null
    }
    protected fun createTerrain(dc: DrawContext) {
        val tess: Tessellator? = dc.globe?.tessellator
        dc.terrain = tess?.tessellate(dc)
    }
    protected fun createViewingState(dc: DrawContext) {

        val near = dc.eyePosition.altitude * 0.75
        val far = dc.globe!!.horizonDistance(dc.eyePosition.altitude, 160000.0)

        this.camera.set(dc.eyePosition.latitude, dc.eyePosition.longitude, dc.eyePosition.altitude,
            WorldWind.ABSOLUTE, dc.heading, dc.tilt, dc.roll)

        dc.globe!!.cameraToCartesianTransform(camera, dc.modelview)!!.invertOrthonormal()

        dc.modelview.extractEyePoint(dc.eyePoint)

        dc.projection.setToPerspectiveProjection(dc.viewport.width().toDouble(), dc.viewport.height().toDouble(), dc.fieldOfView, near, far)

        dc.modelviewProjection.setToMultiply(dc.projection, dc.modelview)

        dc.screenProjection.setToScreenProjection(dc.viewport.width().toDouble(), dc.viewport.height().toDouble())

        dc.frustum.setToProjectionMatrix(dc.projection)
        dc.frustum.transformByMatrix(this.matrix.transposeMatrix(dc.modelview))
        dc.frustum.normalize()
    }


}