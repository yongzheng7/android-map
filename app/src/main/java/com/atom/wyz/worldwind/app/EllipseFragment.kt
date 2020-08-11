package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.attribute.ShapeAttributes
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.shape.Ellipse

class EllipseFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        // Let the super class (BasicGlobeFragment) do the creation
        val wwd: WorldWindow = super.createWorldWindow()
        val tutorialLayer = RenderableLayer()

        wwd.layers.addLayer(tutorialLayer)
        var ellipse = Ellipse(Position(0.0, 0.0, 0.0), 500000.0, 300000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        tutorialLayer.addRenderable(ellipse)

        // Create an ellipse with the default attributes, an altitude of 200 km, and a 500km major-radius and a 300km
        // minor-radius.
        ellipse = Ellipse(Position(25.0, -120.0, 200e3), 500000.0, 300000.0)
        tutorialLayer.addRenderable(ellipse)


        var attrs = ShapeAttributes()
        attrs.interiorColor = (SimpleColor(1f, 1f, 1f, 0.5f)) // 50% transparent white


        attrs.drawVerticals = (true)
        ellipse = Ellipse(Position(25.0, -100.0, 200e3), 500000.0, 300000.0, attrs)
        ellipse.extrude = (true)
        tutorialLayer.addRenderable(ellipse)


        attrs = ShapeAttributes()
        attrs.interiorColor = (SimpleColor(
            1f,
            1f,
            1f,
            0.5f
        ))
        attrs.outlineWidth = (3f)
        ellipse = Ellipse(Position(45.0, -100.0, 0.0), 500000.0, 300000.0, attrs)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        tutorialLayer.addRenderable(ellipse)

        ellipse = Ellipse(Position(35.0, -120.0, 0.0), 500000.0, 300000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        ellipse.heading = (45.0)
        tutorialLayer.addRenderable(ellipse)


        ellipse = Ellipse(Position(35.0, -100.0, 0.0), 400000.0, 400000.0)
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        tutorialLayer.addRenderable(ellipse)
        return wwd
    }
}