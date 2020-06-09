package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.shape.Path
import com.atom.wyz.worldwind.shape.ShapeAttributes
import java.util.*

class PathsActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a layer to display the tutorial paths.
        val layer = RenderableLayer()
        wwd.layers.addLayer(layer)

        // Create a basic path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        var positions = Arrays.asList(
            Position.fromDegrees(50.0, -180.0, 1e5),
            Position.fromDegrees(30.0, -100.0, 1e6),
            Position.fromDegrees(50.0, -40.0, 1e5)
        )
        var path = Path(positions)
        layer.addRenderable(path)

        // Create a basic path with the default attributes, the CLAMP_TO_GROUND altitude mode,
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(40.0, -180.0, 0.0),
            Position.fromDegrees(20.0, -100.0, 0.0),
            Position.fromDegrees(40.0, -40.0, 0.0)
        )
        path = Path(positions)
        path.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        path.followTerrain = (true)
        layer.addRenderable(path)

        // Create an extruded path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(30.0, -180.0, 1e5),
            Position.fromDegrees(10.0, -100.0, 1e6),
            Position.fromDegrees(30.0, -40.0, 1e5)
        )
        path = Path(positions)
        path.extrude = (true) // extrude the path from the ground to each path position's altitude

        layer.addRenderable(path)

        // Create an extruded path with custom attributes that display the extruded vertical lines,
        // make the extruded interior 50% transparent, and increase the path line with.
        positions = Arrays.asList(
            Position.fromDegrees(20.0, -180.0, 1e5),
            Position.fromDegrees(0.0, -100.0, 1e6),
            Position.fromDegrees(20.0, -40.0, 1e5)
        )
        val attrs = ShapeAttributes()
        attrs.drawVerticals = (true) // display the extruded verticals

        attrs.interiorColor = (Color(1f, 1f, 1f, 0.5f)) // 50% transparent white

        attrs.outlineWidth = (3f)
        path = Path(positions, attrs)
        path.extrude = (true) // extrude the path from the ground to each path position's altitude

        layer.addRenderable(path)
    }
}