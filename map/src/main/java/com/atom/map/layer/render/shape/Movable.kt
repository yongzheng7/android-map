package com.atom.map.layer.render.shape

import com.atom.map.geom.Position
import com.atom.map.globe.Globe

interface Movable {

    fun getReferencePosition(): Position?

    fun moveTo(globe: Globe, position: Position?)

}