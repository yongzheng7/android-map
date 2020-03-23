package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileUrlFactory
import com.atom.wyz.worldwind.ogc.WmsGetMapUrlFactory
import com.atom.wyz.worldwind.ogc.WmsLayer
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.util.Logger

class BlueMarbleLandsatLayer : WmsLayer, TileUrlFactory {

    protected var blueMarbleUrlFactory: TileUrlFactory

    protected var landsatUrlFactory: TileUrlFactory
    
    constructor():this("https://worldwind25.arc.nasa.gov/wms")

    constructor(serviceAddress: String?) :super("Blue Marble & Landsat") {
        if (serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleLandsatLayer", "constructor", "missingServiceAddress"))
        }
        val blueMarbleConfig: WmsLayerConfig = WmsLayerConfig()
        blueMarbleConfig.serviceAddress = serviceAddress
        blueMarbleConfig.wmsVersion = "1.3.0"
        blueMarbleConfig.layerNames = "BlueMarble-200405"
        blueMarbleConfig.coordinateSystem = "EPSG:4326"
        blueMarbleConfig.transparent = false // the BlueMarble layer is opaque

        blueMarbleUrlFactory = WmsGetMapUrlFactory(blueMarbleConfig)

        val landsatConfig: WmsLayerConfig = WmsLayerConfig()
        landsatConfig.serviceAddress = serviceAddress
        landsatConfig.wmsVersion = "1.3.0"
        landsatConfig.layerNames = "BlueMarble-200405,esat" // composite the esat layer over the BlueMarble layer

        landsatConfig.coordinateSystem = "EPSG:4326"
        landsatConfig.transparent = false // combining BlueMarble and esat layers results in opaque images

        landsatUrlFactory = WmsGetMapUrlFactory(landsatConfig)

        val metersPerPixel = 15.0
        val radiansPerPixel: Double = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
        val levelsConfig: LevelSetConfig = LevelSetConfig()
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)

        this.levelSet = (LevelSet(levelsConfig))
        this.tileUrlFactory = this
        this.imageFormat = ("image/png")
    }

    override fun urlForTile(tile: Tile?, imageFormat: String?): String? {
        val radiansPerPixel = tile!!.level.texelHeight
        val metersPerPixel: Double = radiansPerPixel * WorldWind.WGS84_SEMI_MAJOR_AXIS

        return if (metersPerPixel < 2.0e3) { // switch to Landsat at 2km resolution
            landsatUrlFactory.urlForTile(tile, imageFormat)
        } else {
            blueMarbleUrlFactory.urlForTile(tile, imageFormat)
        }
    }
}