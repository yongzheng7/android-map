package com.atom.map.gesture

import android.view.MotionEvent
import com.atom.map.WorldWind

/**
 * 手势识别器实现，用于检测触摸平移手势。
 */
class PanRecognizer : GestureRecognizer {

    var minNumberOfPointers = 1

    var maxNumberOfPointers = Int.MAX_VALUE

    var interpretDistance = 20f

    constructor(listener: GestureListener) : super(listener)
    constructor() : super()

    override  fun prepareToRecognize(event: MotionEvent) { // set translation to zero when the pan begins
        this.translationX(0f)
        this.translationY(0f)
    }

    protected fun shouldInterpret(event: MotionEvent): Boolean {
        val dx: Float = this.translationX
        val dy: Float = this.translationY
        val distance = Math.sqrt(dx * dx + dy * dy.toDouble()).toFloat()
        return distance > interpretDistance
    }

    protected fun shouldRecognize(event: MotionEvent): Boolean {
        val count = event.pointerCount
        return count != 0 && count >= minNumberOfPointers && count <= maxNumberOfPointers
    }

    override fun actionMove(event: MotionEvent) {
        if (state == WorldWind.POSSIBLE) {
            if (shouldInterpret(event)) {
                if (shouldRecognize(event)) {
                    transitionToState(event, WorldWind.BEGAN)
                } else {
                    transitionToState(event, WorldWind.FAILED)
                }
            }
        } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
            transitionToState(event, WorldWind.CHANGED)
        }
    }
}