package com.atom.map.ogc

import com.atom.map.geom.Sector
import com.atom.map.geom.TileMatrix
import com.atom.map.geom.TileMatrixSet
import com.atom.map.renderable.ImageSource

class BasicElevationCoverage :
    TiledElevationCoverage {

    constructor() : this("https://worldwind26.arc.nasa.gov/elev")
    constructor(serviceAddress: String) : super() {
        val sector = Sector().setFullSphere()
        val matrixWidth = 4 // 4x2 top level matrix equivalent to 90 degree top level tiles

        val matrixHeight = 2
        val tileWidth = 256
        val tileHeight = 256
        val numLevels = 13
        this.tileMatrixSet = (
            TileMatrixSet.fromTilePyramid(
                sector,
                matrixWidth,
                matrixHeight,
                tileWidth,
                tileHeight,
                numLevels
            )
        )

        val layerConfig = WmsLayerConfig()
        layerConfig.serviceAddress = serviceAddress
        layerConfig.layerNames = "GEBCO,aster_v2,USGS-NED"
        layerConfig.imageFormat = "application/bil16"
        val wmsTileFactory = WmsTileFactory(layerConfig)

        this.tileFactory = (object : TileFactory {
            override fun createTileSource(
                tileMatrix: TileMatrix,
                row: Int,
                column: Int
            ): ImageSource {
                val sector = tileMatrix.tileSector(row, column)
                val urlString = wmsTileFactory.urlForTile(sector, tileMatrix.tileWidth, tileMatrix.tileHeight)
                return ImageSource.fromUrl(urlString)
            }
        })

    }

}