package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.os.Bundle
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.attribute.ShapeAttributes
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.shape.Polygon
import java.util.*

@SuppressLint("Registered")
class PolygonsActivity :BasicWorldWindActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a layer to display the tutorial polygons.
        val layer = RenderableLayer()
        wwd.layers.addLayer(layer)
        var positions: List<Position> = Arrays.asList(
            Position.fromDegrees(40.0, -135.0, 5.0e5),
            Position.fromDegrees(45.0, -140.0, 7.0e5),
            Position.fromDegrees(50.0, -130.0, 9.0e5),
            Position.fromDegrees(45.0, -120.0, 7.0e5),
            Position.fromDegrees(40.0, -125.0, 5.0e5)
        )
        var poly = Polygon(positions)
        poly.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the polygon vertices to the ground
        poly.followTerrain = (true)
        layer.addRenderable(poly)

       //  Create a terrain following polygon with the default attributes, and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(40.0, -105.0, 0.0),
            Position.fromDegrees(45.0, -110.0, 0.0),
            Position.fromDegrees(50.0, -100.0, 0.0),
            Position.fromDegrees(45.0, -90.0, 0.0),
            Position.fromDegrees(40.0, -95.0, 0.0)
        )
        poly = Polygon(positions)
        poly.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the polygon vertices to the ground
        poly.followTerrain = (true) // follow the ground between polygon vertices
        layer.addRenderable(poly)
        // Create an extruded polygon with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(20.0, -135.0, 5.0e5),
            Position.fromDegrees(25.0, -140.0, 7.0e5),
            Position.fromDegrees(30.0, -130.0, 9.0e5),
            Position.fromDegrees(25.0, -120.0, 7.0e5),
            Position.fromDegrees(20.0, -125.0, 5.0e5)
        )
        poly = Polygon(positions)
        poly.extrude = (true) // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly)
        // Create an extruded polygon with custom attributes that display the extruded vertical lines,
// make the extruded interior 50% transparent, and increase the polygon line with.
        positions = Arrays.asList(
            Position.fromDegrees(20.0, -105.0, 5.0e5),
            Position.fromDegrees(25.0, -110.0, 7.0e5),
            Position.fromDegrees(30.0, -100.0, 9.0e5),
            Position.fromDegrees(25.0, -90.0, 7.0e5),
            Position.fromDegrees(20.0, -95.0, 5.0e5)
        )
        val attrs = ShapeAttributes()
        attrs.drawVerticals  =(true) // display the extruded verticals
        attrs.interiorColor = (SimpleColor(1f, 1f, 1f, 0.5f)) // 50% transparent white
        attrs.outlineWidth = (3f)
        poly = Polygon(positions, attrs)
        poly.extrude = (true) // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly)
        // Create a polygon with an inner hole by specifying multiple polygon boundaries
        poly = Polygon()
        poly.addBoundary(
            Arrays.asList(
                Position.fromDegrees(0.0, -135.0, 5.0e5),
                Position.fromDegrees(5.0, -140.0, 7.0e5),
                Position.fromDegrees(10.0, -130.0, 9.0e5),
                Position.fromDegrees(5.0, -120.0, 7.0e5),
                Position.fromDegrees(0.0, -125.0, 5.0e5)
            )
        )
        poly.addBoundary(
            listOf(
                Position.fromDegrees(2.5, -130.0, 6.0e5),
                Position.fromDegrees(5.0, -135.0, 7.0e5),
                Position.fromDegrees(7.5, -130.0, 8.0e5),
                Position.fromDegrees(5.0, -125.0, 7.0e5)
            )
        )
        layer.addRenderable(poly)
        // Create an extruded polygon with an inner hole and custom attributes that display the extruded vertical lines,
        // make the extruded interior 50% transparent, and increase the polygon line with.
        poly = Polygon(attrs)
        poly.addBoundary(
            listOf(
                Position.fromDegrees(0.0, -105.0, 5.0e5),
                Position.fromDegrees(5.0, -110.0, 7.0e5),
                Position.fromDegrees(10.0, -100.0, 9.0e5),
                Position.fromDegrees(5.0, -90.0, 7.0e5),
                Position.fromDegrees(0.0, -95.0, 5.0e5)
            )
        )
        poly.addBoundary(
            Arrays.asList(
                Position.fromDegrees(2.5, -100.0, 6.0e5),
                Position.fromDegrees(5.0, -105.0, 7.0e5),
                Position.fromDegrees(7.5, -100.0, 8.0e5),
                Position.fromDegrees(5.0, -95.0, 7.0e5)
            )
        )
        poly.extrude  =(true) // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly)
    }

}