package com.atom.wyz.shape

import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.geom.Position
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.attribute.ShapeAttributes
import com.atom.map.layer.render.shape.Ellipse
import com.atom.map.layer.render.shape.Path
import com.atom.map.layer.render.shape.Polygon
import com.atom.wyz.base.BasicGlobeActivity
import com.atom.wyz.worldwind.R
import java.util.*

class ShapesDashAndFillFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        // Let the super class (BasicGlobeFragment) do the creation
        val wwd = super.createWorldWindow()

        // Create a layer to display the tutorial shapes.
        val layer = RenderableLayer()
        wwd.layers.addLayer(layer)
        // Thicken all lines used in the tutorial.

        val thickenLine =
            ShapeAttributes.defaults()
        thickenLine.outlineWidth = (4f)

        var positions = Arrays.asList(
            Position.fromDegrees(60.0, -100.0, 1e5),
            Position.fromDegrees(30.0, -120.0, 1e5),
            Position.fromDegrees(0.0, -100.0, 1e5)
        )
        var path = Path(positions)

        var sa =
            ShapeAttributes.defaults(
                thickenLine
            )
        sa.outlineImageSource = (
            ImageSource.fromLineStipple(
                2 /*factor*/,
                0xF0F0.toShort() /*pattern*/
            )
        )
        path.attributes = (sa)
        layer.addRenderable(path)

        // Modify the factor of the pattern for comparison to first path. Only the factor is modified, not the pattern.
        positions = listOf(
            Position.fromDegrees(60.0, -90.0, 5e4),
            Position.fromDegrees(30.0, -110.0, 5e4),
            Position.fromDegrees(0.0, -90.0, 5e4)
        )
        path = Path(positions)
        sa = ShapeAttributes.defaults(
            thickenLine
        )
        sa.outlineImageSource = (
            ImageSource.fromLineStipple(
                4 /*factor*/,
                0xF0F0.toShort() /*pattern*/
            )
        )
        path.attributes= (sa)
        layer.addRenderable(path)

        // Create a path conforming to the terrain with a different pattern from the first two Paths.
        positions = listOf(
            Position.fromDegrees(60.0, -80.0, 0.0),
            Position.fromDegrees(30.0, -100.0, 0.0),
            Position.fromDegrees(0.0, -80.0, 0.0)
        )
        path = Path(positions)
        sa = ShapeAttributes.defaults(
            thickenLine
        )
        sa.outlineImageSource = (
            ImageSource.fromLineStipple(
                8 /*factor*/,
                0xDFF6.toShort() /*pattern*/
            )
        )
        path.attributes= (sa)
        path.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        path.followTerrain = (true)
        layer.addRenderable(path)

        // Create an Ellipse using an image as a repeating fill pattern

        // Create an Ellipse using an image as a repeating fill pattern
        val ellipse = Ellipse(
            Position(
                40.0,
                -70.0,
                1e5
            ), 1.5e6, 800e3
        )
        sa = ShapeAttributes.defaults(
            thickenLine
        )
        sa.interiorImageSource = (ImageSource.fromResource(R.drawable.nasa_logo))
        ellipse.attributes = (sa)
        layer.addRenderable(ellipse)
        // Create a surface polygon using an image as a repeating fill pattern and a dash pattern for the outline
        // of the polygon.
        positions = listOf(
            Position.fromDegrees(25.0, -85.0, 0.0),
            Position.fromDegrees(10.0, -80.0, 0.0),
            Position.fromDegrees(10.0, -60.0, 0.0),
            Position.fromDegrees(25.0, -55.0, 0.0)
        )
        val polygon =
            Polygon(positions)
        sa = ShapeAttributes.defaults(
            thickenLine
        )
        sa.interiorImageSource = (ImageSource.fromResource(R.drawable.nasa_logo))
        sa.outlineImageSource = (ImageSource.fromLineStipple(8, 0xDFF6.toShort()))
        polygon.attributes = (sa)
        polygon.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        polygon.followTerrain = (true)
        layer.addRenderable(polygon)

        return wwd
    }

}