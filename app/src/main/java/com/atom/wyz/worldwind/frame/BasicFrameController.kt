package com.atom.wyz.worldwind.frame

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Matrix4
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

    override fun renderFrame(rc: RenderContext) {
        createViewingState(rc)
        createTerrain(rc)
        renderLayers(rc)
    }

    protected fun renderLayers(rc: RenderContext) {
        val layers: LayerList = rc.layers
        for (layer in layers) {
            rc.currentLayer = layer
            try {
                rc.currentLayer?.render(rc)
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
        rc.currentLayer = null
    }

    protected fun createTerrain(rc: RenderContext) {
        rc.globe?.tessellator?.tessellate(rc)
    }

    protected fun createViewingState(rc: RenderContext) {

        val near = rc.eyePosition.altitude * 0.75
        val far = rc.globe!!.horizonDistance(rc.eyePosition.altitude, 160000.0)
        // 获取相机的动作
        this.camera.set(
            rc.eyePosition.latitude, rc.eyePosition.longitude, rc.eyePosition.altitude,
            WorldWind.ABSOLUTE, rc.heading, rc.tilt, rc.roll
        )
        // 相机和地球 变换为地球的笛卡尔变换矩阵 ，旋转 远近位移等操作
        rc.globe!!.cameraToCartesianTransform(camera, rc.modelview)!!.invertOrthonormal()
        // 通过该矩阵获取眼睛的笛卡尔位置
        rc.modelview.extractEyePoint(rc.eyePoint)
        // 创建投影空间
        rc.projection.setToPerspectiveProjection(
            rc.viewport.width().toDouble(),
            rc.viewport.height().toDouble(),
            rc.fieldOfView,
            near,
            far
        )
        // 将变换和投影空间合并
        rc.modelviewProjection.setToMultiply(rc.projection, rc.modelview)
        // 屏幕投影
        rc.screenProjection.setToScreenProjection(rc.viewport.width().toDouble(), rc.viewport.height().toDouble())
        // 投影空间设置
        rc.frustum.setToProjectionMatrix(rc.projection)
        rc.frustum.transformByMatrix(this.matrix.transposeMatrix(rc.modelview))
        rc.frustum.normalize()
    }


}