package com.atom.wyz.worldwind.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Color
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.PlacemarkAttributes

class Placemarks2Activity : BasicWorldWindActivity() {

    protected var animationHandler = Handler()

    protected var pauseHandler = false

    val DELAY_TIME = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val placemarksLayer = RenderableLayer("Placemarks")
        getWorldWindow().layers.addLayer(placemarksLayer)


        val ventura = Placemark.createSimple(Position.fromDegrees(34.281, -119.293, 0.0), Color.CYAN, 20)


        val airplane = Placemark(Position.fromDegrees(34.260, -119.2, 5000.0), PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromResource(R.drawable.air_fixwing)).apply {
            this.imageScale = 1.5
        })


        val airport = Placemark(
                Position.fromDegrees(34.200, -119.208, 0.0),
                PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.airport_terminal)).apply {
                    this.imageOffset = (Offset.BOTTOM_CENTER)
                    this.imageScale = 2.0
                },
                "Oxnard Airport")

        val wildfire = Placemark(
                Position.fromDegrees(34.300, -119.25, 0.0),
                PlacemarkAttributes.withImageAndLabel(ImageSource.fromBitmap( BitmapFactory.decodeResource(resources, R.drawable.ehipcc))).apply { this.imageOffset = Offset.BOTTOM_CENTER },
                "Fire")

        placemarksLayer.addRenderable(ventura)
        placemarksLayer.addRenderable(airport)
        placemarksLayer.addRenderable(airplane)
        placemarksLayer.addRenderable(wildfire)

        val pos: Position? = airport.position
        val lookAt: LookAt = LookAt().set(pos!!.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
                1e5 /*range*/, 0.0, 80.0, 0.0)
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)

    }

}