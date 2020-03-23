package com.atom.wyz.worldwind.gesture

import android.view.MotionEvent
import com.atom.wyz.worldwind.util.Logger
import java.util.*

class GestureGroup() {
    protected var recognizerList = ArrayList<GestureRecognizer>()

    fun addRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GestureGroup", "addRecognizer", "missingRecognizer"))
        }
        recognizerList.add(recognizer)
    }

    fun removeRecognizer(recognizer: GestureRecognizer?) {
        if (recognizer == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "GestureGroup", "removeRecognizer", "missingRecognizer"))
        }
        recognizerList.remove(recognizer)
    }

    fun getRecognizers(): List<GestureRecognizer>? {
        return recognizerList
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        for (recognizer in recognizerList) {
            recognizer.onTouchEvent(event)
        }
        return true // TODO
    }
}