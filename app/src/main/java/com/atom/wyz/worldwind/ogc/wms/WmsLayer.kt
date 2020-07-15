package com.atom.wyz.worldwind.ogc.wms

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.layer.TiledSurfaceImage
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger

open class WmsLayer : RenderableLayer {

    init {
        this.displayName = ("WMS Layer")
        this.pickEnabled = (false)
        this.addRenderable(TiledSurfaceImage())
    }

    constructor(diaplayername : String = "WMS Layer" ) : super(diaplayername)
    constructor(sector: Sector, metersPerPixel: Double, config: WmsLayerConfig) : super("WMS Layer") {
        if (metersPerPixel <= 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"))
        }
        this.setConfiguration(sector, metersPerPixel, config)
    }
    constructor(sector: Sector, globe: Globe, metersPerPixel: Double, config: WmsLayerConfig): super("WMS Layer") {
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"))
        }
        this.setConfiguration(sector, globe ,metersPerPixel, config)
    }

    open fun setConfiguration(sector: Sector, metersPerPixel: Double, config: WmsLayerConfig) {
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"))
        }
        val radiansPerPixel: Double = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
        val levelsConfig = LevelSetConfig()
        levelsConfig.sector?.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        val surfaceImage = getRenderable(0) as TiledSurfaceImage?
        surfaceImage?.levelSet = (LevelSet(levelsConfig))
        surfaceImage?.tileFactory = (WmsTileFactory(
            config
        ))

    }

    open fun setConfiguration(sector: Sector, globe: Globe, metersPerPixel: Double, config: WmsLayerConfig) {
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"))
        }

        val radiansPerPixel: Double = metersPerPixel / globe.equatorialRadius
        val levelsConfig: LevelSetConfig = LevelSetConfig()
        levelsConfig.sector.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        val surfaceImage = getRenderable(0) as TiledSurfaceImage?
        surfaceImage?.levelSet = (LevelSet(levelsConfig))
        surfaceImage?.tileFactory = (WmsTileFactory(
            config
        ))
    }
}