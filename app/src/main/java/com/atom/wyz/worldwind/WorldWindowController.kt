package com.atom.wyz.worldwind

import android.view.MotionEvent

interface WorldWindowController {
    var worldWindow: WorldWindow?

    fun onTouchEvent(event: MotionEvent): Boolean
}