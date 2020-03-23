package com.atom.wyz.worldwind.gesture

import android.view.MotionEvent
import com.atom.wyz.worldwind.WorldWind

class PinchRecognizer : GestureRecognizer {

    protected var scale = 1f

    protected var scaleOffset = 1f

    protected var referenceDistance = 0f

    protected var interpretDistance = 20f

    protected var pointerIds = IntArray(2)

    protected var pointerIdCount = 0

    constructor(listener: GestureListener) : super(listener)
    constructor() : super()

    fun scale(): Float {
        return scale * scaleOffset
    }

    override fun reset() {
        super.reset()
        scale = 1f
        scaleOffset = 1f
        referenceDistance = 0f
        pointerIdCount = 0
    }

    override fun actionDown(event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        if (pointerIdCount < 2) {
            pointerIds[pointerIdCount++] = pointerId // add it to the pointer ID array
            if (pointerIdCount == 2) {
                referenceDistance = this.currentPinchDistance(event)
                scaleOffset *= scale
                scale = 1f
            }
        }
    }

    override fun actionMove(event: MotionEvent) {
        if (pointerIdCount == 2) {
            if (state == WorldWind.POSSIBLE) {
                if (this.shouldRecognize(event)) {
                    transitionToState(event, WorldWind.BEGAN)
                }
            } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
                val distance: Float = this.currentPinchDistance(event)
                val newScale = Math.abs(distance / referenceDistance)
                scale = lowPassFilter(scale, newScale)
                transitionToState(event, WorldWind.CHANGED)
            }
        }
    }

    override fun actionUp(event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        if (pointerIds[0] == pointerId) { // remove the first pointer ID
            pointerIds[0] = pointerIds[1]
            pointerIdCount--
        } else if (pointerIds[1] == pointerId) { // remove the second pointer ID
            pointerIdCount--
        }
    }


    protected fun currentPinchDistance(event: MotionEvent): Float {
        val index0 = event.findPointerIndex(pointerIds[0])
        val index1 = event.findPointerIndex(pointerIds[1])
        val dx = event.getX(index0) - event.getX(index1)
        val dy = event.getY(index0) - event.getY(index1)
        return Math.sqrt(dx * dx + dy * dy.toDouble()).toFloat()
    }

    protected fun shouldRecognize(event: MotionEvent): Boolean {
        if (event.pointerCount != 2) return false
        val distance = currentPinchDistance(event).toDouble()
        return Math.abs(distance - referenceDistance) > interpretDistance
    }

    override protected fun prepareToRecognize(event: MotionEvent) {
        referenceDistance = currentPinchDistance(event)
        scale = 1f
    }
}