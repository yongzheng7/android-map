package com.atom.map.layer

import com.atom.map.WorldWind
import com.atom.map.core.tile.Tile
import com.atom.map.core.tile.TileFactory
import com.atom.map.geom.Sector
import com.atom.map.layer.render.ImageOptions
import com.atom.map.layer.render.TiledSurfaceImage
import com.atom.map.ogc.WmsLayerConfig
import com.atom.map.ogc.WmsTileFactory
import com.atom.map.util.Level
import com.atom.map.util.LevelSet
import com.atom.map.util.LevelSetConfig
import com.atom.map.util.Logger

class BlueMarbleLandsatLayer : RenderableLayer,
    TileFactory {
    lateinit var surfaceImage: TiledSurfaceImage

    lateinit var blueMarbleUrlFactory: TileFactory

    lateinit var landsatUrlFactory: TileFactory
    
    constructor():this("https://worldwind25.arc.nasa.gov/wms")

    constructor(serviceAddress: String?) :super("Blue Marble & Landsat") {
        if (serviceAddress == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "BlueMarbleLandsatLayer", "constructor", "missingServiceAddress"))
        }
        val blueMarbleConfig = WmsLayerConfig()
        blueMarbleConfig.serviceAddress = serviceAddress
        blueMarbleConfig.wmsVersion = "1.3.0"
        blueMarbleConfig.layerNames = "BlueMarble-200405"
        blueMarbleConfig.coordinateSystem = "EPSG:4326"
        blueMarbleConfig.transparent = false // the BlueMarble layer is opaque

        blueMarbleUrlFactory =
            WmsTileFactory(blueMarbleConfig)

        val landsatConfig  = WmsLayerConfig()
        landsatConfig.serviceAddress = serviceAddress
        landsatConfig.wmsVersion = "1.3.0"
        landsatConfig.layerNames = "BlueMarble-200405,esat" // composite the esat layer over the BlueMarble layer

        landsatConfig.coordinateSystem = "EPSG:4326"
        landsatConfig.transparent = false // combining BlueMarble and esat layers results in opaque images

        landsatUrlFactory =
            WmsTileFactory(landsatConfig)

        val metersPerPixel = 15.0
        val radiansPerPixel: Double = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
        val levelsConfig = LevelSetConfig()
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)

        this.pickEnabled = (false)
        val surfaceImage =
            TiledSurfaceImage()
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (this)
        surfaceImage.imageOptions = (ImageOptions(
            WorldWind.RGB_565
        )) // reduce memory usage by using a 16-bit configuration with no alpha
        this.addRenderable(surfaceImage)

    }

    override fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile {
        val radiansPerPixel: Double = Math.toRadians(level!!.tileDelta) / level.tileHeight
        val metersPerPixel = radiansPerPixel * WorldWind.WGS84_SEMI_MAJOR_AXIS
        return if (metersPerPixel < 2.0e3) { // switch to Landsat at 2km resolution
            this.landsatUrlFactory.createTile(sector, level, row, column)
        } else {
            this.blueMarbleUrlFactory.createTile(sector, level, row, column)
        }
    }

}