package com.atom.wyz.worldwind.layer.render.shape

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.core.shader.BasicProgram
import com.atom.wyz.worldwind.core.shader.GpuTexture
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.draw.DrawableLines
import com.atom.wyz.worldwind.layer.draw.DrawableScreenTexture
import com.atom.wyz.worldwind.layer.render.AbstractRenderable
import com.atom.wyz.worldwind.layer.render.RenderContext
import com.atom.wyz.worldwind.layer.render.attribute.TextAttributes
import com.atom.wyz.worldwind.layer.render.pick.PickedObject
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.pool.Pool

open class Label : AbstractRenderable,
    Highlightable,
    Movable {
    companion object {
        const val DEFAULT_DEPTH_OFFSET = -0.1
        var groundPoint = Vec3()
        var placePoint: Vec3 = Vec3()
        var screenPlacePoint: Vec3 = Vec3()
        var offset: Vec2 = Vec2()
        var unitSquareTransform: Matrix4 = Matrix4()
        var screenBounds: Viewport = Viewport()
        var pickedObjectId = 0
        var pickColor: SimpleColor = SimpleColor()
        var cameraDistance = 0.0
    }

    var position = Position()

    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE

    var text: String

    var rotation = 0.0

    @WorldWind.OrientationMode
    var rotationMode = WorldWind.RELATIVE_TO_SCREEN

    private var attributes: TextAttributes

    private var highlightAttributes: TextAttributes? = null

    private var activeAttributes: TextAttributes? = null

    override var highlighted: Boolean = false

    var textTexture: GpuTexture? = null

    constructor(position: Position, text: String) : super(text) {
        this.position.set(position)
        this.text = text
        attributes = TextAttributes()
    }

    constructor(position: Position, text: String, attributes: TextAttributes) : super(text) {
        this.position.set(position)
        this.text = text
        this.attributes = attributes
    }

    override fun doRender(rc: RenderContext) {

        this.text?.also { if (it.isEmpty()) return } ?: let { return }
        // 计算经纬度换算成笛卡尔的坐标
        rc.geographicToCartesian(
            position.latitude,
            position.longitude,
            position.altitude,
            altitudeMode,
            placePoint
        )

        // 计算距离摄像机的长度
        cameraDistance = rc.cameraPoint.distanceTo(placePoint)

        // 计算偏移量
        var depthOffset = 0.0
        if (cameraDistance < rc.horizonDistance) { // 摄像机能够看到
            depthOffset =
                DEFAULT_DEPTH_OFFSET
        }
        // 根据偏移计算出在屏幕上的坐标位置 xy
        if (!rc.projectWithDepth(placePoint, depthOffset, screenPlacePoint)) {
            return  // clipped by the near plane or the far plane
        }

        // 获取活跃的属性
        this.determineActiveAttributes(rc)

        if (activeAttributes == null) {
            return
        }

        // Keep track of the drawable count to determine whether or not this label has enqueued drawables.
        val drawableCount = rc.drawableCount()

        if (rc.pickMode) {
            pickedObjectId = rc.nextPickedObjectId()
            pickColor = PickedObject.identifierToUniqueColor(
                pickedObjectId,
                pickColor
            )
        }

        if (mustDrawLeader(rc)) {
            // Compute the placemark's Cartesian ground point.
            groundPoint = rc.geographicToCartesian(
                position.latitude,
                position.longitude,
                0.0,
                WorldWind.CLAMP_TO_GROUND,
                groundPoint
            )
            if (rc.frustum.intersectsSegment(groundPoint, placePoint)) {
                val pool: Pool<DrawableLines> = rc.getDrawablePool(
                    DrawableLines::class.java
                )
                val drawable = DrawableLines.obtain(pool)
                prepareDrawableLeader(rc, drawable)
                rc.offerShapeDrawable(drawable, cameraDistance)
            }
        }

        if(rc.getText(text, activeAttributes!!).also { textTexture = it } == null){
            if (!rc.frustum.containsPoint(placePoint)) {
                return
            }
        }

        this.determineActiveLabelBounds(rc)

        WWMath.boundingRectForUnitSquare(unitSquareTransform, screenBounds)

        if (rc.frustum.intersectsViewport(screenBounds)) {
            val pool: Pool<DrawableScreenTexture> = rc.getDrawablePool(DrawableScreenTexture::class.java)
            val drawable: DrawableScreenTexture = DrawableScreenTexture.obtain(pool)
            prepareDrawableLabel(rc, drawable)
            // Enqueue a drawable for processing on the OpenGL thread.
            rc.offerShapeDrawable(drawable, cameraDistance)
        }

        textTexture = null

        // Enqueue a picked object that associates the label's drawables with its picked object ID.
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

    private fun <T1, T2> isTwoNotNull(a: T1?, b: T2?, block: (T1, T2) -> Unit) {
        if (a != null && b != null) block(a, b)
    }

    protected open fun mustDrawLeader(dc: RenderContext): Boolean {
        val activeAttributes = this.activeAttributes ?: return false
        return (activeAttributes.drawLeader && !dc.pickMode)
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
        drawable.mvpMatrix.multiplyByTranslation(
            groundPoint.x,
            groundPoint.y,
            groundPoint.z
        )

        drawable.lineWidth = activeAttributes.leaderAttributes!!.outlineWidth
        drawable.enableDepthTest = activeAttributes.leaderAttributes!!.depthTest
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes.leaderAttributes!!.outlineColor)

    }

    protected open fun prepareDrawableLabel(
        rc: RenderContext,
        drawable: DrawableScreenTexture
    ){
        drawable.program = rc.getProgram(BasicProgram.KEY) as BasicProgram?
        if (drawable.program == null) {
            drawable.program =
                rc.putProgram(
                    BasicProgram.KEY,
                    BasicProgram(rc.resources)
                ) as BasicProgram
        }
        drawable.unitSquareTransform.set(unitSquareTransform)
        drawable.color.set(if (rc.pickMode) pickColor else activeAttributes!!.textColor)
        drawable.texture = textTexture
        drawable.enableDepthTest = activeAttributes!!.enableDepthTest

    }

    private fun determineActiveLabelBounds(rc: RenderContext) {
        if (textTexture == null) {
            textTexture = rc.renderText(text, activeAttributes!!)
        }

        textTexture?.let {
            // Initialize the unit square transform to the identity matrix.
            unitSquareTransform.setToIdentity()

            val w: Int = it.textureWidth
            val h: Int = it.textureHeight

            activeAttributes!!.textOffset.offsetForSize(
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
            val rotation = if (rotationMode == WorldWind.RELATIVE_TO_GLOBE) rc.camera.heading - rotation else -rotation
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
    }

    private fun determineActiveAttributes(rc: RenderContext) {
        activeAttributes = if (highlighted && highlightAttributes != null) {
            highlightAttributes
        } else {
            attributes
        }
    }

    override fun getReferencePosition(): Position? {
        return this.position
    }

    override fun moveTo(globe: Globe, position: Position?) {
        this.position.set(position)
    }

}