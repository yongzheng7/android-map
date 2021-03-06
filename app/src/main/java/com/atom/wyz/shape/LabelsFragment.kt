package com.atom.wyz.shape

import android.graphics.Color
import android.graphics.Typeface
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.geom.Offset
import com.atom.map.geom.Position
import com.atom.map.geom.SimpleColor
import com.atom.map.geom.LookAt
import com.atom.map.layer.RenderableLayer
import com.atom.map.renderable.Placemark
import com.atom.map.renderable.attribute.TextAttributes
import com.atom.map.renderable.shape.Label
import com.atom.wyz.base.BasicGlobeActivity

class LabelsFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        val wwd = super.createWorldWindow()

        val layer = RenderableLayer("Renderables")
        wwd.layers.addLayer(layer)

        val sanNicolas = Label(
            Position(33.262, -119.538, 5000.0),
            "San Nicolas"
        )
        layer.addRenderable(sanNicolas)

        val parkAttributes: TextAttributes = TextAttributes.defaults()
            .apply {
                this.typeface = (Typeface.create("serif", Typeface.BOLD_ITALIC))
                this.textColor.set(
                    SimpleColor(
                        1f,
                        1f,
                        0f,
                        0.5f
                    )
                )
                // yellow, opaque
                this.textSize = (50f) // default size is 24
                this.textOffset.set(Offset.centerLeft())
            }
        val island = Label(
            Position(34.005, -119.392, 100000.0),
            "Anacapa Island", parkAttributes
        )
        layer.addRenderable(island)


        val pos = Position(34.2, -119.5, 100000.0)
        val northEast = Offset(
            WorldWind.OFFSET_PIXELS, -40.0,  // move left-edge right
            WorldWind.OFFSET_PIXELS, -40.0
        ) // move lower-edge up
        val northWest = Offset(
            WorldWind.OFFSET_INSET_PIXELS, -40.0,  // move right-edge left
            WorldWind.OFFSET_PIXELS, -40.0
        ) // move lower-edge up
        val southWest = Offset(
            WorldWind.OFFSET_INSET_PIXELS, -40.0,  // move right-edge left
            WorldWind.OFFSET_INSET_PIXELS, -40.0
        ) // move top-edge down
        val southEast = Offset(
            WorldWind.OFFSET_PIXELS, -40.0,  // move left-edge right
            WorldWind.OFFSET_INSET_PIXELS, -40.0
        ) // move top-edge down

        val label1 = Label(pos,
            "NW: $northWest _",
            TextAttributes.defaults()
                .apply { this.textOffset = northWest })
        val label2 = Label(pos,
            "SW: $southWest ¯",
            TextAttributes.defaults()
                .apply { this.textOffset = southWest })
        val label3 = Label(pos,
            "_ NE: $northEast",
            TextAttributes.defaults()
                .apply { this.textOffset = northEast })
        val label4 = Label(pos,
            "¯ SE: $southEast",
            TextAttributes.defaults()
                .apply { this.textOffset = southEast })
        val label5 = Label(
            pos,
            "default"
        ) // anchor point is bottomCenter of label
        label5.rotationMode = (WorldWind.RELATIVE_TO_GLOBE)
        layer.addRenderable(label1)
        layer.addRenderable(label2)
        layer.addRenderable(label3)
        layer.addRenderable(label4)
        layer.addRenderable(label5)
        layer.addRenderable(
            Placemark.createSimple(
                pos,
                SimpleColor(Color.YELLOW), 10
            )
        )


        val lookAt: LookAt = LookAt()
            .set(
            pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0.0 /*heading*/, 0.0 /*tilt*/, 0.0 /*roll*/
        )
        wwd.navigator.setAsLookAt(wwd.globe, lookAt)
        return wwd
    }
}