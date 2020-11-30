package com.atom.wyz.pick

import android.os.Bundle
import android.os.Handler
import com.atom.wyz.worldwind.R
import com.atom.map.geom.Offset
import com.atom.map.geom.Position
import com.atom.map.layer.LayerList
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.map.renderable.ImageSource
import com.atom.map.renderable.Placemark
import com.atom.map.renderable.attribute.PlacemarkAttributes
import com.atom.wyz.base.BasicWorldWindActivity
import java.util.*

class PlacemarksActivity : BasicWorldWindActivity() , Runnable {

    protected var animationHandler = Handler()

    protected var pauseHandler = false

    val DELAY_TIME = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layers: LayerList = getWorldWindow().layers
        layers.addLayer(ShowTessellationLayer())


        val placemarksLayer = RenderableLayer("Placemarks")
        getWorldWindow().layers.addLayer(placemarksLayer)

        // Create some placemarks at a known locations
        val origin: Placemark =
            Placemark(
                Position.fromDegrees(
                    0.0,
                    0.0,
                    0.0
                )
            )
        val northPole: Placemark =
            Placemark(
                Position.fromDegrees(
                    90.0,
                    0.0,
                    0.0
                )
            )
        val southPole: Placemark =
            Placemark(
                Position.fromDegrees(
                    -90.0,
                    0.0,
                    0.0
                )
            )
        val antiMeridian: Placemark =
            Placemark(
                Position.fromDegrees(
                    0.0,
                    180.0,
                    0.0
                )
            )

        origin.attributes.imageSource = (ImageSource.fromResource(R.drawable.air_fixwing))
        northPole.attributes.imageSource = (ImageSource.fromResource(R.drawable.airplane))
        southPole.attributes.imageSource = (ImageSource.fromResource(R.drawable.airport))
        antiMeridian.attributes.imageSource = (ImageSource.fromResource(R.drawable.airport_terminal))
        
        origin.attributes.imageOffset = (Offset.center())
        northPole.attributes.imageOffset = (Offset.center())
        southPole.attributes.imageOffset = (Offset.center())
        antiMeridian.attributes.imageOffset = (Offset.center())
        
        placemarksLayer.addRenderable(origin)
        placemarksLayer.addRenderable(northPole)
        placemarksLayer.addRenderable(southPole)
        placemarksLayer.addRenderable(antiMeridian)

        ////////////////////
        // Stress Tests
        ////////////////////
        Placemark.DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e7

        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        val random = Random(123)

//        for (i in 0..100) {
//
//            attributes.imageColor = Color.random()
//            val lat = Math.toDegrees(Math.asin(random.nextDouble())) * if (random.nextBoolean()) 1 else -1
//            val lon = 180.0 - random.nextDouble() * 360
//            val pos= Position.fromDegrees(lat, lon, 0.0)
//            val placemark = Placemark(pos, PlacemarkAttributes(attributes))
//            placemarksLayer.addRenderable(placemark)
//
//        }


        //        // Create "sprinkles" -- random colored squares
//        PlacemarkAttributes attributes = new PlacemarkAttributes();
//        attributes.setImageScale(10);
//        for (int i = 0; i < NUM_PLACEMARKS; i++) {
//            // Generate a random color for this placemark
//            attributes.setImageColor(Color.random());
//            // Create an even distribution of latitude and longitudes
//            // Use a random sin value to generate latitudes without clustering at the poles
//            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
//            double lon = 180d - (random.nextDouble() * 360);
//            Position pos = Position.fromDegrees(lat, lon, 0);
//
//            Placemark placemark = new Placemark(pos, new PlacemarkAttributes(attributes));
//            placemark.setEyeDistanceScaling(false);
//            placemarksLayer.addRenderable(placemark);
//        }
        // Create pushpins anchored at the "pinpoints" with eye distance scaling

        val attributes: PlacemarkAttributes =
            PlacemarkAttributes.defaults()
        attributes.imageOffset = (Offset.center())
        attributes.imageSource = ImageSource.fromResource(R.drawable.crosshairs)
        attributes.imageScale = (1.0)
        for (i in 0 until 1000) { // Create an even distribution of latitude and longitudes across the globe.
        // Use a random sin value to generate latitudes without clustering at the poles.
            val lat = Math.toDegrees(Math.asin(random.nextDouble())) * if (random.nextBoolean()) 1 else -1
            val lon = 180.0 - random.nextDouble() * 360
            val pos: Position = Position.fromDegrees(lat, lon, 0.0)
            val placemark: Placemark =
                Placemark(
                    pos,
                    PlacemarkAttributes.defaults(
                        attributes
                    )
                )
            placemark.eyeDistanceScaling = (true)
            placemarksLayer.addRenderable(placemark)
        }
    }


    override fun run() {
        val navigator = getWorldWindow().navigator
        navigator.longitude = (navigator.longitude - 0.03)
        getWorldWindow().requestRender()
        if (!pauseHandler) {
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