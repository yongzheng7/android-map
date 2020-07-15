package com.atom.wyz.worldwind.app

import android.graphics.Typeface
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.Label
import com.atom.wyz.worldwind.shape.TextAttributes

class LabelsFragment : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        val wwd = super.createWorldWindow()

        val layer = RenderableLayer("Renderables")
        wwd.layers.addLayer(layer)

        val sanNicolas = Label(Position(33.262, -119.538, 0.0), "San Nicolas")
        layer.addRenderable(sanNicolas)

        val parkAttributes: TextAttributes = TextAttributes().apply {
            this.typeface = (Typeface.create("serif", Typeface.BOLD_ITALIC))
            this.textColor = Color(1f, 1f, 0f, 0.5f) // yellow, opaque
            this.textSize = (50f) // default size is 24
        }
        val island = Label(
            Position(34.005, -119.392, 0.0),
            "Anacapa Island", parkAttributes
        )
        layer.addRenderable(island)


        val pos = Position(34.2, -119.5, 0.0)
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

        val label1 = Label(pos, "NW: $northWest _", TextAttributes().apply { this.textOffset = northWest })
        val label2 = Label(pos, "SW: $southWest ¯", TextAttributes().apply { this.textOffset = southWest })
        val label3 = Label(pos, "_ NE: $northEast", TextAttributes().apply { this.textOffset = northEast })
        val label4 = Label(pos, "¯ SE: $southEast", TextAttributes().apply { this.textOffset = southEast })
        val label5 = Label(pos, "default") // anchor point is bottomCenter of label
        label5.rotationMode = (WorldWind.RELATIVE_TO_GLOBE)
        layer.addRenderable(label1)
        layer.addRenderable(label2)
        layer.addRenderable(label3)
        layer.addRenderable(label4)
        layer.addRenderable(label5)
        layer.addRenderable(Placemark.createSimple(pos, Color(Color.YELLOW), 10))


        val lookAt: LookAt = LookAt().set(
            pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0.0 /*heading*/, 0.0 /*tilt*/, 0.0 /*roll*/
        )
        wwd.navigator.setAsLookAt(wwd.globe, lookAt)
        return wwd
    }
}