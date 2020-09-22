package com.atom.wyz.worldwind.navigator

import com.atom.wyz.worldwind.WorldHelper

interface NavigatorListener {

    fun onNavigatorEvent(wwd: WorldHelper, event: NavigatorEvent)

}