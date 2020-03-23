package com.atom.wyz.worldwind.render

import android.graphics.Rect
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.draw.DrawablePlacemark
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class Placemark : AbstractRenderable, OrderedRenderable {

    companion object {

        var defaultEyeDistanceScalingThreshold = 1e6

        var defaultDepthOffset = -0.003

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


        protected var cacheKeyPool = 0

        private var unitQuadBuffer2: FloatBuffer? = null

        private var unitQuadBuffer3: FloatBuffer? = null

        private var leaderBuffer: FloatBuffer? = null

        private var leaderPoints: FloatArray? = null

        fun getUnitQuadBuffer2D(): FloatBuffer? {
            if (unitQuadBuffer2 == null) {
                val points = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f) // lower right corner
                val size = points.size * 4
                unitQuadBuffer2 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                unitQuadBuffer2?.put(points)?.rewind()
            }
            return unitQuadBuffer2
        }

        fun getUnitQuadBuffer3D(): FloatBuffer? {
            if (unitQuadBuffer3 == null) {
                val points = floatArrayOf(0f, 1f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 1f, 0f, 0f) // lower right corner
                val size = points.size * 4
                unitQuadBuffer3 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                unitQuadBuffer3?.put(points)?.rewind()
            }
            return unitQuadBuffer3
        }

        fun getLeaderBuffer(groundPoint: Vec3, placePoint: Vec3): FloatBuffer {
            if (leaderBuffer == null) {
                leaderPoints = FloatArray(6)
                leaderBuffer = ByteBuffer.allocateDirect(leaderPoints!!.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            }
            // TODO: consider whether these assignments should be inlined.
            leaderPoints?.let {
                it[0] = groundPoint.x.toFloat()
                it[1] = groundPoint.y.toFloat()
                it[2] = groundPoint.z.toFloat()
                it[3] = placePoint.x.toFloat()
                it[4] = placePoint.y.toFloat()
                it[5] = placePoint.z.toFloat()
            }

            leaderBuffer?.put(leaderPoints)?.rewind()
            return leaderBuffer!!
        }


        fun generateCacheKey(): String? {
            return "OrderedPlacemark " + ++cacheKeyPool
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
    var highlighted = false

    var eyeDistanceScaling: Boolean
    var eyeDistanceScalingThreshold: Double
    var eyeDistanceScalingLabelThreshold: Double

    var altitudeMode: Int = WorldWind.ABSOLUTE

    var enableLeaderLinePicking: Boolean

    var imageRotation: Double

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

        highlightAttributes = null
        highlighted = false

        this.eyeDistanceScaling = eyeDistanceScaling
        eyeDistanceScalingThreshold = defaultEyeDistanceScalingThreshold
        eyeDistanceScalingLabelThreshold = 1.5 * eyeDistanceScalingThreshold

        enableLeaderLinePicking = false

        imageRotation = 0.0
        imageTilt = 0.0
        imageRotationReference = WorldWind.RELATIVE_TO_SCREEN
        imageTiltReference = WorldWind.RELATIVE_TO_SCREEN

    }

    fun getActiveAttributes(dc: DrawContext): PlacemarkAttributes? {
        return attributes
    }

    protected fun makeDrawablePlacemark(dc: DrawContext?): DrawablePlacemark {
        if (this.drawablePlacemark == null) {
            this.drawablePlacemark = DrawablePlacemark()
        }
        return this.drawablePlacemark!!
    }

    override fun doRender(dc: DrawContext) {
        val activeAttributes: PlacemarkAttributes? = getActiveAttributes(dc)
        // 计算 经纬度高程对呀的笛卡尔坐标系
        placePoint = dc.globe!!.geographicToCartesian(position!!.latitude, position!!.longitude, position!!.altitude, placePoint)
        // 眼睛的距离
        eyeDistance = dc.eyePoint.distanceTo(placePoint)

        if (activeAttributes!!.drawLeaderLine) {
            if (groundPoint == null) {
                groundPoint = Vec3()
            }
            groundPoint = dc.globe!!.geographicToCartesian(position!!.latitude, position!!.longitude, 0.0, groundPoint)
        }
        // Get a drawable delegate this placemark.
        val drawable = this.makeDrawablePlacemark(dc)

        if (this.prepareDrawable(drawable, dc) && isVisible(drawable, dc)) {
            dc.offerOrderedRenderable(this, eyeDistance)
        }

    }

    override fun renderOrdered(dc: DrawContext) {
        drawablePlacemark!!.draw(dc)
    }

    fun mustDrawLeaderLine(dc: DrawContext): Boolean {
        val activeAttributes: PlacemarkAttributes = getActiveAttributes(dc) ?: return false
        return activeAttributes.drawLeaderLine && activeAttributes.leaderLineAttributes != null && (!dc.pickingMode || enableLeaderLinePicking)
    }

    fun mustDrawLabel(dc :DrawContext ): Boolean {
        val activeAttributes: PlacemarkAttributes = getActiveAttributes(dc) ?: return false
        return label != null && !label!!.isEmpty() && activeAttributes.labelAttributes != null
    }

    fun isVisible(drawable: DrawablePlacemark, dc: DrawContext): Boolean {

        val imageBounds: Rect? = drawable.imageTransform?.let { WWMath.boundingRectForUnitSquare(drawable.imageTransform) }
                ?: let { null }
        val labelBounds: Rect? = drawable.labelTransform?.let { WWMath.boundingRectForUnitSquare(drawable.labelTransform) }
                ?: let { null }

        if (dc.pickingMode) {
            return imageBounds != null && imageBounds.intersect(dc.viewport)
        } else {
            return (imageBounds != null && imageBounds.intersect(dc.viewport))
                    || (mustDrawLabel(dc) && labelBounds != null && labelBounds.intersect(dc.viewport))
                    || (mustDrawLeaderLine(dc) && dc.frustum.intersectsSegment(this.groundPoint, this.placePoint));
        }
    }

    protected fun prepareDrawable(drawable: DrawablePlacemark, dc: DrawContext): Boolean { // Get a reference to the attributes to use in the next drawing pass.

        val activeAttributes: PlacemarkAttributes = getActiveAttributes(dc) ?: return false

        drawable.label = this.label
        drawable.actualRotation = if (imageRotationReference == WorldWind.RELATIVE_TO_GLOBE) dc.heading - imageRotation else -imageRotation
        drawable.actualTilt = if (imageTiltReference == WorldWind.RELATIVE_TO_GLOBE) dc.tilt + imageTilt else imageTilt


        // Prepare the image
        activeAttributes.imageColor?.let { drawable.imageColor.set(it) }

        activeAttributes.imageSource?.let {
            drawable.activeTexture = dc.getTexture(it)
            if (drawable.activeTexture == null) {
                drawable.activeTexture = dc.retrieveTexture(it)
            }
        } ?: let {
            drawable.activeTexture = null
        }

        var depthOffset: Double = defaultDepthOffset

        if (eyeDistance < dc.horizonDistance) {
            var longestSide = 1.0
            drawable.activeTexture?.let {
                longestSide = Math.max(it.imageWidth, it.imageHeight).toDouble()
            }
            val metersPerPixel = dc.pixelSizeAtDistance(eyeDistance)
            depthOffset = longestSide * activeAttributes.imageScale * metersPerPixel * -1
        }
        if (!dc.projectWithDepth(placePoint, depthOffset, drawable.screenPlacePoint)) { // Probably outside the clipping planes
            return false
        }

        //val visibilityScale: Double = if (this.eyeDistanceScaling) Math.max(activeAttributes.minimumImageScale, Math.min(1.0, this.eyeDistanceScalingThreshold / eyeDistance)) else 1.0
        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(value = this.eyeDistanceScalingThreshold / eyeDistance, min = activeAttributes.minimumImageScale, max = 1.0) else 1.0

        drawable.activeTexture?.let {
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
        // Prepare the label
        drawable.drawLabel = mustDrawLabel(dc)
        if (drawable.drawLabel) {
            if (drawable.labelColor == null) {
                drawable.labelColor = Color(activeAttributes.labelAttributes?.color!!)
            } else {
                drawable.labelColor!!.set(activeAttributes.labelAttributes?.color!!)
            }
            val labelFont = attributes.labelAttributes!!.font
            val labelKey = label + labelFont.toString()
            drawable.labelTexture = dc.renderResourceCache?.get(labelKey) as GpuTexture?
            if (drawable.labelTexture == null) {
                //this.labelTexture = dc.createFontTexture(Placemark.this.displayName, labelFont, false);
            }
            if (drawable.labelTexture != null) {
                if (drawable.labelTransform == null) {
                    drawable.labelTransform = Matrix4()
                }
                val w: Int = drawable.labelTexture!!.imageWidth
                val h: Int = drawable.labelTexture!!.imageHeight
                val s = attributes.labelAttributes!!.scale * visibilityScale
                val offset: Vec2 = attributes.labelAttributes!!.offset!!.offsetForSize(w.toDouble(), h.toDouble())
                drawable.labelTransform!!.setTranslation(
                        drawable.screenPlacePoint.x - offset.x * s,
                        drawable.screenPlacePoint.y - offset.y * s,
                        drawable.screenPlacePoint.z)
                drawable.labelTransform!!.setScale(w * s, h * s, 1.0)
            }
        }
        return true
    }
}
