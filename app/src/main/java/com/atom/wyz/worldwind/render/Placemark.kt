package com.atom.wyz.worldwind.render

import android.graphics.Rect
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawablePlacemark
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Vec2
import com.atom.wyz.worldwind.geom.Vec3
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath

open class Placemark : AbstractRenderable{

    companion object {

        var DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6

        var DEFAULT_DEPTH_OFFSET = -0.003

        fun simple(position: Position?, color: Color?, pixelSize: Int): Placemark {
            val defaults = PlacemarkAttributes.defaults()
            defaults.imageColor = color
            defaults.imageScale = pixelSize.toDouble()
            return Placemark(position, defaults)
        }

        fun simpleImage(position: Position?, imageSource: ImageSource?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImage(imageSource))
        }

        fun simpleImageAndLabel(position: Position?, imageSource: ImageSource?, label: String?): Placemark {
            return Placemark(position, PlacemarkAttributes.withImageAndLabel(imageSource), label)
        }
    }

    var position: Position? = null

    var label: String? = null
        get() {
            if (field == null) {
                return this.displayName
            } else {
                return field
            }
        }
    private var eyeDistance = 0.0

    private var placePoint: Vec3? = Vec3(0.0, 0.0, 0.0)

    private var groundPoint: Vec3? = null

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

    constructor(position: Position?) : this(position, PlacemarkAttributes())

    constructor(position: Position?, attributes: PlacemarkAttributes?) : this(position, attributes, "Label")

    constructor(position: Position?, attributes: PlacemarkAttributes?, label: String?) : this(position, attributes, label, "Label", false)

    constructor(position: Position?, attributes: PlacemarkAttributes?, displayName: String?, label: String?, eyeDistanceScaling: Boolean) {
        if (position == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Placemark", "constructor", "missingPosition"))
        }
        this.position = position
        this.altitudeMode = WorldWind.ABSOLUTE
        displayName?.let { this.displayName = it }
        label?.let { this.label = it }
        this.attributes = if (attributes != null) attributes else PlacemarkAttributes()


        this.eyeDistanceScaling = eyeDistanceScaling
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

    protected fun makeDrawablePlacemark(dc: DrawContext?): DrawablePlacemark {
        if (this.drawablePlacemark == null) {
            this.drawablePlacemark = DrawablePlacemark()
        }
        return this.drawablePlacemark!!
    }

    override fun doRender(dc: DrawContext) {
        determineActiveAttributes(dc)
        val activeAttributes = this.activeAttributes ?: return

        placePoint = dc.globe!!.geographicToCartesian(position!!.latitude, position!!.longitude, position!!.altitude, placePoint)

        eyeDistance = dc.eyePoint.distanceTo(placePoint)

        if (activeAttributes.drawLeaderLine) {
            if (groundPoint == null) {
                groundPoint = Vec3()
            }
            groundPoint = dc.globe!!.geographicToCartesian(position!!.latitude, position!!.longitude, 0.0, groundPoint)
        }

        val drawable = this.makeDrawablePlacemark(dc)

        if (this.prepareDrawable(drawable, dc) && isVisible(drawable, dc)) {
            drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
            if (drawable.program == null) {
                drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
            }
            dc.offerDrawable(drawable , eyeDistance)
        }

    }

    fun mustDrawLeaderLine(dc: DrawContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false ;
        return activeAttributes.drawLeaderLine && activeAttributes.leaderLineAttributes != null && (!dc.pickingMode || enableLeaderLinePicking)
    }

    fun mustDrawLabel(dc :DrawContext ): Boolean {
        val activeAttributes = this.activeAttributes ?: return false ;
        return label != null && !label!!.isEmpty() && activeAttributes.labelAttributes != null
    }

    fun isVisible(drawable: DrawablePlacemark, dc: DrawContext): Boolean {
        val imageBounds: Rect? = drawable.imageTransform.let { WWMath.boundingRectForUnitSquare(drawable.imageTransform) }
        return (imageBounds != null && Rect.intersects(imageBounds, dc.viewport) || mustDrawLeaderLine(dc) && dc.frustum.intersectsSegment(groundPoint, placePoint))

    }

    protected fun prepareDrawable(drawable: DrawablePlacemark, dc: DrawContext): Boolean { // Get a reference to the attributes to use in the next drawing pass.

        val activeAttributes = this.activeAttributes ?: return false

        drawable.rotation = if (imageRotationReference == WorldWind.RELATIVE_TO_GLOBE) dc.heading - imageRotation else -imageRotation
        drawable.tilt = if (imageTiltReference == WorldWind.RELATIVE_TO_GLOBE) dc.tilt + imageTilt else imageTilt


        // Prepare the image
        activeAttributes.imageColor?.let { drawable.imageColor.set(it) }

        activeAttributes.imageSource?.let {
            drawable.iconTexture = dc.getTexture(it)
            if (drawable.iconTexture == null) {
                drawable.iconTexture = dc.retrieveTexture(it)
            }
        } ?: let {
            drawable.iconTexture = null
        }

        var depthOffset: Double = DEFAULT_DEPTH_OFFSET

        if (eyeDistance < dc.horizonDistance) {
            var longestSide = 1.0
            drawable.iconTexture?.let {
                longestSide = Math.max(it.imageWidth, it.imageHeight).toDouble()
            }
            val metersPerPixel = dc.pixelSizeAtDistance(eyeDistance)
            depthOffset = longestSide * activeAttributes.imageScale * metersPerPixel * -1
        }
        if (!dc.projectWithDepth(placePoint, depthOffset, drawable.screenPlacePoint)) { // Probably outside the clipping planes
            return false
        }

        drawable.program = dc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = dc.putProgram(BasicProgram.KEY, BasicProgram(dc.resources!!)) as BasicProgram
        }

        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(value = this.eyeDistanceScalingThreshold / eyeDistance, min = activeAttributes.minimumImageScale, max = 1.0) else 1.0

        drawable.iconTexture?.let {
            val w: Int = it.imageWidth
            val h: Int = it.imageHeight
            val s: Double = activeAttributes.imageScale * visibilityScale
            val offset: Vec2 = activeAttributes.imageOffset!!.offsetForSize(w.toDouble(), h.toDouble())
            drawable.imageTransform.setTranslation(
                    drawable.screenPlacePoint.x - offset.x * s,
                    drawable.screenPlacePoint.y - offset.y * s,
                    drawable.screenPlacePoint.z)
            drawable.imageTransform.setScale(w * s, h * s, 1.0)
        } ?: let {
            val size: Double = activeAttributes.imageScale * visibilityScale
            val offset: Vec2 = activeAttributes.imageOffset!!.offsetForSize(size, size)
            drawable.imageTransform.setTranslation(
                    drawable.screenPlacePoint.x - offset.x,
                    drawable.screenPlacePoint.y - offset.y,
                    drawable.screenPlacePoint.z)
            drawable.imageTransform.setScale(size, size, 1.0)
        }


        // Prepare the optional leader line
        drawable.drawLeader = mustDrawLeaderLine(dc)
        if (drawable.drawLeader) {
            if (drawable.leaderColor == null) {
                drawable.leaderColor = Color(activeAttributes.leaderLineAttributes?.outlineColor!!)
            } else {
                drawable.leaderColor!!.set(activeAttributes.leaderLineAttributes?.outlineColor!!)
            }
            drawable.leaderWidth = activeAttributes.leaderLineAttributes?.outlineWidth!!
            drawable.enableLeaderPicking = this.enableLeaderLinePicking

            if (drawable.screenGroundPoint == null) {
                drawable.screenGroundPoint = Vec3()
            }

            if (!dc.projectWithDepth(groundPoint, depthOffset, drawable.screenGroundPoint)) {
                drawable.drawLeader = false
            }
        }
        return true
    }
}
