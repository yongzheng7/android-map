package com.atom.map.gesture

import android.view.MotionEvent
import com.atom.map.WorldWind
import com.atom.map.util.WWMath

class RotationRecognizer : GestureRecognizer {

    protected var rotation = 0f

    protected var rotationOffset = 0f

    protected var referenceAngle = 0f

    protected var interpretAngle = 20f

    protected var pointerIds = IntArray(2)

    protected var pointerIdCount = 0

    constructor(listener: GestureListener) : super(listener)
    constructor() : super()

    fun rotation(): Float {
        return WWMath.normalizeAngle180((rotation + rotationOffset).toDouble()).toFloat()
    }

    override fun reset() {
        super.reset()
        rotation = 0f
        rotationOffset = 0f
        referenceAngle = 0f
        pointerIdCount = 0
    }

    override fun actionDown(event: MotionEvent) {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        if (pointerIdCount < 2) {
            pointerIds[pointerIdCount++] = pointerId // add it to the pointer ID array
            if (pointerIdCount == 2) {
                referenceAngle = currentTouchAngle(event)
                rotationOffset += rotation
                rotation = 0f
            }
        }
    }

    override fun actionMove(event: MotionEvent) {
        if (pointerIdCount == 2) {
            if (state == WorldWind.POSSIBLE) {
                if (shouldRecognize(event)) {
                    transitionToState(event, WorldWind.BEGAN)
                }
            } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
                val angle = currentTouchAngle(event)
                val newRotation = WWMath.normalizeAngle180((angle - referenceAngle).toDouble()).toFloat()
                rotation = lowPassFilter(rotation, newRotation)
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

    override fun prepareToRecognize(event: MotionEvent) {
        referenceAngle = currentTouchAngle(event)
        rotation = 0f
    }

    protected fun shouldRecognize(event: MotionEvent): Boolean {
        if (event.pointerCount != 2) {
            return false // require exactly two pointers to recognize the gesture
        }
        val angle = currentTouchAngle(event)
        val rotation = WWMath.normalizeAngle180((angle - referenceAngle).toDouble())
        return Math.abs(rotation) > interpretAngle
    }

    protected fun currentTouchAngle(event: MotionEvent): Float {
        val index0 = event.findPointerIndex(pointerIds[0])
        val index1 = event.findPointerIndex(pointerIds[1])
        val dx = event.getX(index0) - event.getX(index1)
        val dy = event.getY(index0) - event.getY(index1)
        val rad = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
        return Math.toDegrees(rad.toDouble()).toFloat()
    }
}