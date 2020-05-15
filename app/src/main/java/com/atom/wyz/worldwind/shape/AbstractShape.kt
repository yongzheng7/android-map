package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.BoundingBox
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.render.AbstractRenderable

open class AbstractShape  : AbstractRenderable , Attributable, Highlightable {
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

    protected var pickedObjectId = 0

    protected var pickColor: Color = Color()

    protected val boundingBox: BoundingBox

    constructor(attributes: ShapeAttributes = ShapeAttributes()) : super("AbstractShape") {
        this.attributes = attributes
        boundingBox = BoundingBox()
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

    protected open fun reset() {}

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

    protected open fun makeDrawable(rc: RenderContext) {}

    override fun isHighlighted(): Boolean {
        return _highlighted  ;
    }

    override fun setHighlighted(highlighted: Boolean) {
        _highlighted = highlighted
    }
}