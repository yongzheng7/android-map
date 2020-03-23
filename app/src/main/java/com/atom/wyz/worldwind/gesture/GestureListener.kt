package com.atom.wyz.worldwind.gesture

import android.view.MotionEvent

interface GestureListener {
    fun gestureStateChanged(event: MotionEvent, recognizer: GestureRecognizer)
}