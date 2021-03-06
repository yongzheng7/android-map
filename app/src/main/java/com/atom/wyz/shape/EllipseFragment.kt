package com.atom.wyz.shape

import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.geom.Position
import com.atom.map.geom.SimpleColor
import com.atom.map.layer.RenderableLayer
import com.atom.map.renderable.attribute.ShapeAttributes
import com.atom.map.renderable.shape.Ellipse
import com.atom.map.renderable.shape.Label
import com.atom.wyz.base.BasicGlobeActivity

class EllipseFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        // Let the super class (BasicGlobeFragment) do the creation
        val wwd: WorldWindow = super.createWorldWindow()
        val tutorialLayer = RenderableLayer()

        wwd.layers.addLayer(tutorialLayer)

        var ellipse = Ellipse(
            Position(45.0,
                -120.0,
                0.0),
            500000.0,
            300000.0
        )
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        tutorialLayer.addRenderable(ellipse)

        var sanNicolas = Label(
            Position(45.0,
                -120.0,
                0.0),
            "1"
        )
        tutorialLayer.addRenderable(sanNicolas)

        // Create an ellipse with the default attributes, an altitude of 200 km, and a 500km major-radius and a 300km
        // minor-radius.
        ellipse = Ellipse(
            Position(
                25.0,
                -120.0,
                0.0
            ), 1000.0, 1000.0
        )
        tutorialLayer.addRenderable(ellipse)

        sanNicolas = Label(
            Position(25.0,
                -120.0,
                0.0),
            "2"
        )
        tutorialLayer.addRenderable(sanNicolas)

        var attrs = ShapeAttributes.defaults()
        attrs.interiorColor = (SimpleColor(1f, 1f, 1f, 0.5f)) // 50% transparent white
        attrs.drawVerticals = (true)

        ellipse = Ellipse(
            Position(
                25.0,
                -100.0,
                200e3
            ), 500000.0, 300000.0, attrs
        )
        ellipse.extrude = (true)
        tutorialLayer.addRenderable(ellipse)

        sanNicolas = Label(
            Position(25.0,
                -100.0,
                200e3),
            "3"
        )
        tutorialLayer.addRenderable(sanNicolas)

        ellipse = Ellipse(
            Position(
                35.0,
                -100.0,
                200e3
            ), 400000.0, 300000.0 , attrs
        )
        ellipse.altitudeMode = (WorldWind.ABSOLUTE)
        ellipse.followTerrain = (true)
        ellipse.extrude = (true)
        tutorialLayer.addRenderable(ellipse)

        sanNicolas = Label(
            Position(35.0,
                -100.0,
                200e3),
            "4"
        )
        tutorialLayer.addRenderable(sanNicolas)

        // 线粗3
        attrs = ShapeAttributes.defaults()
        attrs.interiorColor = (SimpleColor(
            1f,
            1f,
            1f,
            0.5f
        ))
        attrs.outlineWidth = (3f)
        ellipse = Ellipse(
            Position(
                45.0,
                -100.0,
                0.0
            ), 500000.0, 300000.0, attrs
        )
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        tutorialLayer.addRenderable(ellipse)
        sanNicolas = Label(
            Position(45.0,
                -100.0,
                0.0),
            "5"
        )
        tutorialLayer.addRenderable(sanNicolas)

        // 旋转45度
        ellipse = Ellipse(
            Position(
                35.0,
                -120.0,
                0.0
            ), 500000.0, 300000.0
        )
        ellipse.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
        ellipse.followTerrain = (true)
        ellipse.heading = (45.0)
        tutorialLayer.addRenderable(ellipse)
        sanNicolas = Label(
            Position(35.0,
                -120.0,
                0.0),
            "6"
        )
        tutorialLayer.addRenderable(sanNicolas)
        return wwd
    }
}