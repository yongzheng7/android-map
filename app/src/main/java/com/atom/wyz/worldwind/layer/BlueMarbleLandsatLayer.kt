package com.atom.wyz.worldwind.layer

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Tile
import com.atom.wyz.worldwind.globe.TileFactory
import com.atom.wyz.worldwind.ogc.WmsLayerConfig
import com.atom.wyz.worldwind.ogc.WmsTileFactory
import com.atom.wyz.worldwind.render.ImageOptions
import com.atom.wyz.worldwind.util.Level
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger

class BlueMarbleLandsatLayer : RenderableLayer, TileFactory {
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
        val surfaceImage = TiledSurfaceImage()
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (this)
        surfaceImage.imageOptions = (ImageOptions(WorldWind.RGB_565)) // reduce memory usage by using a 16-bit configuration with no alpha
        this.addRenderable(surfaceImage)

    }

    override fun createTile(sector: Sector?, level: Level?, row: Int, column: Int): Tile {
        val radiansPerPixel: Double = level!!.texelHeight
        val metersPerPixel = radiansPerPixel * WorldWind.WGS84_SEMI_MAJOR_AXIS
        return if (metersPerPixel < 2.0e3) { // switch to Landsat at 2km resolution
            this.landsatUrlFactory.createTile(sector, level, row, column)
        } else {
            this.blueMarbleUrlFactory.createTile(sector, level, row, column)
        }
    }

}