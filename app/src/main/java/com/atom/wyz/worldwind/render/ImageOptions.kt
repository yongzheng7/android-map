package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.WorldWind

class ImageOptions(@WorldWind.ImageFormat var imageFormat: Int = WorldWind.RGBA_8888) {

    @WorldWind.ResamplingMode
    var resamplingMode: Int = WorldWind.BILINEAR

    @WorldWind.WrapMode
    var wrapMode: Int = WorldWind.CLAMP

}