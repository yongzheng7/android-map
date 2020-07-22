package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.render.AbstractRenderable
import com.atom.wyz.worldwind.render.GpuTexture
import com.atom.wyz.worldwind.util.WWMath

abstract class AbstractShape : AbstractRenderable, Attributable, Highlightable {
    companion object {
        const val NEAR_ZERO_THRESHOLD = 1.0e-10
    }

    override var attributes: ShapeAttributes? = null

    override var highlightAttributes: ShapeAttributes? = null

    protected var activeAttributes: ShapeAttributes? = null


    @WorldWind.AltitudeMode
    var altitudeMode: Int = WorldWind.ABSOLUTE
        set(value) {
            field = value
            this.reset()
        }

    @WorldWind.PathType
    var pathType: Int = WorldWind.GREAT_CIRCLE
        set(value) {
            field = value
            this.reset()
        }
    var maximumIntermediatePoints = 10

    var pickedObjectId = 0

    var pickColor: Color = Color()

    var boundingSector: Sector = Sector()

    val boundingBox: BoundingBox = BoundingBox()

    private val scratchPoint: Vec3 = Vec3()

    override var highlighted: Boolean = false

    constructor(attributes: ShapeAttributes = ShapeAttributes()) : super("AbstractShape") {
        this.attributes = attributes
    }

    override fun doRender(rc: RenderContext) {
        // Don't render anything if the shape is not visible.
        if (!this.intersectsFrustum(rc)) {
            return
        }

        // Select the currently active attributes. Don't render anything if the attributes are unspecified.
        this.determineActiveAttributes(rc)
        if (activeAttributes == null) {
            return
        }

        // Keep track of the drawable count to determine whether or not this shape has enqueued drawables.
        val drawableCount = rc.drawableCount()
        if (rc.pickMode) {
            pickedObjectId = rc.nextPickedObjectId()
            pickColor = PickedObject.identifierToUniqueColor(pickedObjectId, pickColor)
        }

        // Enqueue drawables for processing on the OpenGL thread.
        this.makeDrawable(rc)

        // Enqueue a picked object that associates the shape's drawables with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(pickedObjectId, this, rc.currentLayer!!))
        }
    }


    protected fun intersectsFrustum(rc: RenderContext): Boolean {
        return boundingBox.isUnitBox() || boundingBox.intersectsFrustum(rc.frustum)
    }

    protected fun determineActiveAttributes(rc: RenderContext) {
        if (this.highlighted && highlightAttributes != null) {
            activeAttributes = highlightAttributes
        } else {
            activeAttributes = attributes
        }
    }

    protected abstract fun makeDrawable(rc: RenderContext)
    protected abstract fun reset()

    protected open fun cameraDistanceGeographic(rc: RenderContext, boundingSector: Sector): Double {
        val lat: Double = WWMath.clamp(rc.camera.latitude, boundingSector.minLatitude, boundingSector.maxLatitude)
        val lon: Double = WWMath.clamp(rc.camera.longitude, boundingSector.minLongitude, boundingSector.maxLongitude)
        val point = rc.geographicToCartesian(lat, lon, 0.0, WorldWind.CLAMP_TO_GROUND, scratchPoint)
        return point.distanceTo(rc.cameraPoint)
    }

    protected open fun computeRepeatingTexCoordTransform(
        texture: GpuTexture,
        metersPerPixel: Double,
        result: Matrix3
    ): Matrix3 {
        val texCoordMatrix: Matrix3 = result.setToIdentity()
        texCoordMatrix.setScale(1.0 / (texture.textureWidth* metersPerPixel), 1.0 / (texture.textureHeight * metersPerPixel))
        texCoordMatrix.multiplyByMatrix(texture.texCoordTransform)
        return texCoordMatrix
    }

    protected open fun cameraDistanceCartesian(
        rc: RenderContext,
        array: FloatArray,
        count: Int,
        stride: Int,
        offset: Vec3
    ): Double {
        val cx = rc.cameraPoint.x - offset.x
        val cy = rc.cameraPoint.y - offset.y
        val cz = rc.cameraPoint.z - offset.z
        var minDistance2 = Double.POSITIVE_INFINITY
        var idx = 0
        while (idx < count) {
            val px = array[idx].toDouble()
            val py = array[idx + 1].toDouble()
            val pz = array[idx + 2].toDouble()
            val dx = px - cx
            val dy = py - cy
            val dz = pz - cz
            val distance2 = dx * dx + dy * dy + dz * dz
            if (minDistance2 > distance2) {
                minDistance2 = distance2
            }
            idx += stride
        }
        return Math.sqrt(minDistance2)
    }
}