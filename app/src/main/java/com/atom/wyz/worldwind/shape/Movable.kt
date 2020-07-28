package com.atom.wyz.worldwind.shape

import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.globe.Globe

interface Movable {

    fun getReferencePosition(): Position?

    fun moveTo(globe: Globe, position: Position?)

}