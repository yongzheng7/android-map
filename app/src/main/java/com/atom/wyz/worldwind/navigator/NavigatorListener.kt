package com.atom.wyz.worldwind.navigator

import com.atom.wyz.worldwind.WorldWindow

interface NavigatorListener {

    fun onNavigatorEvent(wwd: WorldWindow, event: NavigatorEvent)

}