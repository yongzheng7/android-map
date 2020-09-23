package com.atom.wyz.worldwind.layer.render

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.layer.render.attribute.ShapeAttributes
import com.atom.wyz.worldwind.layer.draw.DrawableSightline
import com.atom.wyz.worldwind.geom.BoundingSphere
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.render.pick.PickedObject
import com.atom.wyz.worldwind.core.shader.SensorProgram
import com.atom.wyz.worldwind.layer.render.shape.Attributable
import com.atom.wyz.worldwind.layer.render.shape.Highlightable
import com.atom.wyz.worldwind.layer.render.shape.Movable
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.pool.Pool

class OmnidirectionalSightline : AbstractRenderable,
    Attributable,
    Highlightable,
    Movable {

    var position = Position()

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE

    var range = 0f

    override var attributes: ShapeAttributes? = null

    override var highlightAttributes: ShapeAttributes? = null

    override var highlighted: Boolean = false

    var occludeAttributes: ShapeAttributes

    var activeAttributes: ShapeAttributes? = null

    val centerPoint: Vec3 = Vec3()

    val scratchPoint: Vec3 = Vec3()

    var pickedObjectId = 0

    val pickColor = SimpleColor()

    val boundingSphere: BoundingSphere = BoundingSphere()


    constructor(position: Position, range: Float) {
        this.position.set(position)
        this.range = range
        attributes =
            ShapeAttributes()
        occludeAttributes =
            ShapeAttributes()
        occludeAttributes.interiorColor = (SimpleColor(1f, 0f, 0f, 1f)) // red
    }

    constructor(position: Position, range: Float, attributes: ShapeAttributes) {
        this.position.set(position)
        this.range = range
        this.attributes = attributes
        occludeAttributes =
            ShapeAttributes()
        occludeAttributes.interiorColor = (SimpleColor(1f, 0f, 0f, 1f)) // red
    }

    override fun doRender(rc: RenderContext) {
        Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "doRender_65" ," 1"))

        if (!this.determineCenterPoint(rc)) {
            return
        }
        Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "doRender_70" ," 2 "))

        // Don't render anything if the sensor's coverage area is not visible.
        if (!this.isVisible(rc)) {
            return
        }
        Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "doRender_76" ," 3"))

        // Select the currently active attributes.
        this.determineActiveAttributes(rc)

        // Configure the pick color when rendering in pick mode.
        if (rc.pickMode) {
            pickedObjectId = rc.nextPickedObjectId()
            pickColor.set(PickedObject.identifierToUniqueColor(pickedObjectId, pickColor))
        }
        // Enqueue drawables for processing on the OpenGL thread.
        this.makeDrawable(rc)
        // Enqueue a picked object that associates the sensor's drawables with its picked object ID.
        if (rc.pickMode) {
            rc.offerPickedObject(
                PickedObject.fromRenderable(
                    pickedObjectId,
                    this,
                    rc.currentLayer!!
                )
            )
        }
    }

    protected fun determineCenterPoint(rc: RenderContext): Boolean {
        val lat = position.latitude
        val lon = position.longitude
        val alt = position.altitude
        when (altitudeMode) {
            WorldWind.ABSOLUTE -> rc.globe.geographicToCartesian(lat, lon, alt * rc.verticalExaggeration, centerPoint)
            WorldWind.CLAMP_TO_GROUND -> if (rc.terrain != null && rc.terrain!!.surfacePoint(lat, lon, scratchPoint)) {
                centerPoint.set(scratchPoint) // found a point on the terrain
            }
            WorldWind.RELATIVE_TO_GROUND -> if (rc.terrain != null && rc.terrain!!.surfacePoint(lat, lon, scratchPoint)) {
                centerPoint.set(scratchPoint) // found a point on the terrain
                if (alt != 0.0) { // Offset along the normal vector at the terrain surface point.
                    val scratchVector = Vec3()
                    rc.globe.geographicToCartesianNormal(lat, lon, scratchVector)
                    centerPoint.x += scratchVector.x * alt
                    centerPoint.y += scratchVector.y * alt
                    centerPoint.z += scratchVector.z * alt
                }
            }
        }
        return centerPoint.x != 0.0 && centerPoint.y != 0.0 && centerPoint.z != 0.0
    }

    protected fun isVisible(rc: RenderContext): Boolean {
        val cameraDistance = centerPoint.distanceTo(rc.cameraPoint)
        val pixelSizeMeters = rc.pixelSizeAtDistance(cameraDistance)

        return if (range < pixelSizeMeters) {
            false // The range is zero, or is less than one screen pixel
        } else boundingSphere.set(centerPoint, range).intersectsFrustum(rc.frustum)

    }

    protected fun determineActiveAttributes(rc: RenderContext?) {
        if (highlighted && highlightAttributes != null) {
            activeAttributes = highlightAttributes
        } else {
            activeAttributes = attributes
        }
    }

    protected fun makeDrawable(rc: RenderContext) {
        val shapeAttributes = activeAttributes ?: return
        val pool: Pool<DrawableSightline> = rc.getDrawablePool(
            DrawableSightline::class.java)
        val drawable: DrawableSightline = DrawableSightline.obtain(pool)

        // Compute the transform from sensor local coordinates to world coordinates.
       rc.globe.cartesianToLocalTransform(
            centerPoint.x,
            centerPoint.y,
            centerPoint.z,
            drawable.centerTransform
        )
        drawable.range = WWMath.clamp(range.toDouble(), 0.0, Double.MAX_VALUE).toFloat()
        // Configure the drawable colors according to the current attributes. When picking use a unique color associated
        // with the picked object ID. Null attributes indicate that nothing is drawn.
        drawable.visibleColor.set(if (rc.pickMode) pickColor else shapeAttributes.interiorColor)
        drawable.occludedColor.set(if (rc.pickMode) pickColor else occludeAttributes.interiorColor)
        // Use the sensor GLSL program to draw the sensor's coverage.
        drawable.program = rc.getProgram(SensorProgram.KEY) as SensorProgram?
        if (drawable.program == null) {
            Logger.log(Logger.ERROR , Logger.makeMessage("DrawableSightline" , "makeDrawable_158" ," drawable.program == null"))
            drawable.program = rc.putProgram(
                SensorProgram.KEY,
                SensorProgram(rc.resources)
            ) as SensorProgram
        }
        // Enqueue a drawable for processing on the OpenGL thread.
        rc.offerSurfaceDrawable(drawable, 0.0 /*z-order*/)
    }

    override fun getReferencePosition(): Position? {
        return this.position
    }

    override fun moveTo(globe: Globe, position: Position?) {
        this.position.set(position)
    }

}