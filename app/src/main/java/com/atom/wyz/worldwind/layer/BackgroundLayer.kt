package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.SurfaceImage
import com.atom.wyz.worldwind.util.Logger

class BackgroundLayer : RenderableLayer {

    constructor() :this(ImageSource.fromResource(R.drawable.gov_nasa_worldwind_worldtopobathy2004053)
        , ImageOptions(WorldWind.RGB_565))

    constructor(imageSource: ImageSource? , imageOptions : ImageOptions?) {
        if (imageSource == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleBackgroundLayer", "constructor", "missingUrl"))
        }
        this.displayName = ("Background")
        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.pickEnabled = (false)
        // Delegate display to the SurfaceImage shape.
        this.addRenderable(SurfaceImage(Sector().setFullSphere(), imageSource).apply { this.imageOptions = imageOptions })
    }

}