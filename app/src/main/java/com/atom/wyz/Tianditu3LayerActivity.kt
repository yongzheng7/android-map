package com.atom.wyz

import android.util.Log
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.core.tile.ImageTile
import com.atom.map.core.tile.Tile
import com.atom.map.core.tile.TileFactory
import com.atom.map.geom.Sector
import com.atom.map.layer.CartesianLayer
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.map.layer.render.ImageOptions
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.TiledSurfaceImage
import com.atom.map.util.Level
import com.atom.map.util.LevelSet
import com.atom.map.util.LevelSetConfig
import java.io.File

/**
 * 天地图 影像 标注
 */
open class Tianditu3LayerActivity : BasicGlobeActivity(), TileFactory {
    private val key: String = "05f9d29cd248ba2fe566446ef123775c"
    private val name: String = "vec"
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.clearLayers()
        wwd.layers.addLayer(CartesianLayer())
        wwd.layers.addLayer(ShowTessellationLayer())
        wwd.layers.addLayer(createRenderableLayer())
        return wwd
    }

    protected open fun createRenderableLayer(): RenderableLayer {
        val renderableLayer = RenderableLayer()
        renderableLayer.displayName = ("天地图矢量底图")
        renderableLayer.pickEnabled = (false)
        val levelsConfig = LevelSetConfig(null, 45.0, 16, 256, 256)
        val surfaceImage = TiledSurfaceImage()
        surfaceImage.levelSet = (LevelSet(levelsConfig))
        surfaceImage.tileFactory = (this)
        surfaceImage.imageOptions = (ImageOptions(WorldWind.RGBA_8888))
        renderableLayer.addRenderable(surfaceImage)
        return renderableLayer
    }

    override fun createTile(sector: Sector, level: Level, row: Int, column: Int): Tile {
        var y = row
        val z = level.levelNumber + 3
        y = (1 shl z - 1) - 1 - y
        val tile = ImageTile(sector, level, row, column)
        val tileUrl = tileUrl(key, name, column, y, z)
        val cachePath: String = cachePath(name, column, y, z)
        tile.imageSource = (ImageSource.fromUrl(tileUrl))
        return tile
    }

    private fun cachePath(layer: String, x: Int, y: Int, z: Int): String {
        return layer + File.separator + z + File.separator + y + "." + x + ".jpg"
    }

    private fun tileUrl(
        key: String,
        layer: String,
        x: Int,
        y: Int,
        z: Int
    ): String {
        var url =
            "http://t" + (Math.random() * 8).toInt() + ".tianditu.gov.cn/" + layer + "_c" + "/wmts?SERVICE=WMTS"
        url += "&REQUEST=GetTile"
        url += "&VERSION=1.0.0"
        url += "&LAYER=$layer"
        url += "&STYLE=default"
        url += "&TILEMATRIXSET=c"
        url += "&FORMAT=tiles"
        url += "&TILEMATRIX=$z"
        url += "&TILEROW=$y"
        url += "&TILECOL=$x"
        url += "&tk=$key"
        Log.e("tianditu", url);
        return url
    }
}