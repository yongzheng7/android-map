package com.atom.wyz.pick

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import com.atom.wyz.worldwind.R
import com.atom.map.geom.Offset
import com.atom.map.geom.Position
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.Placemark
import com.atom.map.layer.render.attribute.PlacemarkAttributes
import com.atom.map.navigator.Navigator
import com.atom.wyz.base.BasicWorldWindActivity
import kotlin.math.asin
import kotlin.random.Random

@SuppressLint("Registered")
open class PlacemarksStressTestActivity : BasicWorldWindActivity(), FrameCallback {


    protected var activityPaused = false

    protected var cameraDegreesPerSecond = 2.0

    protected var lastFrameTimeNanos: Long = 0

    private val NUM_PLACEMARKS = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (l in getWorldWindow().layers) {
            l.enabled = false
        }
        val placemarksLayer = RenderableLayer("Placemarks_renderer")
        getWorldWindow().layers.addLayer(ShowTessellationLayer())
        getWorldWindow().layers.addLayer(placemarksLayer)

        // Create some placemarks at a known locations
        val origin: Placemark =
            Placemark(
                Position.fromDegrees(0.0, 0.0, 1e5),
                PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.air_fixwing))
                    .apply {
                        this.imageOffset(Offset.center())
                    },
                "Origin"
            )

        val northPole: Placemark =
            Placemark(
                Position.fromDegrees(90.0, 0.0, 1e5),
                PlacemarkAttributes.withImageAndLabelLeaderLine(ImageSource.fromResource(R.drawable.air_fixwing))
                    .apply {
                        this.imageOffset(Offset.bottomCenter())
                    },
                "North Pole"
            )
        val southPole: Placemark =
            Placemark(
                Position.fromDegrees(-90.0, 0.0, 0.0),
                PlacemarkAttributes.withImageAndLabel(
                    ImageSource.fromResource(R.drawable.crosshairs)
                ).apply {
                    this.imageOffset(Offset.bottomLeft())
                },
                "South Pole"
            )
        val antiMeridian: Placemark =
            Placemark(
                Position.fromDegrees(0.0, 180.0, 0.0),
                PlacemarkAttributes.withImageAndLabel(
                    ImageSource.fromResource(R.drawable.ehipcc)
                ).apply {
                    this.imageOffset(Offset.bottomRight())
                },
                "Anti-meridian"
            )

        placemarksLayer.addRenderable(origin)
        placemarksLayer.addRenderable(northPole)
        placemarksLayer.addRenderable(southPole)
        placemarksLayer.addRenderable(antiMeridian)

        ////////////////////
        // Stress Tests
        ////////////////////
        Placemark.DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e7
        val random = Random(123)

        val attributes: PlacemarkAttributes = PlacemarkAttributes.withImage(
            ImageSource.fromResource(R.drawable.ic_menu_home)
        )

        for (i in 0 until NUM_PLACEMARKS) {
            val lat =
                Math.toDegrees(asin(random.nextDouble())) * if (random.nextBoolean()) 1 else -1
            val lon = 180.0 - random.nextDouble() * 360
            val pos: Position = Position.fromDegrees(lat, lon, 5000.0)
            val placemark: Placemark = Placemark(pos, PlacemarkAttributes.defaults(attributes))

            placemark.eyeDistanceScaling = true
            placemark.displayName = placemark.position.toString()

            placemarksLayer.addRenderable(placemark)
        }
    }


    override fun onPause() {
        super.onPause()
        activityPaused = true
        lastFrameTimeNanos = 0
    }

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
            navigator.longitude = (navigator.longitude - cameraDegrees)
            // Redraw the World Window to display the above changes.
            getWorldWindow().requestRedraw()
        }

        if (!activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this)
        }

        lastFrameTimeNanos = frameTimeNanos
    }
}