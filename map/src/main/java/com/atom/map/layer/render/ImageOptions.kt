package com.atom.map.layer.render

import com.atom.map.WorldWind

class ImageOptions(@WorldWind.ImageFormat var imageFormat: Int = WorldWind.RGBA_8888) {

    @WorldWind.ResamplingMode
    var resamplingMode: Int = WorldWind.BILINEAR

    @WorldWind.WrapMode
    var wrapMode: Int = WorldWind.CLAMP

}