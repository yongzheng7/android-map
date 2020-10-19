package com.atom.map.navigator

import com.atom.map.WorldHelper

interface NavigatorListener {

    fun onNavigatorEvent(wwd: WorldHelper, event: NavigatorEvent)

}