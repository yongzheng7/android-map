package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.layer.ShowTessellationLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import java.util.*

@SuppressLint("Registered")
class PlacemarksStressTestActivity : BasicWorldWindActivity() , Runnable {

    protected var animationHandler = Handler()

    protected var pauseHandler = false

    val DELAY_TIME = 100

    val NUM_PLACEMARKS = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Turn off all layers while debugging/profiling memory allocations
        for (l in getWorldWindow().layers) {
            l.enabled = (false)
        }

        getWorldWindow().layers.addLayer(ShowTessellationLayer())

        // Add a Renderable layer for the placemarks before the Atmosphere layer
        val layers: LayerList = getWorldWindow().layers
        val index: Int = layers.indexOfLayerNamed("Atmosphere")
        val placemarksLayer: RenderableLayer = RenderableLayer("Placemarks")
        getWorldWindow().layers.addLayer(index, placemarksLayer)

        // Create some placemarks at a known locations
        var withImageAndLabel = PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.air_fixwing))
        withImageAndLabel.imageOffset = Offset.CENTER
        val origin: Placemark = Placemark(Position.fromDegrees(0.0, 0.0, 1e5),
                withImageAndLabel,
                "Origin")
        withImageAndLabel = PlacemarkAttributes.withImageAndLabelLeaderLine(ImageSource.fromResource(R.drawable.air_fixwing))
        withImageAndLabel.imageOffset = Offset.BOTTOM_CENTER
        val northPole: Placemark = Placemark(Position.fromDegrees(90.0, 0.0, 1e5),
                withImageAndLabel,
                "North Pole")
        withImageAndLabel = PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.crosshairs))
        withImageAndLabel.imageOffset = Offset.BOTTOM_LEFT
        val southPole: Placemark = Placemark(Position.fromDegrees(-90.0, 0.0, 0.0),
                withImageAndLabel,
                "South Pole")
        withImageAndLabel =  PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.ehipcc))
        withImageAndLabel.imageOffset = Offset.BOTTOM_RIGHT

        val antiMeridian: Placemark = Placemark(Position.fromDegrees(0.0, 180.0, 0.0),
                withImageAndLabel,
                "Anti-meridian")

        placemarksLayer.addRenderable(origin)
        placemarksLayer.addRenderable(northPole)
        placemarksLayer.addRenderable(southPole)
        placemarksLayer.addRenderable(antiMeridian)

        ////////////////////
        // Stress Tests
        ////////////////////
        Placemark.defaultEyeDistanceScalingThreshold = 1e7 
        val random = Random(123)
        
        val attributes: PlacemarkAttributes = PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.ic_menu_home))
        attributes.imageOffset  = Offset.CENTER

        for (i in 0 until NUM_PLACEMARKS) {
            val lat = Math.toDegrees(Math.asin(random.nextDouble())) * if (random.nextBoolean()) 1 else -1
            val lon = 180.0 - random.nextDouble() * 360
            val pos: Position = Position.fromDegrees(lat, lon, 5000.0)
            val placemark: Placemark = Placemark(pos, PlacemarkAttributes(attributes))
            placemark.eyeDistanceScaling = true
            placemark.displayName = placemark.position.toString()
            placemarksLayer.addRenderable(placemark)
        }
    }

    override fun run() { // Move the navigator to simulate the Earth's rotation about its axis.
        val navigator = getWorldWindow().navigator
        navigator.setLongitude(navigator.getLongitude() - 0.03)
        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender()
        if (!pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            animationHandler.postDelayed(this, 30)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseHandler = true
    }

    override fun onResume() {
        super.onResume()
        pauseHandler = false
        animationHandler.postDelayed(this, DELAY_TIME.toLong())
    }
}