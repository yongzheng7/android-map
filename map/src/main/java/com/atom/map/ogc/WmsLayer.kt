package com.atom.map.ogc

import com.atom.map.WorldWind
import com.atom.map.geom.Sector
import com.atom.map.globe.Globe
import com.atom.map.layer.RenderableLayer
import com.atom.map.renderable.TiledSurfaceImage
import com.atom.map.util.LevelSet
import com.atom.map.util.LevelSetConfig
import com.atom.map.util.Logger

open class WmsLayer : RenderableLayer{

    constructor(name : String = "WMS Layer"): super(name)
    constructor(
        sector: Sector,
        metersPerPixel: Double,
        config: WmsLayerConfig
    )  : super("WMS Layer")
    {
        if (metersPerPixel <= 0) {
            throw IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution")
            )
        }
        this.setConfiguration(sector, metersPerPixel, config)
    }

    fun WmsLayer(
        sector: Sector,
        globe: Globe,
        metersPerPixel: Double,
        config: WmsLayerConfig
    ) {
        require(metersPerPixel > 0) {
            Logger.logMessage(
                Logger.ERROR,
                "WmsLayer",
                "constructor",
                "invalidResolution"
            )
        }
        this.setConfiguration(sector, globe, metersPerPixel, config)
    }

    init {
        this.displayName = ("WMS Layer")
        this.pickEnabled = (false)
        this.addRenderable(TiledSurfaceImage())
    }

    fun setConfiguration(
        sector: Sector,
        metersPerPixel: Double,
        config: WmsLayerConfig
    ) {
        require(metersPerPixel > 0) {
            Logger.logMessage(
                Logger.ERROR,
                "WmsLayer",
                "setConfiguration",
                "invalidResolution"
            )
        }
        val radiansPerPixel: Double = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS
        val levelsConfig = LevelSetConfig()
        levelsConfig.sector.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        val surfaceImage = getRenderable(0) as TiledSurfaceImage
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (WmsTileFactory(config))
    }

    /**
     * Specifies this Web Map Service (WMS) layer's configuration. The configuration must specify the following values:
     * service address, WMS protocol version, layer names, coordinate reference system, sector and resolution. All other
     * WMS configuration values may be unspecified, in which case a default value is used.
     *
     * @param sector         the geographic region in which to display the WMS layer
     * @param metersPerPixel the desired resolution in meters on the specified globe
     * @param config         the WMS layer configuration values
     */
    fun setConfiguration(
        sector: Sector,
        globe: Globe,
        metersPerPixel: Double,
        config: WmsLayerConfig
    ) {
        require(metersPerPixel > 0) {
            Logger.logMessage(
                Logger.ERROR,
                "WmsLayer",
                "setConfiguration",
                "invalidResolution"
            )
        }
        val radiansPerPixel: Double = metersPerPixel / globe.getEquatorialRadius()
        val levelsConfig = LevelSetConfig()
        levelsConfig.sector.set(sector)
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel)
        val surfaceImage = getRenderable(0) as TiledSurfaceImage
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (WmsTileFactory(config))
    }
}