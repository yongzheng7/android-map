package com.atom.wyz.worldwind.ogc

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.TiledImageLayer
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.LevelSetConfig
import com.atom.wyz.worldwind.util.Logger

open class WmsLayer : TiledImageLayer {
    constructor(diaplayername : String = "WMS Layer" ) : super(diaplayername) {
    }

    constructor(sector: Sector?, metersPerPixel: Double, config: WmsLayerConfig?) : super("WMS Layer") {

        if (sector == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingSector"))
        }
        if (metersPerPixel <= 0) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"))
        }
        if (config == null) {
            throw IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingConfig"))
        }
        this.setConfiguration(sector, metersPerPixel, config)
    }

    constructor(sector: Sector?, globe: Globe?, metersPerPixel: Double, config: WmsLayerConfig?) {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingSector"))
        }
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingGlobe"))
        }
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"))
        }
        if (config == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingConfig"))
        }
        this.setConfiguration(sector, globe ,metersPerPixel, config)
    }

    open fun setConfiguration(sector: Sector?, metersPerPixel: Double, config: WmsLayerConfig?) {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingSector"))
        }
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"))
        }
        if (config == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingConfig"))
        }
        val radiansPerPixel: Double = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
        val levelsConfig = LevelSetConfig()
        levelsConfig.sector?.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        this.levelSet = (LevelSet(levelsConfig))
        this.tileFactory = (WmsTileFactory(config))
    }

    open fun setConfiguration(sector: Sector?, globe: Globe?, metersPerPixel: Double, config: WmsLayerConfig?) {
        if (sector == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingSector"))
        }
        if (globe == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingGlobe"))
        }
        if (metersPerPixel <= 0) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"))
        }
        if (config == null) {
            throw java.lang.IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingConfig"))
        }
        val radiansPerPixel: Double = metersPerPixel / globe.equatorialRadius
        val levelsConfig: LevelSetConfig = LevelSetConfig()
        levelsConfig.sector?.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        this.levelSet = (LevelSet(levelsConfig))
        this.tileFactory = (WmsTileFactory(config))
    }
}