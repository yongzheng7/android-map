package com.atom.wyz.worldwind.render

import android.graphics.Rect
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawableLines
import com.atom.wyz.worldwind.draw.DrawableQuad
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.pool.Pool

open class Placemark : AbstractRenderable {

    companion object {

        var DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6

        var DEFAULT_DEPTH_OFFSET = -0.003

        fun simple(position: Position, color: Color?, pixelSize: Int): Placemark {
            val defaults = PlacemarkAttributes.defaults()
            defaults.imageColor = color
            defaults.imageScale = pixelSize.toDouble()
            return Placemark(position, defaults)
        }

        fun simpleImage(position: Position, imageSource: ImageSource?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImage(imageSource))
        }

        fun simpleImageAndLabel(position: Position, imageSource: ImageSource?, label: String?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImageAndLabel(imageSource), label)
        }

        private var placePoint = Vec3()
        private val screenPlacePoint = Vec3()
        private var groundPoint = Vec3()
        private val offset: Vec2 = Vec2()
        private val unitQuadBounds = Rect()
        private val unitQuadTransform: Matrix4 = Matrix4()
    }

    var position: Position?

    var label: String? = null
        get() {
            if (field == null) {
                return this.displayName
            } else {
                return field
            }
        }
    private var eyeDistance = 0.0


    var attributes: PlacemarkAttributes

    var highlightAttributes: PlacemarkAttributes? = null
    var activeAttributes: PlacemarkAttributes? = null

    var activeTexture: GpuTexture? = null


    var highlighted = false

    var eyeDistanceScaling: Boolean
    var eyeDistanceScalingThreshold: Double
    var eyeDistanceScalingLabelThreshold: Double

    var altitudeMode: Int = WorldWind.ABSOLUTE

    var enableLeaderLinePicking: Boolean = false

    var imageRotation = 0.0
    var imageTilt = 0.0


    @WorldWind.OrientationMode
    var imageRotationReference = 0

    @WorldWind.OrientationMode
    var imageTiltReference = 0


    constructor(position: Position) : this(position, PlacemarkAttributes())

    constructor(position: Position, attributes: PlacemarkAttributes) : this(position, attributes, null, null)

    constructor(position: Position, attributes: PlacemarkAttributes, label: String?) : this(
        position,
        attributes,
        label,
        null
    )

    constructor(position: Position?, attributes: PlacemarkAttributes?, displayName: String?, label: String?) {
        if (position == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Placemark", "constructor", "missingPosition")
            )
        }
        this.position = position
        this.altitudeMode = WorldWind.ABSOLUTE
        displayName?.let { this.displayName = it }
        label?.let { this.label = it }
        this.attributes = if (attributes != null) attributes else PlacemarkAttributes()


        this.eyeDistanceScaling = false
        eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD
        eyeDistanceScalingLabelThreshold = 1.5 * eyeDistanceScalingThreshold

        imageRotationReference = WorldWind.RELATIVE_TO_SCREEN
        imageTiltReference = WorldWind.RELATIVE_TO_SCREEN

    }

    protected open fun determineActiveAttributes(dc: DrawContext) {
        if (highlighted && highlightAttributes != null) {
            activeAttributes = highlightAttributes
        } else {
            activeAttributes = attributes
        }
    }

    override fun doRender(dc: DrawContext) {
        determineActiveAttributes(dc)

        val position = this.position ?: return
        val globe = dc.globe ?: return

        globe.geographicToCartesian(position.latitude, position.longitude, position.altitude, placePoint)

        eyeDistance = dc.eyePoint.distanceTo(placePoint)

        var depthOffset = 0.0
        if (eyeDistance < dc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET
        }

        if (!dc.projectWithDepth(placePoint, depthOffset, screenPlacePoint)) {
            return
        }
        if (mustDrawLeaderLine(dc)) {
            groundPoint = globe.geographicToCartesian(
                position.latitude,
                position.longitude,
                0.0,
                groundPoint
            )
            if (dc.frustum.intersectsSegment(groundPoint, placePoint)) {
                val pool: Pool<DrawableLines> = dc.getDrawablePool(DrawableLines::class.java)
                val drawable = DrawableLines.obtain(pool)
                prepareDrawableLeader(dc, drawable)
                dc.offerShapeDrawable(drawable, eyeDistance)
            }
        }
        this.determineActiveTexture(dc)

        val iconBounds: Rect = WWMath.boundingRectForUnitSquare(unitQuadTransform, unitQuadBounds) // TODO allocation

        if (Rect.intersects(iconBounds, dc.viewport)) {
            val pool: Pool<DrawableQuad> = dc.getDrawablePool(DrawableQuad::class.java)
            val drawable: DrawableQuad = DrawableQuad.obtain(pool)
            prepareDrawableIcon(dc, drawable)
            dc.offerShapeDrawable(drawable, eyeDistance)
        }

        activeTexture = null


    }

    protected open fun prepareDrawableIcon(
        dc: DrawContext,
        drawable: DrawableQuad
    ) {
        val activeAttributes = this.activeAttributes ?: return

        drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
        }

        drawable.mvpMatrix.setToMultiply(dc.screenProjection, unitQuadTransform)

        drawable.color.set(activeAttributes.imageColor!!)
        drawable.enableDepthTest = activeAttributes.depthTest
        drawable.texture = activeTexture
    }

    protected open fun prepareDrawableLeader(
        dc: DrawContext,
        drawable: DrawableLines
    ) {

        val activeAttributes = this.activeAttributes ?: return

        // Use the basic GLSL program to draw the placemark's leader line.
        drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
        }

        drawable.vertexPoints[0] = 0f // groundPoint.x - groundPoint.x
        drawable.vertexPoints[1] = 0f // groundPoint.y - groundPoint.y
        drawable.vertexPoints[2] = 0f // groundPoint.z - groundPoint.z

        drawable.vertexPoints[3] = (placePoint.x - groundPoint.x).toFloat()
        drawable.vertexPoints[4] = (placePoint.y - groundPoint.y).toFloat()
        drawable.vertexPoints[5] = (placePoint.z - groundPoint.z).toFloat()

        drawable.mvpMatrix.set(dc.modelviewProjection)
        drawable.mvpMatrix.multiplyByTranslation(groundPoint.x, groundPoint.y, groundPoint.z)

        drawable.color.set(activeAttributes.leaderLineAttributes!!.outlineColor!!)
        drawable.lineWidth = activeAttributes.leaderLineAttributes!!.outlineWidth
        drawable.enableDepthTest = activeAttributes.leaderLineAttributes!!.depthTest

    }

    protected open fun mustDrawLeaderLine(dc: DrawContext): Boolean {
        return (activeAttributes!!.drawLeaderLine && activeAttributes!!.leaderLineAttributes != null && (enableLeaderLinePicking || !dc.pickingMode))
    }

    protected open fun mustDrawLabel(dc: DrawContext?): Boolean {
        return (label != null && !label!!.isEmpty() && activeAttributes!!.labelAttributes != null)
    }

    protected open fun determineActiveTexture(dc: DrawContext) {
        val activeAttributes = this.activeAttributes ?: return
        if (activeAttributes.imageSource != null) {
            activeTexture = dc.getTexture(activeAttributes.imageSource!!) // try to get the texture from the cache
            if (activeTexture == null) {
                activeTexture = dc.retrieveTexture(activeAttributes.imageSource) // puts retrieved textures in the cache
            }
        } else {
            activeTexture = null // there is no imageSource; draw a simple colored square
        }

        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(
            value = this.eyeDistanceScalingThreshold / eyeDistance,
            min = activeAttributes.minimumImageScale,
            max = 1.0
        ) else 1.0

        unitQuadTransform.setToIdentity()

        if (activeTexture != null) {
            val activeTexture = this.activeTexture!!
            val w: Int = activeTexture.imageWidth
            val h: Int = activeTexture.imageHeight
            val s = activeAttributes.imageScale * visibilityScale
            val offset =
                activeAttributes.imageOffset!!.offsetForSize(w.toDouble(), h.toDouble(), offset) // TODO allocation
            unitQuadTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x * s,
                screenPlacePoint.y - offset.y * s,
                screenPlacePoint.z
            )
            unitQuadTransform.multiplyByScale(w * s, h * s, 1.0)
        } else {
            val size = activeAttributes.imageScale * visibilityScale
            val offset = activeAttributes.imageOffset!!.offsetForSize(size, size, offset) // TODO allocation
            unitQuadTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x,
                screenPlacePoint.y - offset.y,
                screenPlacePoint.z
            )
            unitQuadTransform.multiplyByScale(size, size, 1.0)
        }
        if (imageRotation != 0.0) {
            val rotation =
                if (imageRotationReference == WorldWind.RELATIVE_TO_GLOBE) dc.heading - imageRotation else -imageRotation
            unitQuadTransform.multiplyByTranslation(0.5, 0.5, 0.0)
            unitQuadTransform.multiplyByRotation(0.0, 0.0, 1.0, rotation)
            unitQuadTransform.multiplyByTranslation(-0.5, -0.5, 0.0)
        }
        if (imageTilt != 0.0) {
            val tilt =
                if (imageTiltReference == WorldWind.RELATIVE_TO_GLOBE) dc.tilt + imageTilt else imageTilt
            unitQuadTransform.multiplyByRotation(-1.0, 0.0, 0.0, tilt)
        }
    }
}
