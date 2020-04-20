package com.atom.wyz.worldwind

interface NavigatorListener {
    fun onNavigatorEvent(wwd: WorldWindow, event: NavigatorEvent)
}