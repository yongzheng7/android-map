package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.BoundingBox
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.render.AbstractRenderable

abstract class AbstractShape : AbstractRenderable, Attributable, Highlightable {
    companion object {
        const val NEAR_ZERO_THRESHOLD = 1.0e-10
    }

    override var attributes: ShapeAttributes? = null

    override var highlightAttributes: ShapeAttributes? = null

    protected var activeAttributes: ShapeAttributes? = null

    var _highlighted = false

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
        if (this._highlighted && highlightAttributes != null) {
            activeAttributes = highlightAttributes
        } else {
            activeAttributes = attributes
        }
    }

    protected abstract fun makeDrawable(rc: RenderContext)
    protected abstract fun reset()

    override fun isHighlighted(): Boolean {
        return _highlighted;
    }

    override fun setHighlighted(highlighted: Boolean) {
        _highlighted = highlighted
    }
}