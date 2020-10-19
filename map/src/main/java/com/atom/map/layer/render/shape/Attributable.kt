package com.atom.map.layer.render.shape

import com.atom.map.layer.render.attribute.ShapeAttributes

interface Attributable {

    var attributes : ShapeAttributes?

    var highlightAttributes : ShapeAttributes?

}