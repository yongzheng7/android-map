package com.atom.wyz.worldwind.controller

import android.view.MotionEvent
import com.atom.wyz.worldwind.WorldWindow

interface WorldWindowController {
    var worldWindow: WorldWindow?

    fun onTouchEvent(event: MotionEvent): Boolean
}