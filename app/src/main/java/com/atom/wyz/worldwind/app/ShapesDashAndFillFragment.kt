package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.shape.Path
import com.atom.wyz.worldwind.shape.Polygon
import com.atom.wyz.worldwind.shape.ShapeAttributes
import java.util.*

class ShapesDashAndFillFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        val wwd = super.createWorldWindow()
        // Create a layer to display the tutorial shapes.
        val layer = RenderableLayer()
        wwd.layers.addLayer(layer)

        // Thicken all lines used in the tutorial.
        val thickenLine = ShapeAttributes()
        thickenLine.outlineWidth  =4f

        // Create a path with a simple dashed pattern generated from the ImageSource factory. The
        // ImageSource.fromLineStipple function generates a texture based on the provided factor and pattern, similar to
        // stipple properties of OpenGL2. The binary representation of the pattern will be the pattern displayed, where
        // positions with a 1 appearing as opaque and a 0 as completely transparent.
        var positions = Arrays.asList(
            Position.fromDegrees(60.0, -100.0, 1e5),
            Position.fromDegrees(30.0, -120.0, 1e5),
            Position.fromDegrees(0.0, -100.0, 1e5)
        )
        var path = Path(positions)
        var sa = ShapeAttributes(thickenLine)
        sa.outlineImageSource = (ImageSource.fromLineStipple(5 /*factor*/, 0xF0F0.toShort() /*pattern*/))
        path.attributes = sa
        layer.addRenderable(path)

        // Modify the factor of the pattern for comparison to first path. Extrude to the ground demonstrating how the
        // dash pattern carries through the extrusion.
        positions = Arrays.asList(
            Position.fromDegrees(60.0, -90.0, 5e4),
            Position.fromDegrees(30.0, -110.0, 5e4),
            Position.fromDegrees(0.0, -90.0, 5e4)
        )
        path = Path(positions)
        sa = ShapeAttributes(thickenLine)
        sa.outlineImageSource = (ImageSource.fromLineStipple(10 /*factor*/, 0xF0F0.toShort() /*pattern*/))
        path.attributes = sa
        path.extrude = true
        layer.addRenderable(path)

        // Create a path conforming to the terrain with a different pattern.
        // Create a path conforming to the terrain with a different pattern.
        positions = Arrays.asList(
            Position.fromDegrees(60.0, -80.0, 0.0),
            Position.fromDegrees(30.0, -100.0, 0.0),
            Position.fromDegrees(0.0, -80.0, 0.0)
        )
        path = Path(positions)
        sa = ShapeAttributes(thickenLine)
        sa.outlineImageSource = (ImageSource.fromLineStipple(10 /*factor*/, 0xFFF4.toShort() /*pattern*/))
        path.attributes = (sa)
        path.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        path.followTerrain = (true)
        layer.addRenderable(path)

        // Create a polygon using the NASA logo as a repeating fill pattern.
        // Create a polygon using the NASA logo as a repeating fill pattern.
        positions = Arrays.asList(
            Position.fromDegrees(50.0, -70.0, 1e5),
            Position.fromDegrees(35.0, -85.0, 1e5),
            Position.fromDegrees(35.0, -55.0, 1e5)
        )
        var polygon = Polygon(positions)
        sa = ShapeAttributes(thickenLine)
        sa.interiorImageSource = (ImageSource.fromResource(R.drawable.nasa_logo))
        polygon.attributes = sa
        layer.addRenderable(polygon)

        // Create a surface polygon using the NASA logo as a repeating fill pattern and a dash pattern for the outline
        // of the polygon.
        positions = Arrays.asList(
            Position.fromDegrees(25.0, -85.0, 0.0),
            Position.fromDegrees(10.0, -80.0, 0.0),
            Position.fromDegrees(10.0, -60.0, 0.0),
            Position.fromDegrees(25.0, -55.0, 0.0)
        )
        polygon = Polygon(positions)
        sa = ShapeAttributes(thickenLine)
        sa.interiorImageSource = (ImageSource.fromResource(R.drawable.nasa_logo))
        sa.outlineImageSource = (ImageSource.fromLineStipple(5 /*factor*/, 0xF0F0.toShort() /*pattern*/))
        polygon.attributes = sa
        polygon.altitudeMode =(WorldWind.CLAMP_TO_GROUND)
        polygon.followTerrain =(true)
        layer.addRenderable(polygon)

        return wwd
    }

}