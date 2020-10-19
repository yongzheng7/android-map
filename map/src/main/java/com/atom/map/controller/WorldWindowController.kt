package com.atom.map.controller

import android.view.MotionEvent
import com.atom.map.WorldHelper

interface WorldWindowController {

    var world : WorldHelper?

    fun onTouchEvent(event: MotionEvent): Boolean
}