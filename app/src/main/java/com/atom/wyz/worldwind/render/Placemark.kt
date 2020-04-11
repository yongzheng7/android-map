package com.atom.wyz.worldwind.render

import android.graphics.Rect
import com.atom.wyz.worldwind.DrawContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawablePlacemark
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath

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

    private var placePoint = Vec3()
    private val screenPlacePoint = Vec3()


    private var groundPoint: Vec3? = null
    private var screenGroundPoint: Vec3? = null
    var attributes: PlacemarkAttributes

    var highlightAttributes: PlacemarkAttributes? = null
    var activeAttributes: PlacemarkAttributes? = null
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

    var drawablePlacemark: DrawablePlacemark? = null

    constructor(position: Position) : this(position, PlacemarkAttributes())

    constructor(position: Position, attributes: PlacemarkAttributes) : this(position, attributes, null , null)

    constructor(position: Position, attributes: PlacemarkAttributes, label: String?) : this(position, attributes, label, null)

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

        val drawable = DrawablePlacemark.obtain();

        this.prepareDrawableIcon(dc, drawable)

        drawable.drawLeader = mustDrawLeaderLine(dc)
        if (drawable.drawLeader) {
            groundPoint = globe.geographicToCartesian(
                position.latitude,
                position.longitude,
                0.0,
                if (groundPoint != null) groundPoint else Vec3()
            )

            if (screenGroundPoint == null) {
                screenGroundPoint = Vec3()
            }
            if (!dc.projectWithDepth(groundPoint, depthOffset, screenGroundPoint)) {
                drawable.drawLeader = false
            }
            if (drawable.drawLeader) {
                this.prepareDrawableLeader(dc, drawable)
            }
        }

        if (isVisible(dc, drawable)) {
            drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
            if (drawable.program == null) {
                drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
            }
            dc.offerShapeDrawable(drawable,  eyeDistance)
        } else {
            drawable.recycle()
        }

    }

    protected open fun prepareDrawableIcon(
        dc: DrawContext,
        drawable: DrawablePlacemark
    ) {
        val activeAttributes = this.activeAttributes ?: return

        activeAttributes.imageColor?.let { drawable.iconColor.set(it) }

        activeAttributes.imageSource?.let {
            drawable.iconTexture = dc.getTexture(it)
            if (drawable.iconTexture == null) {
                drawable.iconTexture = dc.retrieveTexture(it)
            }
        } ?: let {
            drawable.iconTexture = null
        }

        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(
            value = this.eyeDistanceScalingThreshold / eyeDistance,
            min = activeAttributes.minimumImageScale,
            max = 1.0
        ) else 1.0

        drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
        }

        drawable.iconMvpMatrix.set(dc.screenProjection)

        drawable.iconTexture?.let {
            val w: Int = it.imageWidth
            val h: Int = it.imageHeight
            val s: Double = activeAttributes.imageScale * visibilityScale
            val offset: Vec2 = activeAttributes.imageOffset!!.offsetForSize(w.toDouble(), h.toDouble())
            drawable.iconMvpMatrix.setTranslation(
                this.screenPlacePoint.x - offset.x * s,
                this.screenPlacePoint.y - offset.y * s,
                this.screenPlacePoint.z
            )
            drawable.iconMvpMatrix.setScale(w * s, h * s, 1.0)
            drawable.iconTexCoordMatrix.set(it.texCoordTransform)

        } ?: let {
            val size: Double = activeAttributes.imageScale * visibilityScale
            val offset: Vec2 = activeAttributes.imageOffset!!.offsetForSize(size, size)
            drawable.iconMvpMatrix.setTranslation(
                this.screenPlacePoint.x - offset.x,
                this.screenPlacePoint.y - offset.y,
                this.screenPlacePoint.z
            )
            drawable.iconMvpMatrix.setScale(size, size, 1.0)
        }

        if (imageRotation != 0.0) {
            val rotation =
                if (imageRotationReference == WorldWind.RELATIVE_TO_GLOBE) dc.heading - imageRotation else -imageRotation
            drawable.iconMvpMatrix.multiplyByTranslation(0.5, 0.5, 0.0)
            drawable.iconMvpMatrix.multiplyByRotation(0.0, 0.0, 1.0, rotation)
            drawable.iconMvpMatrix.multiplyByTranslation(-0.5, -0.5, 0.0)
        }

        if (imageTilt != 0.0) {
            val tilt = if (imageTiltReference == WorldWind.RELATIVE_TO_GLOBE) dc.tilt + imageTilt else imageTilt
            drawable.iconMvpMatrix.multiplyByRotation((-1).toDouble(), 0.0, 0.0, tilt)
        }
    }

    protected open fun prepareDrawableLeader(
        dc: DrawContext,
        drawable: DrawablePlacemark
    ) {
        val activeAttributes = this.activeAttributes ?: return
        val screenGroundPoint = this.screenGroundPoint ?: return

        if (drawable.leaderColor == null) {
            drawable.leaderColor = Color()
        }

        if (drawable.leaderMvpMatrix == null) {
            drawable.leaderMvpMatrix = Matrix4()
        }

        if (drawable.leaderVertexPoint == null) {
            drawable.leaderVertexPoint = FloatArray(6)
        }

        drawable.leaderWidth = activeAttributes.leaderLineAttributes?.outlineWidth!!
        drawable.leaderColor!!.set(activeAttributes.leaderLineAttributes!!.outlineColor!!)

        drawable.enableLeaderDepthTest = this.activeAttributes!!.leaderLineAttributes!!.depthTest
        drawable.enableLeaderPicking = this.enableLeaderLinePicking

        drawable.leaderMvpMatrix!!.set(dc.modelviewProjection)
        drawable.leaderMvpMatrix!!.multiplyByTranslation(groundPoint!!.x, groundPoint!!.y, groundPoint!!.z)

        drawable.leaderVertexPoint!![0] = screenGroundPoint.x.toFloat()
        drawable.leaderVertexPoint!![1] = screenGroundPoint.y.toFloat()
        drawable.leaderVertexPoint!![2] = screenGroundPoint.z.toFloat()
        drawable.leaderVertexPoint!![3] = screenPlacePoint.x.toFloat()
        drawable.leaderVertexPoint!![4] = screenPlacePoint.y.toFloat()
        drawable.leaderVertexPoint!![5] = screenPlacePoint.z.toFloat()

    }

    fun mustDrawLeaderLine(dc: DrawContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false
        return activeAttributes.drawLeaderLine && activeAttributes.leaderLineAttributes != null && (!dc.pickingMode || enableLeaderLinePicking)
    }

    fun isVisible(dc: DrawContext, drawable: DrawablePlacemark): Boolean {
        val imageBounds = WWMath.boundingRectForUnitSquare(drawable.iconMvpMatrix)
        return Rect.intersects(imageBounds, dc.viewport) ||
                (mustDrawLeaderLine(dc) && dc.frustum.intersectsSegment(groundPoint, placePoint))
    }
}
