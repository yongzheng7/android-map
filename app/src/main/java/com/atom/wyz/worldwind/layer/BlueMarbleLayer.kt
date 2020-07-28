package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.WmsLayer
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.render.TiledSurfaceImage

class BlueMarbleLayer : WmsLayer {

    constructor() : this("https://worldwind25.arc.nasa.gov/wms")

    constructor(serviceAddress: String) : super("Blue Marble"){
        val config: WmsLayerConfig = WmsLayerConfig()
        config.serviceAddress = serviceAddress
        config.wmsVersion = "1.3.0"
        config.layerNames = "BlueMarble-200405"
        config.coordinateSystem = "EPSG:4326"
        config.transparent = false // the BlueMarble layer is opaque
        setConfiguration(Sector().setFullSphere(), 500.0, config)
        val surfaceImage = getRenderable(0) as TiledSurfaceImage?
        surfaceImage?.imageOptions = (ImageOptions(WorldWind.RGB_565)) // exploit opaque imagery to reduce memory usage
    }
}