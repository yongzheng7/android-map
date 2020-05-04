package com.atom.wyz.worldwind.render

import com.atom.wyz.worldwind.WorldWind

class ImageOptions {
    @WorldWind.ImageFormat
    var imageFormat: Int = WorldWind.RGBA_8888

    constructor(imageFormat: Int = WorldWind.RGBA_8888) {
        this.imageFormat = imageFormat
    }
}