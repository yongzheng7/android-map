package com.atom.wyz.worldwind.controller

import android.view.MotionEvent
import com.atom.wyz.worldwind.WorldHelper

interface WorldWindowController {

    var world : WorldHelper?

    fun onTouchEvent(event: MotionEvent): Boolean
}