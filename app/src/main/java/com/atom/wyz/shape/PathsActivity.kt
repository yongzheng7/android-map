package com.atom.wyz.shape

import android.os.Bundle
import com.atom.map.WorldWind
import com.atom.map.geom.Position
import com.atom.map.geom.SimpleColor
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.attribute.ShapeAttributes
import com.atom.map.layer.render.shape.Path
import com.atom.wyz.base.BasicWorldWindActivity
import java.util.*

class PathsActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a layer to display the tutorial paths.
        //wwd.layers.clearLayers()
        val layer = RenderableLayer()
        getWorldWindow().layers.addLayer(layer)

        // line  1
        var positions = Arrays.asList(
            Position.fromDegrees(50.0, -180.0, 1e5),
            Position.fromDegrees(30.0, -100.0, 1e6),
            Position.fromDegrees(50.0, -40.0, 1e5)
        )
        var path = Path(positions)
        layer.addRenderable(path)

        // line  2
        positions = Arrays.asList(
            Position.fromDegrees(40.0, -180.0, 0.0),
            Position.fromDegrees(20.0, -100.0, 0.0),
            Position.fromDegrees(40.0, -40.0, 0.0)
        )
        path = Path(positions)
        path.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        path.followTerrain = (true)
        layer.addRenderable(path)

        // line  3
        positions = Arrays.asList(
            Position.fromDegrees(30.0, -180.0, 1e5),
            Position.fromDegrees(10.0, -100.0, 1e6),
            Position.fromDegrees(30.0, -40.0, 1e5)
        )
        path = Path(positions)
        path.extrude = (true)
        layer.addRenderable(path)

        // line  4
        positions = Arrays.asList(
            Position.fromDegrees(20.0, -180.0, 1e5),
            Position.fromDegrees(0.0, -100.0, 1e6),
            Position.fromDegrees(20.0, -40.0, 1e5)
        )
        val attrs =
            ShapeAttributes.defaults()
        attrs.drawVerticals = (true)
        attrs.interiorColor = (SimpleColor(
            1f,
            1f,
            1f,
            0.5f
        ))
        attrs.outlineWidth = (3f)
        path = Path(positions, attrs)
        path.extrude = (true)
        layer.addRenderable(path)
    }
}