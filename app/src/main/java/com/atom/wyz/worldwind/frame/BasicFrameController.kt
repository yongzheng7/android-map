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



    override fun drawFrame(dc: DrawContext) {
        clearFrame(dc)
        drawDrawables(dc)
    }

    protected fun clearFrame(dc: DrawContext) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    protected fun drawDrawables(dc: DrawContext) {
        dc.rewindDrawables()
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
        tessellateTerrain(rc)
        renderLayers(rc)
        prepareDrawables(rc)
    }

    protected fun prepareDrawables(rc: RenderContext) {
        rc.sortDrawables()
    }


    protected fun renderLayers(rc: RenderContext) {
        val layers: LayerList = rc.layers ?: return
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

    protected fun tessellateTerrain(rc: RenderContext) {
        rc.globe?.tessellator?.tessellate(rc)
    }


}