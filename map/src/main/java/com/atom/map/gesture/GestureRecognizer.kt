package com.atom.map.gesture

import android.view.MotionEvent
import com.atom.map.WorldWind
import com.atom.map.util.Logger
import java.util.*

open class GestureRecognizer {
    var enabled = true

    var x = 0f

    var y = 0f

    var startX = 0f

    var startY = 0f

    var translationX = 0f
    fun translationX(value: Float) {
        this.translationX = value
        this.startX = this.x
        this.centroidShiftX = 0f
    }

    var translationY = 0f
    fun translationY(value: Float) {
        this.translationY = value
        this.startY = this.y
        this.centroidShiftY = 0f
    }

    var centroidShiftX = 0f

    var centroidShiftY = 0f

    protected var centroidArray = FloatArray(2)

    protected var listenerList = ArrayList<GestureListener>()

    var lowPassWeight = 0.4f
    @WorldWind.GestureState
    var state: Int = WorldWind.POSSIBLE

    private var stateSequence: Long = 0

    constructor(listener: GestureListener) {
        listenerList.add(listener)
    }

    constructor()

    fun addListener(listener: GestureListener) = this.listenerList.add(listener)
    fun removeListener(listener: GestureListener) = this.listenerList.remove(listener)
    fun getListeners(): ArrayList<GestureListener> = this.listenerList
    protected fun notifyListeners(event: MotionEvent) {
        for (listener in listenerList) {
            listener.gestureStateChanged(event, this)
        }
    }

    /**
     * 重置
     */
    open protected fun reset() {
        this.state = WorldWind.POSSIBLE
        this.stateSequence = 0
        this.x = 0f
        this.y = 0f
        this.startX = 0f
        this.startY = 0f
        this.translationX = 0f
        this.translationY = 0f
        this.centroidShiftX = 0f
        this.centroidShiftY = 0f
    }

    /**
     * 状态变化
     */
    protected fun transitionToState(event: MotionEvent, @WorldWind.GestureState newState: Int) {
        when (newState) {
            WorldWind.POSSIBLE -> {
                state = newState
            }
            WorldWind.FAILED -> {
                state = newState
            }
            WorldWind.RECOGNIZED -> {
                state = newState
                stateSequence++
                this.prepareToRecognize(event)
                notifyListeners(event)
            }
            WorldWind.BEGAN -> {
                state = newState
                stateSequence++
                this.prepareToRecognize(event)
                notifyListeners(event)
            }
            WorldWind.CHANGED -> {
                state = newState
                stateSequence++
                notifyListeners(event)
            }
            WorldWind.CANCELLED -> {
                state = newState
                stateSequence++
                notifyListeners(event)
            }
            WorldWind.ENDED -> {
                state = newState
                stateSequence++
                notifyListeners(event)
            }
        }
    }

    open protected fun prepareToRecognize(event: MotionEvent) {}

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enabled) return false

        val currentStateSequence = this.stateSequence

        try {
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN -> this.handleActionDown(event)
                MotionEvent.ACTION_POINTER_DOWN -> this.handleActionPointerDown(event)
                MotionEvent.ACTION_MOVE -> this.handleActionMove(event)
                MotionEvent.ACTION_CANCEL -> this.handleActionCancel(event)
                MotionEvent.ACTION_POINTER_UP -> this.handleActionPointerUp(event)
                MotionEvent.ACTION_UP -> this.handleActionUp(event)
                else -> {
                    if (Logger.isLoggable(Logger.DEBUG)) {
                        Logger.logMessage(
                            Logger.DEBUG, "GestureRecognizer", "onTouchEvent",
                            "Unrecognized event action \'$action\'"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.logMessage(Logger.ERROR, "GestureRecognizer", "onTouchEvent", "Exception handling event", e)
        }

        return currentStateSequence != stateSequence // stateSequence changes if the event was recognized
    }

    /**
     * 初次点击按下
     */
    protected fun handleActionDown(event: MotionEvent) {
        val index = event.actionIndex
        x = event.getX(index)
        y = event.getY(index)

        startX = x
        startY = y

        translationX = 0f
        translationY = 0f

        centroidShiftX = 0f
        centroidShiftY = 0f
        actionDown(event)
    }

    /**
     * 其次手指按下
     */
    protected fun handleActionPointerDown(event: MotionEvent) {
        centroidChanged(event)
        actionDown(event)
    }

    /**
     * 手指进行移动
     */
    protected fun handleActionMove(event: MotionEvent) {
        eventCentroid(event, centroidArray)


        val dx = centroidArray[0] - startX + centroidShiftX
        val dy = centroidArray[1] - startY + centroidShiftY

        x = centroidArray[0]
        y = centroidArray[1]

        translationX = lowPassFilter(translationX, dx)
        translationY = lowPassFilter(translationY, dy)

        actionMove(event)
    }

    /**
     * 手指取消
     */
    protected fun handleActionCancel(event: MotionEvent) {
        actionCancel(event)
        val state: Int = this.state
        if (state == WorldWind.POSSIBLE ) {
            transitionToState(event, WorldWind.FAILED)
        }else if(state == WorldWind.BEGAN || state == WorldWind.CHANGED){
            transitionToState(event, WorldWind.CANCELLED)
        }
        reset()
    }

    /**
     * 手指抬起
     */
    protected fun handleActionPointerUp(event: MotionEvent) {
        centroidChanged(event)
        actionUp(event)
    }

    /**
     * 最后一个抬起
     */
    protected fun handleActionUp(event: MotionEvent) {
        actionUp(event)
        val state = this.state
        if (state == WorldWind.POSSIBLE) {
            transitionToState(event, WorldWind.FAILED)
        } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
            transitionToState(event, WorldWind.ENDED)
        }
        reset()
    }

    /**
     * 中心位置改变
     */
    protected fun centroidChanged(event: MotionEvent) {
        centroidShiftX += x //第一个手指的xy 保存到中心点
        centroidShiftY += y
        eventCentroid(event, centroidArray)
        x = centroidArray[0]
        y = centroidArray[1]
        centroidShiftX -= centroidArray[0]
        centroidShiftY -= centroidArray[1]
    }

    /**
     * 计算多指触控重心位置
     */
    protected fun eventCentroid(event: MotionEvent, result: FloatArray) {
        val index = event.actionIndex //获取手指索引
        val action = event.actionMasked //获取对应状态
        var x = 0f
        var y = 0f
        var count = 0f

        for (idx in 0 until event.pointerCount) {
            if (idx == index && action == MotionEvent.ACTION_POINTER_UP) {
                continue  // suppress coordinates from pointers that are no longer down
            }
            x += event.getX(idx)
            y += event.getY(idx)
            count++
        }
        result[0] = x / count
        result[1] = y / count
    }

    /**
     * 进行权重判断 降低林敏度
     */
    protected fun lowPassFilter(value: Float, newValue: Float): Float {
        val w = lowPassWeight
        return value * (1 - w) + newValue * w
    }

    open protected fun actionDown(event: MotionEvent) {}

    open protected fun actionMove(event: MotionEvent) {}

    open protected fun actionCancel(event: MotionEvent) {}

    open protected fun actionUp(event: MotionEvent) {}
}