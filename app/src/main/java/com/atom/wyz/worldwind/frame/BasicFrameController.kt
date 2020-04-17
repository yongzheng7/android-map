package com.atom.wyz.worldwind.frame

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.globe.Tessellator
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.util.Logger


class BasicFrameController : FrameController {

    protected var camera: Camera = Camera()

    protected var matrix: Matrix4 = Matrix4()

    override fun drawFrame(dc: DrawContext) {
        clearFrame(dc)
        drawDrawables(dc)
    }

    protected fun clearFrame(dc: DrawContext) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    protected fun drawDrawables(dc: DrawContext) {
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
        createViewingState(dc)
        createTerrain(dc)
        renderLayers(dc)
    }

    protected fun renderLayers(dc: DrawContext) {
        val layers: LayerList = dc.layers
        for (layer in layers) {
            dc.currentLayer = layer
            try {
                dc.currentLayer?.render(dc)
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
        dc.globe?.tessellator?.tessellate(dc)
    }

    protected fun createViewingState(dc: DrawContext) {

        val near = dc.eyePosition.altitude * 0.75
        val far = dc.globe!!.horizonDistance(dc.eyePosition.altitude, 160000.0)
        // 获取相机的动作
        this.camera.set(
            dc.eyePosition.latitude, dc.eyePosition.longitude, dc.eyePosition.altitude,
            WorldWind.ABSOLUTE, dc.heading, dc.tilt, dc.roll
        )
        // 相机和地球 变换为地球的笛卡尔变换矩阵 ，旋转 远近位移等操作
        dc.globe!!.cameraToCartesianTransform(camera, dc.modelview)!!.invertOrthonormal()
        // 通过该矩阵获取眼睛的笛卡尔位置
        dc.modelview.extractEyePoint(dc.eyePoint)
        // 创建投影空间
        dc.projection.setToPerspectiveProjection(
            dc.viewport.width().toDouble(),
            dc.viewport.height().toDouble(),
            dc.fieldOfView,
            near,
            far
        )
        // 将变换和投影空间合并
        dc.modelviewProjection.setToMultiply(dc.projection, dc.modelview)
        // 屏幕投影
        dc.screenProjection.setToScreenProjection(dc.viewport.width().toDouble(), dc.viewport.height().toDouble())
        // 投影空间设置
        dc.frustum.setToProjectionMatrix(dc.projection)
        dc.frustum.transformByMatrix(this.matrix.transposeMatrix(dc.modelview))
        dc.frustum.normalize()
    }


}