package com.atom.wyz.worldwind

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.InputEvent
import android.view.MotionEvent
import com.atom.wyz.worldwind.geom.Matrix4
import com.atom.wyz.worldwind.util.Logger
import java.util.*
import java.util.concurrent.TimeUnit

class NavigatorEventSupport(wwd: WorldWindow) {
    protected var wwd = wwd

    protected var listeners = ArrayList<NavigatorListener>()

     var stoppedEventDelay: Long = 250

    protected var lastModelview: Matrix4? = null

    protected var lastTouchEvent: MotionEvent? = null

    protected var stopTouchEvent: MotionEvent? = null

    protected var stopHandler =
        Handler(Looper.getMainLooper(), Handler.Callback {
            onNavigatorStopped()
            false
        })


    fun reset() {
        lastModelview = null
        stopHandler.removeMessages(0 /*what*/)
        lastTouchEvent?.recycle()
        lastTouchEvent = null
        stopTouchEvent?.recycle()
        stopTouchEvent = null
    }

    fun addNavigatorListener(listener: NavigatorListener?) {
        if (listener == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "NavigatorEventSupport", "addNavigatorListener", "missingListener")
            )
        }
        listeners.add(listener)
    }

    fun removeNavigatorListener(listener: NavigatorListener?) {
        if (listener == null) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "NavigatorEventSupport", "removeNavigatorListener", "missingListener")
            )
        }
        listeners.remove(listener)
    }

    fun setNavigatorStoppedDelay(delay: Long, unit: TimeUnit) {
        stoppedEventDelay = unit.toMillis(delay)
    }

    @SuppressLint("Recycle")
    fun onTouchEvent(event: MotionEvent?) {
        if (listeners.isEmpty()) {
            return  // no listeners to notify; ignore the event
        }
        if (lastModelview == null) {
            return  // no frame rendered yet; ignore the event
        }
        if (lastTouchEvent != null) {
            lastTouchEvent!!.recycle()
        }
        lastTouchEvent = MotionEvent.obtain(event)
    }

    fun onFrameRendered(rc: RenderContext) {
        if (listeners.isEmpty()) {
            return  // no listeners to notify; ignore the event
        }
        if (lastModelview == null) { // this is the first frame; copy the frame's modelview
            lastModelview = Matrix4(rc.modelview)
        } else if (!lastModelview!!.equals(rc.modelview)) { // the frame's modelview has changed
            lastModelview!!.set(rc.modelview)
            // Notify the listeners of a navigator moved event.
            onNavigatorMoved()
            // Schedule a navigator stopped event after a specified delay in milliseconds.
            stopHandler.removeMessages(0 /*what*/)
            stopHandler.sendEmptyMessageDelayed(0 /*what*/, stoppedEventDelay)
        }
    }

    protected fun onNavigatorMoved() {
        notifyListeners(WorldWind.NAVIGATOR_MOVED, lastTouchEvent)
        if (lastTouchEvent != null) {
            if (stopTouchEvent != null) {
                stopTouchEvent!!.recycle()
            }
            stopTouchEvent = lastTouchEvent
            lastTouchEvent = null
        }
    }

    protected fun onNavigatorStopped() {
        notifyListeners(WorldWind.NAVIGATOR_STOPPED, stopTouchEvent)
        if (stopTouchEvent != null) {
            stopTouchEvent!!.recycle()
            stopTouchEvent = null
        }
    }

    protected fun notifyListeners(action: Int, inputEvent: InputEvent?) {
        val event = NavigatorEvent.obtain(wwd.navigator, action, inputEvent!!)
        var idx = 0
        val len = listeners.size
        while (idx < len) {
            listeners[idx].onNavigatorEvent(wwd, event)
            idx++
        }
        event.recycle()
    }

}