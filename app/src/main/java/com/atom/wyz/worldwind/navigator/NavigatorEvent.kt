package com.atom.wyz.worldwind.navigator

import android.view.InputEvent
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.pool.BasicPool
import com.atom.wyz.worldwind.util.pool.Pool

class NavigatorEvent {
    companion object {
        private val pool: Pool<NavigatorEvent> = BasicPool()

        fun obtain(): NavigatorEvent {
            val instance = pool.acquire()
            return instance ?: NavigatorEvent()
        }

        fun obtain(navigator: Navigator?, @WorldWind.NavigatorAction type: Int, lastInputEvent: InputEvent ): NavigatorEvent {
            val instance =
                obtain()
            instance.navigator = navigator
            instance.action = type
            instance.lastInputEvent = lastInputEvent
            return instance
        }
    }

    var navigator: Navigator? = null

    @WorldWind.NavigatorAction
    var action  = WorldWind.NAVIGATOR_MOVED

     var lastInputEvent: InputEvent? = null

    /**
     * Recycle the event, making it available to be re-used.
     */
    fun recycle() {
        navigator = null
        action = WorldWind.NAVIGATOR_MOVED
        lastInputEvent = null
        pool.release(this)
    }
}