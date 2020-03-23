package com.atom.wyz.worldwind

import android.opengl.GLES20
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.globe.Tessellator
import com.atom.wyz.worldwind.render.DrawContext
import com.atom.wyz.worldwind.render.OrderedRenderable
import com.atom.wyz.worldwind.util.Logger


class BasicFrameController(override var frameStatistics: FrameStatistics = FrameStatistics()) : FrameController {

    protected var camera: Camera = Camera()

    protected var matrix: Matrix4 = Matrix4()

    override fun drawFrame(dc: DrawContext) {
        try {
            frameStatistics.beginFrame()
            this.doDrawFrame(dc)
        } finally {
            frameStatistics.endFrame()
        }
    }

    protected fun doDrawFrame(dc: DrawContext) {
        this.clearFrame(dc)
        this.createViewingState(dc)
        this.createTerrain(dc)
        this.drawLayers(dc)
        this.drawOrderedRenderables(dc)
    }

    protected fun createTerrain(dc: DrawContext) {
        val tess: Tessellator? = dc.globe?.tessellator
        dc.terrain = tess?.tessellate(dc)
    }

    protected fun createViewingState(dc: DrawContext) {

        val near = dc.eyePosition.altitude * 0.75
        val far = dc.globe!!.horizonDistance(dc.eyePosition.altitude, 160000.0)

        this.camera.set(dc.eyePosition.latitude, dc.eyePosition.longitude, dc.eyePosition.altitude, WorldWind.ABSOLUTE, dc.heading, dc.tilt, dc.roll)

        dc.globe!!.cameraToCartesianTransform(camera, dc.modelview)!!.invertOrthonormal()

        dc.modelview.extractEyePoint(dc.eyePoint)

        dc.projection.setToPerspectiveProjection(dc.viewport.width().toDouble(), dc.viewport.height().toDouble(), dc.fieldOfView, near, far)

        dc.modelviewProjection.setToMultiply(dc.projection, dc.modelview)

        dc.screenProjection.setToScreenProjection(dc.viewport.width().toDouble(), dc.viewport.height().toDouble())

        dc.frustum.setToProjectionMatrix(dc.projection)
        dc.frustum.transformByMatrix(this.matrix.transposeMatrix(dc.modelview))
        dc.frustum.normalize()
    }

    protected fun clearFrame(dc: DrawContext) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    protected fun drawLayers(dc: DrawContext) {
        for (layer in dc.layers) {
            dc.currentLayer = layer
            try {
                layer.render(dc)
            } catch (e: Exception) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawLayers",
                        "Exception while rendering layer  \'" + layer.displayName + "\'", e)
                // Keep going. Draw the remaining layers.
            }
        }
        dc.currentLayer = null
    }

    protected fun drawOrderedRenderables(dc: DrawContext) {
        var or: OrderedRenderable? = null
        while (dc.pollOrderedRenderable().also({ or = it ?: return }) != null) {
            try {
                or?.renderOrdered(dc)
            } catch (e: java.lang.Exception) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawOrderedRenderables",
                        "Exception while rendering ordered renderable \'$or\'", e)
                // Keep going. Draw the remaining ordered renderables.
            }
        }
    }

    fun dd(){


    }
}