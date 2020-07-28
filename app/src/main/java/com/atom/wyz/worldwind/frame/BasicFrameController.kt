package com.atom.wyz.worldwind.frame

import android.opengl.GLES20
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.draw.Drawable
import com.atom.wyz.worldwind.draw.DrawableSurfaceColor
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.shader.BasicProgram
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.pool.Pool


open class BasicFrameController : FrameController {

    private var pickColor: Color = Color()

    override fun drawFrame(dc: DrawContext) {
        clearFrame(dc)
        drawDrawables(dc)
        if (dc.pickMode && dc.pickPoint != null) {
            resolvePick(dc)
        } else if (dc.pickMode) {
            this.resolvePickRect(dc)
        }
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

    protected fun resolvePick(dc: DrawContext) {
        val pickedObjects = dc.pickedObjects ?: return
        if (pickedObjects.count() == 0) {
            return
        }
        val let = dc.pickPoint?.let {
            pickColor = dc.readPixelColor(
                Math.round(it.x).toInt(),
                Math.round(it.y).toInt(),
                pickColor
            )
        }
        val topObjectId: Int = PickedObject.uniqueColorToIdentifier(pickColor)
        if (topObjectId != 0) {
            val terrainObject = pickedObjects.terrainPickedObject()
            val topObject = pickedObjects.pickedObjectWithId(topObjectId)
            if (topObject != null) {
                topObject.markOnTop()
                pickedObjects.clearPickedObjects()
                pickedObjects.offerPickedObject(topObject)
                pickedObjects.offerPickedObject(terrainObject)
            } else {
                pickedObjects.clearPickedObjects() // no eligible objects drawn at the pick point
            }
        } else {
            pickedObjects.clearPickedObjects()
        }
    }

    override fun renderFrame(rc: RenderContext) {
        rc.terrainTessellator?.tessellate(rc)
        if (rc.pickMode) {
            this.renderTerrainPickedObject(rc)
        }
        rc.layers?.render(rc)
        rc.sortDrawables()
    }

    protected fun renderTerrainPickedObject(rc: RenderContext) {
        val terrain = rc.terrain ?: return
        if (terrain.sector.isEmpty()) {
            return  // no terrain to pick
        }
        // Acquire a unique picked object ID for the terrain.
        val pickedObjectId = rc.nextPickedObjectId()
        // Enqueue a drawable for processing on the OpenGL thread that displays the terrain in the unique pick color.
        val pool: Pool<DrawableSurfaceColor> = rc.getDrawablePool(DrawableSurfaceColor::class.java)
        val drawable = DrawableSurfaceColor.obtain(pool)
        drawable.color = PickedObject.identifierToUniqueColor(pickedObjectId, drawable.color)
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }
        rc.offerSurfaceDrawable(drawable, Double.NEGATIVE_INFINITY)
        // If the pick ray intersects the terrain, enqueue a picked object that associates the terrain drawable with its
        // picked object ID and the intersection position.
        val pickPoint = Vec3()
        if (rc.pickRay != null && rc.terrain!!.intersect(rc.pickRay!!, pickPoint)) {
            rc.globe.cartesianToGeographic(pickPoint.x, pickPoint.y, pickPoint.z, Position()).let {
                it.altitude = 0.0
                rc.offerPickedObject(PickedObject.fromTerrain(pickedObjectId, it))
            }
        }
    }

    protected fun resolvePickRect(dc: DrawContext) {
        val pickedObjects = dc.pickedObjects ?: return
        if (pickedObjects.count() == 0) {
            return  // no eligible objects; avoid expensive calls to glReadPixels
        }
        val pickColors = dc.readPixelColors(
                dc.pickViewport!!.x,
                dc.pickViewport!!.y,
                dc.pickViewport!!.width,
                dc.pickViewport!!.height
            )
        for (pickColor in pickColors) {
            val topObjectId = PickedObject.uniqueColorToIdentifier(pickColor)
            if (topObjectId != 0) {
                val topObject = pickedObjects.pickedObjectWithId(topObjectId)
                topObject?.markOnTop()
            }
        }
        pickedObjects.keepTopObjects()
    }
}