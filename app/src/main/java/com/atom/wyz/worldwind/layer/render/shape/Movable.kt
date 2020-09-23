package com.atom.wyz.worldwind.layer.render.shape

import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.globe.Globe

interface Movable {

    fun getReferencePosition(): Position?

    fun moveTo(globe: Globe, position: Position?)

}