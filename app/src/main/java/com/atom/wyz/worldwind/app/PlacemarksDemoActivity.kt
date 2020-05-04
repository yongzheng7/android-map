package com.atom.wyz.worldwind.app

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.atom.wyz.worldwind.BasicWorldWindowController
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.RenderContext
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.render.Renderable
import com.atom.wyz.worldwind.shape.Highlightable
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.util.*

// TODO
class PlacemarksDemoActivity : BasicWorldWindActivity() {
    // A component for displaying the status of this activity
    protected var statusText: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add a TextView on top of the globe to convey the status of this activity
        // Add a TextView on top of the globe to convey the status of this activity
        statusText = TextView(this)
        statusText!!.setTextColor(Color.YELLOW)
        val globeLayout = findViewById(R.id.globe) as FrameLayout
        globeLayout.addView(statusText)

        // Override the World Window's built-in navigation behavior by adding picking support.
        // Override the World Window's built-in navigation behavior by adding picking support.
        getWorldWindow().worldWindowController = (PickController())

        CreatePlacesTask().execute()
    }

    class PlaceLevelOfDetailSelector : Placemark.LevelOfDetailSelector {
        companion object {
            protected const val LEVEL_0_DISTANCE = 2000000

            protected const val LEVEL_0_POPULATION = 500000

            const val LEVEL_1_DISTANCE = 1500000

            protected const val LEVEL_1_POPULATION = 250000

            protected const val LEVEL_2_DISTANCE = 500000

            protected const val LEVEL_2_POPULATION = 100000

            protected const val LEVEL_3_DISTANCE = 250000

            protected const val LEVEL_3_POPULATION = 50000

            protected const val LEVEL_4_DISTANCE = 100000

            protected const val LEVEL_4_POPULATION = 10000

            protected const val LEVEL_0 = 0

            protected const val LEVEL_1 = 1

            protected const val LEVEL_2 = 2

            protected const val LEVEL_3 = 3

            protected const val LEVEL_4 = 4

            protected const val LEVEL_5 = 5

            protected var defaultAttributes: PlacemarkAttributes =
                PlacemarkAttributes().apply {
                    this.imageScale = 15.0
                    this.minimumImageScale = 5.0
                }

            protected var iconCache: HashMap<String, WeakReference<PlacemarkAttributes>> =
                HashMap<String, WeakReference<PlacemarkAttributes>>()

            protected fun getPlacemarkAttributes(
                resources: Resources,
                place: Place
            ): PlacemarkAttributes? {
                var resourceId: Int
                var scale: Double
                if (place.population > LEVEL_0_POPULATION) {
                    resourceId = R.drawable.btn_rating_star_on_selected
                    scale = 1.3
                } else if (place.population > LEVEL_1_POPULATION) {
                    resourceId = R.drawable.btn_rating_star_on_pressed
                    scale = 1.2
                } else if (place.population > LEVEL_2_POPULATION) {
                    resourceId = R.drawable.btn_rating_star_on_normal
                    scale = 1.1
                } else if (place.population > LEVEL_3_POPULATION) {
                    resourceId = R.drawable.btn_rating_star_off_selected
                    scale = 0.7
                } else if (place.population > LEVEL_4_POPULATION) {
                    resourceId = R.drawable.btn_rating_star_off_pressed
                    scale = 0.6
                } else {
                    resourceId = R.drawable.btn_rating_star_off_normal
                    scale = 0.6
                }
                if (place.type === PlacemarksDemoActivity.Place.NATIONAL_CAPITAL) {
                    resourceId = R.drawable.star_big_on
                    scale *= 2.5
                } else if (place.type === PlacemarksDemoActivity.Place.STATE_CAPITAL) {
                    resourceId = R.drawable.star_big_on
                    scale *= 1.79
                }
                // Generate a cache key for this symbol
                val iconKey = "$resources-$resourceId-$scale"
                // Look for an attribute bundle in our cache and determine if the cached reference is valid
                val reference =
                    iconCache[iconKey]
                var placemarkAttributes = reference?.get()
                // Create the attributes if they haven't been created yet or if they've been released
                if (placemarkAttributes == null) { // Create the attributes bundle and add it to the cache.
                    // The actual bitmap will be lazily (re)created using a factory.
                    placemarkAttributes = createPlacemarkAttributes(resources, resourceId, scale)
                    requireNotNull(placemarkAttributes) { "Cannot generate a icon for: $iconKey" }
                    // Add a weak reference to the attribute bundle to our cache
                    iconCache[iconKey] = WeakReference(placemarkAttributes)
                }
                return placemarkAttributes
            }

            protected fun createPlacemarkAttributes(
                resources: Resources?, @DrawableRes resourceId: Int,
                scale: Double
            ): PlacemarkAttributes? {
                return PlacemarkAttributes().apply {
                    this.imageSource = ImageSource.fromResource(resourceId)
                    this.imageScale = scale
                    this.minimumImageScale = 0.5
                }
            }
        }

        protected val resources: Resources

        protected val place: Place

        protected var lastLevelOfDetail = -1

        protected var lastHighlightState = false

        protected var attributes: PlacemarkAttributes? = null

        constructor(
            resources: Resources,
            place: Place
        ) {
            this.resources = resources
            this.place = place
        }

        override fun selectLevelOfDetail(rc: RenderContext, placemark: Placemark?, cameraDistance: Double) {
            val highlighted = placemark!!.isHighlighted()
            val highlightChanged = lastHighlightState != highlighted

            // Determine the attributes based on the distance from the camera to the placemark
            // Determine the attributes based on the distance from the camera to the placemark
            if (cameraDistance > LEVEL_0_DISTANCE) {
                if (lastLevelOfDetail != LEVEL_0 || highlightChanged) {
                    if (place.population > LEVEL_0_POPULATION || place.isCapital()) {
                        attributes = getPlacemarkAttributes(resources, place)
                    } else {
                        attributes = null
                    }
                    lastLevelOfDetail = LEVEL_0
                }
            } else if (cameraDistance > LEVEL_1_DISTANCE) {
                if (lastLevelOfDetail != LEVEL_1 || highlightChanged) {
                    if (place.population > LEVEL_1_POPULATION || place.isCapital()) {
                        attributes = getPlacemarkAttributes(resources, place)
                    } else {
                        attributes = null
                    }
                    lastLevelOfDetail = LEVEL_1
                }
            } else if (cameraDistance > LEVEL_2_DISTANCE) {
                if (lastLevelOfDetail != LEVEL_2 || highlightChanged) {
                    if (place.population > LEVEL_2_POPULATION || place.isCapital()) {
                        attributes = getPlacemarkAttributes(resources, place)
                    } else {
                        attributes = null
                    }
                    lastLevelOfDetail = LEVEL_2
                }
            } else if (cameraDistance > LEVEL_3_DISTANCE) {
                if (lastLevelOfDetail != LEVEL_3 || highlightChanged) {
                    if (place.population > LEVEL_3_POPULATION || place.isCapital()) {
                        attributes = getPlacemarkAttributes(resources, place)
                    } else {
                        attributes = null
                    }
                    lastLevelOfDetail = LEVEL_3
                }
            } else if (cameraDistance > LEVEL_4_DISTANCE) {
                if (lastLevelOfDetail != LEVEL_4 || highlightChanged) {
                    if (place.population > LEVEL_4_POPULATION || place.isCapital()) {
                        attributes = getPlacemarkAttributes(resources, place)
                    } else {
                        attributes = null
                    }
                    lastLevelOfDetail = LEVEL_4
                }
            } else {
                if (lastLevelOfDetail != LEVEL_5 || highlightChanged) {
                    attributes = getPlacemarkAttributes(resources, place)
                    lastLevelOfDetail = LEVEL_5
                }
            }

            if (highlightChanged) { // Use a distinct set attributes when highlighted, otherwise used the shared attributes
                if (highlighted) { // Create a copy of the shared attributes bundle and increase the scale
                    attributes ?.let{
                        attributes = PlacemarkAttributes(it).apply { this.imageScale = (it.imageScale * 2.0) }
                    }
                }
            }
            lastHighlightState = highlighted

            // Update the placemark's attributes bundle
            attributes ?.let {  placemark.attributes = it }
        }

    }

    class IconBitmapFactory : ImageSource.BitmapFactory {

        companion object {
            /**
             * The default icon to use when the renderer cannot render an image.
             */
            protected var defaultImage = BitmapFactory.decodeResource(
                Resources.getSystem(),
                R.drawable.ic_dialog_alert
            ) // Warning sign


            protected var options = defaultAndroidBitmapFactoryOptions()


            protected fun defaultAndroidBitmapFactoryOptions(): BitmapFactory.Options? {
                val options = BitmapFactory.Options()
                options.inScaled = false // suppress default image scaling; load the image in its native dimensions
                return options
            }
        }

        protected val resources: Resources

        protected val resourceId: Int

        constructor(resources: Resources, @DrawableRes resourceId: Int) {
            this.resources = resources
            this.resourceId = resourceId
        }

        override fun createBitmap(): Bitmap? {
            println("createBitmap called")
            // Use an Android BitmapFactory to convert the resource to a Bitmap
            // Use an Android BitmapFactory to convert the resource to a Bitmap
            val bitmap = BitmapFactory.decodeResource(
                resources,
                resourceId,
                options
            )
            if (bitmap == null) {
                Logger.logMessage(
                    Logger.ERROR,
                    "IconBitmapFactory",
                    "createBitmap",
                    "Failed to decode resource for " + resourceId
                )
                // TODO: File JIRA issue - must return a valid bitmap, else the ImageRetriever repeatedly attempts to create the bitmap.
                return defaultImage
            }
            // Return the bitmap
            // Return the bitmap
            return bitmap
        }

    }

    class Place {
        companion object {
            const val PLACE = "Populated Place"

            const val COUNTY_SEAT = "County Seat"

            const val STATE_CAPITAL = "State Capital"

            const val NATIONAL_CAPITAL = "National Capital"
        }

        val position: Position

        val name: String

        var type: String

        val population: Int

        constructor(
            position: Position,
            name: String,
            feature2: String,
            population: Int
        ) {
            this.position = position
            this.name = name
            this.population = population
            // FEATURE2 may contain  multiple types; "-999" is used for a regular populated place
            // Here we extract the most important type
            if (feature2.contains(NATIONAL_CAPITAL)) {
                type = NATIONAL_CAPITAL
            } else if (feature2.contains(STATE_CAPITAL)) {
                type = STATE_CAPITAL
            } else if (feature2.contains(COUNTY_SEAT)) {
                type = COUNTY_SEAT
            } else {
                type = PLACE
            }
        }

        fun isCapital(): Boolean {
            return type === NATIONAL_CAPITAL || type === STATE_CAPITAL
        }


        override fun toString(): String {
            return "Place{" +
                    "name='" + name + '\'' +
                    ", position=" + position +
                    ", type='" + type + '\'' +
                    ", population=" + population +
                    '}'
        }
    }

    inner class PickController : BasicWorldWindowController() {
        protected var pickedObject // last picked object from onDown events
                : Any? = null

        protected var selectedObject // last "selected" object from single tap
                : Any? = null

        protected var pickGestureDetector =
            GestureDetector(applicationContext, object : SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent): Boolean {
                    pick(event)
                    return false
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    toggleSelection()
                    return false
                }
            })

        override fun onTouchEvent(event: MotionEvent): Boolean { // Allow pick listener to process the event first.
            val consumed = pickGestureDetector.onTouchEvent(event)
            return if (!consumed) {
                super.onTouchEvent(event)
            } else consumed
        }

        /**
         * Performs a pick at the tap location.
         */
        fun pick(event: MotionEvent) { // Forget our last picked object
            pickedObject = null
            // Perform a new pick at the screen x, y
            val pickList = getWorldWindow().pick(event.x, event.y)
            // Get the top-most object for our new picked object
            val topPickedObject = pickList.topPickedObject()
            if (topPickedObject != null) {
                pickedObject = topPickedObject.userObject
            }
        }

        /**
         * Toggles the selected state of a picked object.
         */
        fun toggleSelection() {
            // Display the highlight or normal attributes to indicate the
            // selected or unselected state respectively.
            if (pickedObject is Highlightable) { // Determine if we've picked a "new" object so we know to deselect the previous selection
                val isNewSelection = pickedObject !== selectedObject
                // Only one object can be selected at time; deselect any previously selected object
                if (isNewSelection && selectedObject is Highlightable) {
                    (selectedObject as Highlightable?)?.setHighlighted(false)
                }
                // Show the selection by showing its highlight attributes and enunciating the name
                if (isNewSelection && pickedObject is Renderable) {
                    Toast.makeText(
                        applicationContext,
                        (pickedObject as Renderable).displayName ,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                (pickedObject as Highlightable).setHighlighted(isNewSelection)
                getWorldWindow().requestRedraw()
                // Track the selected object
                selectedObject = if (isNewSelection) pickedObject else null
            }
        }
    }

    /**
     * CreatePlacesTask is an AsyncTask that initializes the place icons on a background thread. It must be created and
     * executed on the UI Thread.
     */
    inner class CreatePlacesTask : AsyncTask<Void?, String?, Void?>() {
        private val places = ArrayList<Place>()
        private val placeLayer: RenderableLayer = RenderableLayer()
        private var numPlacesCreated = 0
        /**
         * Loads the ntad_place database and creates the placemarks on a background thread. The [RenderableLayer]
         * objects for the place icons have not been attached to the WorldWind at this stage, so its safe to perform
         * this operation on a background thread.  The layers will be added to the WorldWindow in onPostExecute.
         */
         override fun doInBackground(vararg notUsed: Void?): Void? {
            loadPlacesDatabase()
            createPlaceIcons()
            return null
        }

         override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            statusText?.setText(values[0])
        }

        /**
         * Updates the WorldWindow layer list on the UI Thread.
         */
        override fun onPostExecute(notUsed: Void?) {
            super.onPostExecute(notUsed)
            getWorldWindow().layers.addLayer(placeLayer)
            statusText?.setText(
                String.format(
                    Locale.US,
                    "%,d US places created",
                    numPlacesCreated
                )
            )
            getWorldWindow().requestRedraw()
        }

        /**
         * Called by doInBackground(); loads the National Transportation Atlas Database (NTAD) place data.
         */
        private fun loadPlacesDatabase() {
            publishProgress("Loading NTAD place database...")
            var reader: BufferedReader? = null
            try {
                val `in`: InputStream = getResources().openRawResource(R.raw.ntad_place)
                reader = BufferedReader(InputStreamReader(`in`))
                // Process the header in the first line of the CSV file ...
                var line = reader.readLine()
                val headers =
                    Arrays.asList(*line.split(",").toTypedArray())
                val LAT = headers.indexOf("LATITUDE")
                val LON = headers.indexOf("LONGITUDE")
                val NAM = headers.indexOf("NAME")
                val POP = headers.indexOf("POP_2010")
                val TYP = headers.indexOf("FEATURE2")
                while (reader.readLine().also { line = it } != null) {
                    val fields = line.split(",").toTypedArray()
                    val place = Place(
                        Position.fromDegrees(fields[LAT].toDouble(), fields[LON].toDouble(), 0.0),
                        fields[NAM],
                        fields[TYP], fields[POP].toInt()
                    )
                    places.add(place)
                }
            } catch (e: IOException) {
                Logger.log(Logger.ERROR, "Exception attempting to read NTAD Place database")
            } finally {
                WWUtil.closeSilently(reader)
            }
        }

        /**
         * Called by doInBackground(); creates place icons from places collection and adds them to the places layer.
         */
        private fun createPlaceIcons() {
            publishProgress("Creating place icons...")
            for (place in places) {
                val placemark = Placemark(place.position, null, place.name)
                placemark.levelOfDetailSelector = (PlaceLevelOfDetailSelector(getResources(), place))
                placemark.eyeDistanceScaling = (true)
                placemark.eyeDistanceScalingThreshold = (PlaceLevelOfDetailSelector.LEVEL_1_DISTANCE.toDouble())
                placeLayer.addRenderable(placemark)
                numPlacesCreated++
            }
        }

    }
}