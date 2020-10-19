package com.atom.map.layer.render

import com.atom.map.WorldWind
import com.atom.map.core.shader.BasicProgram
import com.atom.map.core.shader.GpuTexture
import com.atom.map.geom.*
import com.atom.map.globe.Globe
import com.atom.map.layer.draw.DrawableLines
import com.atom.map.layer.draw.DrawableScreenTexture
import com.atom.map.layer.render.attribute.PlacemarkAttributes
import com.atom.map.layer.render.pick.PickedObject
import com.atom.map.layer.render.shape.Highlightable
import com.atom.map.layer.render.shape.Movable
import com.atom.map.util.StringUtils
import com.atom.map.util.WWMath
import com.atom.map.util.pool.Pool

open class Placemark : AbstractRenderable,
    Highlightable,
    Movable {

    interface LevelOfDetailSelector {
        fun selectLevelOfDetail(rc: RenderContext, placemark: Placemark?, cameraDistance: Double)
    }

    companion object {

        var DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6

        var DEFAULT_DEPTH_OFFSET = -0.003

        fun createSimple(position: Position, color: SimpleColor, pixelSize: Int): Placemark {
            val defaults = PlacemarkAttributes.defaults()
            defaults.imageColor = color
            defaults.imageScale = pixelSize.toDouble()
            return Placemark(
                position,
                defaults
            )
        }

        fun createSimpleImage(position: Position, imageSource: ImageSource): Placemark {
            return Placemark(
                position,
                PlacemarkAttributes.withImage(
                    imageSource
                )
            )
        }

        fun createSimpleImageAndLabel(
            position: Position,
            imageSource: ImageSource,
            label: String
        ): Placemark {
            return Placemark(
                position,
                PlacemarkAttributes.withImage(
                    imageSource
                ),
                label
            )
        }

        private var placePoint = Vec3()
        private val screenPlacePoint = Vec3()
        private var groundPoint = Vec3()
        private val offset: Vec2 = Vec2()
        private val screenBounds = Viewport()
        private val unitSquareTransform: Matrix4 = Matrix4()
    }

    var position: Position? = null

    var label: String

    var cameraDistance = 0.0

    var levelOfDetailSelector: LevelOfDetailSelector? = null

    var attributes: PlacemarkAttributes

    var highlightAttributes: PlacemarkAttributes? = null

    private  var activeAttributes: PlacemarkAttributes ? = null

    var eyeDistanceScaling: Boolean
    var eyeDistanceScalingThreshold: Double
    var eyeDistanceScalingLabelThreshold: Double

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE

    var imageRotation = 0.0

    var imageTilt = 0.0

    @WorldWind.OrientationMode
    var imageRotationReference = 0

    @WorldWind.OrientationMode
    var imageTiltReference = 0

    /**
     * The picked object ID associated with the placemark during the current render pass.
     */
    private var pickedObjectId = 0

    protected var pickColor = SimpleColor()

    constructor(position: Position) : this(position, PlacemarkAttributes.defaults())

    constructor(
        position: Position,
        attributes: PlacemarkAttributes,
        label: String = StringUtils.getRandomString(6)
    ) : super("Placemark") {
        this.position = position
        this.label = label
        this.attributes = attributes
        this.eyeDistanceScaling = false
        this.altitudeMode = WorldWind.ABSOLUTE
        this.eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD
        this.eyeDistanceScalingLabelThreshold = 1.5 * eyeDistanceScalingThreshold
        this.imageRotationReference = WorldWind.RELATIVE_TO_SCREEN
        this.imageTiltReference = WorldWind.RELATIVE_TO_SCREEN
    }

    protected open fun determineActiveAttributes(rc: RenderContext) {
        activeAttributes = if (highlighted && highlightAttributes != null) {
            highlightAttributes
        } else {
            attributes
        }
    }

    override fun doRender(rc: RenderContext) {
        val position = this.position ?: return
        // Compute the placemark's Cartesian model point.
        rc.geographicToCartesian(
            position.latitude,
            position.longitude,
            position.altitude,
            altitudeMode,
            placePoint
        )

        cameraDistance = rc.cameraPoint.distanceTo(placePoint)

        var depthOffset = 0.0

        if (cameraDistance < rc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET
        }
        if (!rc.projectWithDepth(
                placePoint, depthOffset,
                screenPlacePoint
            )
        ) {
            return
        }
        determineActiveAttributes(rc)

        if (activeAttributes == null) {
            return
        }

        // 允许地标根据距相机的距离调整细节级别
        levelOfDetailSelector?.selectLevelOfDetail(rc, this, cameraDistance)

        val drawableCount = rc.drawableCount()
        if (rc.pickMode) {
            pickedObjectId = rc.nextPickedObjectId()
            pickColor = PickedObject.identifierToUniqueColor(pickedObjectId, pickColor)
        }


        if (mustDrawLeader(rc)) {
            // Compute the placemark's Cartesian ground point.
            groundPoint = rc.geographicToCartesian(
                position.latitude, position.longitude, 0.0, WorldWind.CLAMP_TO_GROUND,
                groundPoint
            )
            if (rc.frustum.intersectsSegment(groundPoint, placePoint)) {
                val leader: Pool<DrawableLines> = rc.getDrawablePool(
                    DrawableLines::class.java
                )
                val leaderDrawable = DrawableLines.obtain(leader)
                prepareDrawableLeader(rc, leaderDrawable)
                rc.offerShapeDrawable(leaderDrawable, cameraDistance)
            }
        }

        if (mustDrawLabel(rc)) {
            var labelTexture: GpuTexture? = null
            if (rc.getText(label, activeAttributes!!.labelAttributes)
                    .also { labelTexture = it } == null
            ) {
                if (!rc.frustum.containsPoint(placePoint)) {
                    return
                }
            }
            labelTexture = determineActiveLabelBounds(rc, labelTexture)

            // TODO allocation
            WWMath.boundingRectForUnitSquare(
                unitSquareTransform,
                screenBounds
            )
            if (rc.frustum.intersectsViewport(screenBounds) && labelTexture != null) {
                val pool: Pool<DrawableScreenTexture> = rc.getDrawablePool(DrawableScreenTexture::class.java)
                val drawable: DrawableScreenTexture = DrawableScreenTexture.obtain(pool)
                prepareDrawableLabel(rc, drawable , labelTexture!!)
                rc.offerShapeDrawable(drawable, cameraDistance)
            }
        }

        var surfaceTexture: GpuTexture? = null
        activeAttributes!!.imageSource?.let {
            if (rc.getTexture(it).also { itTexture -> surfaceTexture = itTexture } == null) {
                if (!rc.frustum.containsPoint(placePoint)) {
                    return
                }
            }
        }
        surfaceTexture = this.determineActiveTextureBounds(rc, surfaceTexture)

        // TODO allocation
        WWMath.boundingRectForUnitSquare(
            unitSquareTransform,
            screenBounds
        )

        if (rc.frustum.intersectsViewport(screenBounds) && surfaceTexture != null) {
            val pool: Pool<DrawableScreenTexture> = rc.getDrawablePool(
                DrawableScreenTexture::class.java
            )
            val drawable: DrawableScreenTexture = DrawableScreenTexture.obtain(pool)
            prepareDrawableIcon(rc, drawable, surfaceTexture!!)
            rc.offerShapeDrawable(drawable, cameraDistance)
        }

        // Enqueue a picked object that associates the placemark's icon and leader with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(
                PickedObject.fromRenderable(
                    pickedObjectId,
                    this,
                    rc.currentLayer!!
                )
            )
        }

    }

    protected open fun prepareDrawableIcon(
        rc: RenderContext,
        drawable: DrawableScreenTexture,
        texture: GpuTexture
    ) {
        val activeAttributes = this.activeAttributes ?: return

        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }

        drawable.unitSquareTransform.set(unitSquareTransform)


        // Configure the drawable according to the placemark's active attributes. Use a color appropriate for the pick
        // mode. When picking use a unique color associated with the picked object ID. Use the texture associated with
        // the active attributes' image source and its associated tex coord transform. If the texture is not specified
        // or not available, draw a simple colored square.
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes.imageColor)
        drawable.enableDepthTest = activeAttributes.depthTest
        drawable.texture = texture
    }

    protected open fun prepareDrawableLeader(
        rc: RenderContext,
        drawable: DrawableLines
    ) {

        val activeAttributes = this.activeAttributes ?: return

        // Use the basic GLSL program to draw the placemark's leader line.
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program = rc.putProgram(
                BasicProgram.KEY,
                BasicProgram(rc.resources)
            ) as BasicProgram
        }

        drawable.vertexPoints[0] = 0f // groundPoint.x - groundPoint.x
        drawable.vertexPoints[1] = 0f // groundPoint.y - groundPoint.y
        drawable.vertexPoints[2] = 0f // groundPoint.z - groundPoint.z

        drawable.vertexPoints[3] = (placePoint.x - groundPoint.x).toFloat()
        drawable.vertexPoints[4] = (placePoint.y - groundPoint.y).toFloat()
        drawable.vertexPoints[5] = (placePoint.z - groundPoint.z).toFloat()

        drawable.mvpMatrix.set(rc.modelviewProjection)
        drawable.mvpMatrix.multiplyByTranslation(groundPoint.x, groundPoint.y, groundPoint.z)

        drawable.lineWidth = activeAttributes.leaderAttributes.outlineWidth
        drawable.enableDepthTest = activeAttributes.leaderAttributes.depthTest
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes.leaderAttributes.outlineColor)

    }

    private fun prepareDrawableLabel(rc: RenderContext,
                                     drawable: DrawableScreenTexture , texture : GpuTexture) {
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program =
                rc.putProgram(
                    BasicProgram.KEY,
                    BasicProgram(rc.resources)
                ) as BasicProgram
        }
        drawable.unitSquareTransform.set(unitSquareTransform)
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes!!.labelAttributes.textColor)
        drawable.texture = texture
        drawable.enableDepthTest = activeAttributes!!.labelAttributes.enableDepthTest
    }

    protected open fun mustDrawLeader(dc: RenderContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false
        return (activeAttributes.drawLeader && !dc.pickMode)
    }

    protected open fun mustDrawLabel(dc: RenderContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false
        return (activeAttributes.drawLabel && !dc.pickMode)
    }


    protected open fun determineActiveTextureBounds(
        rc: RenderContext,
        texture: GpuTexture?
    ): GpuTexture? {
        var tempTexture = texture
        if (activeAttributes!!.imageSource != null) {
            if (tempTexture == null) {
                tempTexture = rc.retrieveTexture(activeAttributes!!.imageSource!!, null)
            }
        }

        val visibilityScale: Double = if (this.eyeDistanceScaling) WWMath.clamp(
            value = this.eyeDistanceScalingThreshold / cameraDistance,
            min = activeAttributes!!.minimumImageScale,
            max = 1.0
        ) else 1.0

        unitSquareTransform.setToIdentity()

        if (tempTexture != null) {
            val w: Int = tempTexture.textureWidth
            val h: Int = tempTexture.textureHeight
            val s = activeAttributes!!.imageScale * visibilityScale
            val offset = activeAttributes!!.imageOffset.offsetForSize(
                w.toDouble(), h.toDouble(),
                offset
            ) // TODO allocation
            unitSquareTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x * s,
                screenPlacePoint.y - offset.y * s,
                screenPlacePoint.z
            )
            unitSquareTransform.multiplyByScale(w * s, h * s, 1.0)
        } else {
            // This branch serves both non-textured attributes and also textures that haven't been loaded yet.
            // We set the size for non-loaded textures to the typical size of a contemporary "small" icon (24px)
            var size: Double = if (activeAttributes!!.imageSource != null) 24.0 else activeAttributes!!.imageScale
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
        return tempTexture
    }

    protected open fun determineActiveLabelBounds(
        rc: RenderContext,
        texture: GpuTexture?
    ): GpuTexture? {

        var textTexture = texture

        if (textTexture == null) {
            textTexture = rc.renderText(label, activeAttributes!!.labelAttributes)
        }

        unitSquareTransform.setToIdentity()

        textTexture.let {

            val w: Int = it.textureWidth
            val h: Int = it.textureHeight

            activeAttributes!!.labelAttributes.textOffset.offsetForSize(
                w.toDouble(),
                h.toDouble(),
                offset
            )

            unitSquareTransform.setTranslation(
                screenPlacePoint.x - offset.x,
                screenPlacePoint.y - offset.y,
                screenPlacePoint.z
            )

            // Apply the label's rotation according to its rotation value and orientation mode. The rotation is applied
            // such that the text rotates around the text offset point.
            val rotation = if ( imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ) rc.camera.heading - imageRotation else -imageRotation
            if (rotation != 0.0) {
                unitSquareTransform.multiplyByTranslation(
                    offset.x,
                    offset.y,
                    0.0
                )
                unitSquareTransform.multiplyByRotation(
                    0.0,
                    0.0,
                    1.0,
                    rotation
                )
                unitSquareTransform.multiplyByTranslation(
                    -offset.x,
                    -offset.y,
                    0.0
                )
            }
            // Apply the label's translation and scale according to its text size.
            unitSquareTransform.multiplyByScale(w.toDouble(), h.toDouble(), 1.0)
        }
        return textTexture
    }


    override fun getReferencePosition(): Position? {
        return position
    }

    override fun moveTo(globe: Globe, position: Position?) {
        this.position = position
    }

    override var highlighted: Boolean = false
}
