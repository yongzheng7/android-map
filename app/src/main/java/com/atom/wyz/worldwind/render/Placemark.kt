package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawableLines
import com.atom.wyz.worldwind.draw.DrawableScreenTexture
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.shape.Highlightable
import com.atom.wyz.worldwind.shape.Movable
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.pool.Pool

open class Placemark : AbstractRenderable, Highlightable, Movable {

    interface LevelOfDetailSelector {
        fun selectLevelOfDetail(rc: RenderContext, placemark: Placemark?, cameraDistance: Double)
    }

    companion object {

        var DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6

        var DEFAULT_DEPTH_OFFSET = -0.003

        fun createSimple(position: Position, color: Color?, pixelSize: Int): Placemark {
            val defaults = PlacemarkAttributes.defaults()
            defaults.imageColor = color
            defaults.imageScale = pixelSize.toDouble()
            return Placemark(position, defaults)
        }

        fun createSimpleImage(position: Position, imageSource: ImageSource?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImage(imageSource))
        }

        fun createSimpleImageAndLabel(position: Position, imageSource: ImageSource?, label: String?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImage(imageSource), label)
        }

        private var placePoint = Vec3()
        private val screenPlacePoint = Vec3()
        private var groundPoint = Vec3()
        private val offset: Vec2 = Vec2()
        private val screenBounds = Viewport()
        private val unitSquareTransform: Matrix4 = Matrix4()
    }

    var position: Position?

    var cameraDistance = 0.0

    var levelOfDetailSelector: LevelOfDetailSelector? = null

    var attributes: PlacemarkAttributes
    var _highlighted = false
    var highlightAttributes: PlacemarkAttributes? = null
    var activeAttributes: PlacemarkAttributes? = null

    var activeTexture: GpuTexture? = null

    var eyeDistanceScaling: Boolean
    var eyeDistanceScalingThreshold: Double
    var eyeDistanceScalingLabelThreshold: Double

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE

    var enableLeaderPicking: Boolean = false

    var imageRotation = 0.0
    var imageTilt = 0.0


    @WorldWind.OrientationMode
    var imageRotationReference = 0

    @WorldWind.OrientationMode
    var imageTiltReference = 0

    /**
     * The picked object ID associated with the placemark during the current render pass.
     */
    protected var pickedObjectId = 0

    protected var pickColor = Color()


    constructor(position: Position) : this(position, PlacemarkAttributes.defaults())

    constructor(position: Position, attributes: PlacemarkAttributes) : this(position, attributes, null)

    constructor(position: Position?, attributes: PlacemarkAttributes?, displayName: String?) {
        if (position == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Placemark", "constructor", "missingPosition")
            )
        }
        this.position = position
        this.altitudeMode = WorldWind.ABSOLUTE
        displayName?.let { this.displayName = it } ?: let { this.displayName = "Placemark" }
        this.attributes = if (attributes != null) attributes else PlacemarkAttributes()


        this.eyeDistanceScaling = false
        eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD
        eyeDistanceScalingLabelThreshold = 1.5 * eyeDistanceScalingThreshold

        imageRotationReference = WorldWind.RELATIVE_TO_SCREEN
        imageTiltReference = WorldWind.RELATIVE_TO_SCREEN

    }

    protected open fun determineActiveAttributes(rc: RenderContext) {
        if (_highlighted && highlightAttributes != null) {
            activeAttributes = highlightAttributes
        } else {
            activeAttributes = attributes
        }
    }

    override fun doRender(rc: RenderContext) {

        val position = this.position ?: return
        val globe = rc.globe ?: return

        // Compute the placemark's Cartesian model point.
        rc.geographicToCartesian(position.latitude, position.longitude, position.altitude, altitudeMode, placePoint)

        cameraDistance = rc.cameraPoint.distanceTo(placePoint)

        var depthOffset = 0.0
        if (cameraDistance < rc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET
        }

        if (!rc.projectWithDepth(placePoint, depthOffset, screenPlacePoint)) {
            return
        }
        determineActiveAttributes(rc)
        if (activeAttributes == null) {
            return
        }

        // Allow the placemark to adjust the level of detail based on distance to the camera
        levelOfDetailSelector?.selectLevelOfDetail(rc, this, cameraDistance)

        val drawableCount = rc.drawableCount()
        if (rc.pickMode) {
            pickedObjectId = rc.nextPickedObjectId()
            pickColor = PickedObject.identifierToUniqueColor(pickedObjectId, pickColor)
        }
        if (mustDrawLeader(rc)) {
            // Compute the placemark's Cartesian ground point.
            groundPoint = rc.geographicToCartesian(position.latitude, position.longitude, 0.0, WorldWind.CLAMP_TO_GROUND, groundPoint)
            if (rc.frustum.intersectsSegment(groundPoint, placePoint)) {
                val pool: Pool<DrawableLines> = rc.getDrawablePool(DrawableLines::class.java)
                val drawable = DrawableLines.obtain(pool)
                prepareDrawableLeader(rc, drawable)
                rc.offerShapeDrawable(drawable, cameraDistance)
            }
        }

        activeAttributes?.imageSource?.let {
            activeTexture = rc.getTexture(activeAttributes!!.imageSource!!)
            if (activeTexture == null) {
                if (!rc.frustum.containsPoint(placePoint)) {
                    return
                }
            }
        }
        this.determineActiveTexture(rc)

        WWMath.boundingRectForUnitSquare(unitSquareTransform, screenBounds) // TODO allocation

        if (rc.frustum.intersectsViewport(screenBounds)) {
            val pool: Pool<DrawableScreenTexture> = rc.getDrawablePool(DrawableScreenTexture::class.java)
            val drawable: DrawableScreenTexture = DrawableScreenTexture.obtain(pool)
            prepareDrawableIcon(rc, drawable)
            rc.offerShapeDrawable(drawable, cameraDistance)
        }

        activeTexture = null

        // Enqueue a picked object that associates the placemark's icon and leader with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(pickedObjectId, this, rc.currentLayer!!))
        }

    }

    protected open fun prepareDrawableIcon(
        rc: RenderContext,
        drawable: DrawableScreenTexture
    ) {
        val activeAttributes = this.activeAttributes ?: return

        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(BasicProgram.KEY, BasicProgram(rc.resources!!)) as BasicProgram
        }

        drawable.unitSquareTransform.set(unitSquareTransform)



        // Configure the drawable according to the placemark's active attributes. Use a color appropriate for the pick
        // mode. When picking use a unique color associated with the picked object ID. Use the texture associated with
        // the active attributes' image source and its associated tex coord transform. If the texture is not specified
        // or not available, draw a simple colored square.
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes.imageColor!!)
        drawable.enableDepthTest = activeAttributes.depthTest
        drawable.texture = activeTexture
    }

    protected open fun prepareDrawableLeader(
        rc: RenderContext,
        drawable: DrawableLines
    ) {

        val activeAttributes = this.activeAttributes ?: return

        // Use the basic GLSL program to draw the placemark's leader line.
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(BasicProgram.KEY, BasicProgram(rc.resources!!)) as BasicProgram
        }

        drawable.vertexPoints[0] = 0f // groundPoint.x - groundPoint.x
        drawable.vertexPoints[1] = 0f // groundPoint.y - groundPoint.y
        drawable.vertexPoints[2] = 0f // groundPoint.z - groundPoint.z

        drawable.vertexPoints[3] = (placePoint.x - groundPoint.x).toFloat()
        drawable.vertexPoints[4] = (placePoint.y - groundPoint.y).toFloat()
        drawable.vertexPoints[5] = (placePoint.z - groundPoint.z).toFloat()

        drawable.mvpMatrix.set(rc.modelviewProjection)
        drawable.mvpMatrix.multiplyByTranslation(groundPoint.x, groundPoint.y, groundPoint.z)

        drawable.lineWidth = activeAttributes.leaderAttributes!!.outlineWidth
        drawable.enableDepthTest = activeAttributes.leaderAttributes!!.depthTest
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes.leaderAttributes!!.outlineColor!!)

    }

    protected open fun mustDrawLeader(dc: RenderContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false
        return (activeAttributes.drawLeader && activeAttributes.leaderAttributes != null && (enableLeaderPicking || !dc.pickMode))
    }


    protected open fun determineActiveTexture(rc: RenderContext) {
        val activeAttributes = this.activeAttributes ?: return
        if (activeAttributes.imageSource != null) {
            if (activeTexture == null) {
                activeTexture = rc.retrieveTexture(activeAttributes.imageSource , null) // puts retrieved textures in the cache
            }
        } else {
            activeTexture = null // there is no imageSource; draw a simple colored square
        }

        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(
            value = this.eyeDistanceScalingThreshold / cameraDistance,
            min = activeAttributes.minimumImageScale,
            max = 1.0
        ) else 1.0

        unitSquareTransform.setToIdentity()

        if (activeTexture != null) {
            val activeTexture = this.activeTexture!!
            val w: Int = activeTexture.imageWidth
            val h: Int = activeTexture.imageHeight
            val s = activeAttributes.imageScale * visibilityScale
            val offset =
                activeAttributes.imageOffset!!.offsetForSize(w.toDouble(), h.toDouble(), offset) // TODO allocation
            unitSquareTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x * s,
                screenPlacePoint.y - offset.y * s,
                screenPlacePoint.z
            )
            unitSquareTransform.multiplyByScale(w * s, h * s, 1.0)
        } else {
            // This branch serves both non-textured attributes and also textures that haven't been loaded yet.
            // We set the size for non-loaded textures to the typical size of a contemporary "small" icon (24px)
            var size: Double = if (activeAttributes.imageSource != null) 24.0 else activeAttributes.imageScale
            size *= visibilityScale
            unitSquareTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x,
                screenPlacePoint.y - offset.y,
                screenPlacePoint.z
            )
            unitSquareTransform.multiplyByScale(size, size, 1.0)
        }
        if (imageRotation != 0.0) {
            val rotation =
                if (imageRotationReference == WorldWind.RELATIVE_TO_GLOBE) rc.camera.heading - imageRotation else -imageRotation
            unitSquareTransform.multiplyByTranslation(0.5, 0.5, 0.0)
            unitSquareTransform.multiplyByRotation(0.0, 0.0, 1.0, rotation)
            unitSquareTransform.multiplyByTranslation(-0.5, -0.5, 0.0)
        }
        if (imageTilt != 0.0) {
            val tilt =
                if (imageTiltReference == WorldWind.RELATIVE_TO_GLOBE) rc.camera.tilt + imageTilt else imageTilt
            unitSquareTransform.multiplyByRotation(-1.0, 0.0, 0.0, tilt)
        }
    }

    override fun isHighlighted(): Boolean {
        return _highlighted
    }

    override fun setHighlighted(highlighted: Boolean) {
        this._highlighted = highlighted
    }

    override fun getReferencePosition(): Position? {
        return position
    }

    override fun moveTo(globe: Globe, position: Position?) {
        this.position = position
    }
}
