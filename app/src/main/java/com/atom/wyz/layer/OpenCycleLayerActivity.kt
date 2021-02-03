package com.atom.wyz.layer

import android.util.Log
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.core.tile.ImageTile
import com.atom.map.core.tile.Tile
import com.atom.map.core.tile.TileFactory
import com.atom.map.geom.Sector
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.map.renderable.ImageOptions
import com.atom.map.renderable.ImageSource
import com.atom.map.renderable.TiledSurfaceImage
import com.atom.map.util.Level
import com.atom.map.util.LevelSet
import com.atom.map.util.LevelSetConfig
import com.atom.wyz.base.BasicGlobeActivity
import java.io.File

/**
 * 谷歌影像底图
 */
open class OpenCycleLayerActivity : BasicGlobeActivity(), TileFactory {
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
//        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        wwd.layers.addLayer(createRenderableLayer())
        return wwd
    }

    protected open fun createRenderableLayer(): RenderableLayer {
        val renderableLayer = RenderableLayer()
        renderableLayer.displayName = ("谷歌影像底图")
        renderableLayer.pickEnabled = (false)
        val levelsConfig = LevelSetConfig(null, 360.0, 21, 256, 256)
        val surfaceImage = TiledSurfaceImage()
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (this)
        surfaceImage.imageOptions = (ImageOptions(
            WorldWind.RGBA_8888
        ))
        renderableLayer.addRenderable(surfaceImage)
        return renderableLayer
    }

    override fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile {
        var x = column
        var y = row
        val z = level.levelNumber
        y = (1 shl z) - 1 - y
        val tile = ImageTile(getTileSector(x, y, z), level, row, column)
        tile.imageSource = ImageSource.fromUrl(tileUrl( x, y, z))
        return tile
    }

    open fun getTileSector(x: Int, y: Int, z: Int): Sector {
        val lng: Double
        val bottomLat: Double
        val lngWidth: Double
        val latHeight: Double

        val tilesAtThisZoom = 1 shl z
        lngWidth = 360.0 / tilesAtThisZoom // width in degrees longitude
        lng = -180 + x * lngWidth // left edge in degrees longitude
        val latHeightMerc =
            1.0 / tilesAtThisZoom // height in "normalized" mercator 0,0 top left
        val topLatMerc =
            y * latHeightMerc // top edge in "normalized" mercator 0,0 top left
        val bottomLatMerc = topLatMerc + latHeightMerc
        // convert top and bottom lat in mercator to degrees
        // note that in fact the coordinates go from about -85 to +85 not -90 to 90!
        bottomLat =
            180 / Math.PI * (2 * Math.atan(Math.exp(Math.PI * (1 - 2 * bottomLatMerc))) - Math.PI / 2)
        val topLat =
            180 / Math.PI * (2 * Math.atan(Math.exp(Math.PI * (1 - 2 * topLatMerc))) - Math.PI / 2)
        latHeight = topLat - bottomLat
        return Sector(bottomLat, lng, latHeight, lngWidth)
    }

    protected open fun cachePath(layer: String, x: Int, y: Int, z: Int): String {
        return layer + File.separator + z + File.separator + y + "." + x + ".jpg"
    }
    //https://webst01.is.autonavi.com/appmaptile?style=6&x=10&y=15&z=6
    private fun tileUrl(
        x: Int,
        y: Int,
        z: Int
    ): String {
        var url =
            "https://webst0${(System.currentTimeMillis() % 4).toInt() + 1}.is.autonavi.com/appmaptile?style=6"
        url += "&x=$x"
        url += "&y=$y"
        url += "&z=$z"
        Log.e("OpenCycleLayerActivity",url)
        return url
    }
}