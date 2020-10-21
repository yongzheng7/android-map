package com.atom.wyz.base

import android.os.Bundle
import com.atom.wyz.base.BasicWorldWindActivity
import com.atom.wyz.worldwind.R
import com.atom.map.geom.Sector
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.SurfaceImage

class SurfaceImageActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var sector = Sector(37.46, 15.5, 0.5, 0.6)
        val surfaceImageResource =
            SurfaceImage(
                sector,
                ImageSource.fromResource(R.drawable.nasa_logo)
            )

        sector = Sector(37.46543388598137, 14.60128369746704, 0.9, 0.9)
        val urlString = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583231249046&di=3c273bfe3af16d329ea1055d6b46f8f4&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201702%2F23%2F20170223114933_dkSeP.jpeg"
        val surfaceImageUrl = SurfaceImage(
            sector,
            ImageSource.fromUrl(urlString)
        )

        val layer = RenderableLayer("Surface Image")
        layer.addRenderable(surfaceImageResource)
        layer.addRenderable(surfaceImageUrl)
        getWorldWindow().layers.addLayer(layer)

        getWorldWindow().navigator.latitude = (37.46543388598137)
        getWorldWindow().navigator.longitude = (14.97980511744455)
        getWorldWindow().navigator.altitude = (4.0e5)
    }
}