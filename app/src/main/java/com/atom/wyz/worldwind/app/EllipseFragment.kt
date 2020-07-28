package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.shape.Ellipse
import com.atom.wyz.worldwind.attribute.ShapeAttributes

class EllipseFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        // Let the super class (BasicGlobeFragment) do the creation
        val wwd: WorldWindow = super.createWorldWindow()

        // Create a layer in which to display the ellipse shapes. In this tutorial, we use a new instance of
        // RenderableLayer. Like all Renderable objects, Ellipse shapes may be organized into any arrangement of layers.
        val tutorialLayer = RenderableLayer()
        wwd.layers.addLayer(tutorialLayer)

        // Create a surface ellipse with the default attributes, a 500km major-radius and a 300km minor-radius. Surface
        // ellipses are configured with a CLAMP_TO_GROUND altitudeMode and followTerrain set to true.
        var ellipse = Ellipse(Position(45.0, -120.0, 0.0), 500000.0, 300000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the ellipse's center position to the terrain surface
        ellipse.followTerrain = (true) // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse)

        // Create a surface ellipse with with custom attributes that make the interior 50% transparent and increase the
        // outline width.
        val attrs = ShapeAttributes()
        attrs.interiorColor = (Color(1f, 1f, 1f, 0.5f)) // 50% transparent white
        attrs.outlineWidth = (3f)
        ellipse = Ellipse(Position(45.0, -100.0, 0.0), 500000.0, 300000.0, attrs)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the ellipse's center position to the terrain surface
        ellipse.followTerrain = (true) // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse)

        // Create a surface ellipse with a heading of 45 degrees, causing the semi-major axis to point Northeast and the
        // semi-minor axis to point Southeast.
        ellipse = Ellipse(Position(35.0, -120.0, 0.0), 500000.0, 300000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the ellipse's center position to the terrain surface
        ellipse.followTerrain = (true) // cause the ellipse geometry to follow the terrain surface
        ellipse.heading = (45.0)
        tutorialLayer.addRenderable(ellipse)

        // Create a surface circle with the default attributes and 400km radius.
        ellipse = Ellipse(Position(35.0, -100.0, 0.0), 400000.0, 400000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND) // clamp the ellipse's center position to the terrain surface
        ellipse.followTerrain = (true) // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse)
        return wwd
    }
}