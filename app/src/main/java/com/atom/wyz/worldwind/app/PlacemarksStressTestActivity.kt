package com.atom.wyz.worldwind.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import com.atom.wyz.worldwind.Navigator
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
class PlacemarksStressTestActivity : BasicWorldWindActivity() , FrameCallback {


    protected var activityPaused = false

    protected var cameraDegreesPerSecond = 2.0

    protected var lastFrameTimeNanos: Long = 0

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
        val placemarksLayer: RenderableLayer = RenderableLayer("Placemarks")
        getWorldWindow().layers.addLayer(placemarksLayer)

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
        Placemark.DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e7
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


    override fun onPause() {
        super.onPause()
        activityPaused = true
        lastFrameTimeNanos = 0    }

    override fun onResume() {
        super.onResume()
        activityPaused = false
        lastFrameTimeNanos = 0
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos != 0L) { // Compute the frame duration in seconds.
            val frameDurationSeconds = (frameTimeNanos - lastFrameTimeNanos) * 1.0e-9
            val cameraDegrees = frameDurationSeconds * cameraDegreesPerSecond
            // Move the navigator to simulate the Earth's rotation about its axis.
            val navigator: Navigator = getWorldWindow().navigator
            navigator.setLongitude(navigator.getLongitude() - cameraDegrees)
            // Redraw the World Window to display the above changes.
            getWorldWindow().requestRedraw()
        }

        if (!activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this)
        }

        lastFrameTimeNanos = frameTimeNanos
    }
}