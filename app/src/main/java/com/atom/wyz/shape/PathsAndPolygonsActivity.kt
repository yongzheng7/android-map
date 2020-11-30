package com.atom.wyz.shape

import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.Toast
import com.atom.map.WorldWind
import com.atom.map.controller.BasicWorldWindowController
import com.atom.map.geom.Offset
import com.atom.map.geom.Position
import com.atom.map.geom.SimpleColor
import com.atom.map.layer.RenderableLayer
import com.atom.map.renderable.Renderable
import com.atom.map.renderable.attribute.ShapeAttributes
import com.atom.map.renderable.attribute.TextAttributes
import com.atom.map.renderable.shape.Highlightable
import com.atom.map.renderable.shape.Label
import com.atom.map.renderable.shape.Path
import com.atom.map.renderable.shape.Polygon
import com.atom.map.util.Logger
import com.atom.map.util.WWUtil
import com.atom.wyz.base.BasicWorldWindActivity
import com.atom.wyz.worldwind.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class PathsAndPolygonsActivity : BasicWorldWindActivity() {


    protected var shapesLayer: RenderableLayer = RenderableLayer("Shapes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWorldWindow().layers.addLayer(shapesLayer)
        getWorldWindow().worldWindowController = PickController()

        CreateRenderablesTask().execute()
    }

    inner protected class CreateRenderablesTask : AsyncTask<Void?, Renderable?, Void?>() {
        private var numCountriesCreated = 0
        private var numHighwaysCreated = 0
        private var numPlacesCreated = 0
        private val random = Random(22) // for Random color fills.
        /**
         * Loads the world_highways and world_political_areas files a background thread. The [Renderable]
         * objects are added to the RenderableLayer on the UI thread via onProgressUpdate.
         */
         override fun doInBackground(vararg params: Void?): Void? {
            loadCountriesFile()
            loadHighways()
            loadPlaceNames()
            return null
        }

        /**
         * Updates the RenderableLayer on the UI Thread.
         *
         * @param renderables An array of Renderables (length = 1) to add to the shapes layer.
         */
        override fun onProgressUpdate(vararg values: Renderable?) {
            super.onProgressUpdate(*values)
            shapesLayer.addRenderable(values[0]!!)
            getWorldWindow().requestRedraw()
        }

        /**
         * Updates the WorldWindow layer list on the UI Thread.
         */
        override fun onPostExecute(notUsed: Void?) {
            super.onPostExecute(notUsed)
            getWorldWindow().requestRedraw()
        }

        private fun loadPlaceNames() { // Define the text attributes used for places
            val placeAttrs: TextAttributes = TextAttributes.defaults()
                .apply {
                this.typeface= (Typeface.DEFAULT_BOLD) // Override the normal Typeface
                this.textSize = (28f) // default size is 24
                this.textOffset = (Offset.bottomRight()) // anchor the label's bottom-right corner at its position
            }

            // Define the text attribute used for lakes
            val lakeAttrs: TextAttributes = TextAttributes.defaults()
                .apply {
                this.typeface = (Typeface.create("serif", Typeface.BOLD_ITALIC))
                this.textSize = (32f) // default size is 24
                this.textColor = (SimpleColor(
                    0f,
                    1f,
                    1f,
                    0.70f
                )) // cyan, with 7% opacity
                this.textOffset = (Offset.center()) // center the label over its position
            }
            // Load the place names
            var reader: BufferedReader? = null
            try {
                val `in` = resources.openRawResource(R.raw.world_placenames)
                reader = BufferedReader(InputStreamReader(`in`))
                // Process the header in the first line of the CSV file ...
                var line = reader.readLine()
                val headers =
                    Arrays.asList(*line.split(",").toTypedArray())
                val LAT = headers.indexOf("LAT")
                val LON = headers.indexOf("LON")
                val NAM = headers.indexOf("PLACE_NAME")
                // ... and process the remaining lines in the CSV
                while (reader.readLine().also { line = it } != null) {
                    val fields = line.split(",").toTypedArray()
                    val label = Label(
                        Position.fromDegrees(fields[LAT].toDouble(), fields[LON].toDouble(), 0.0),
                        fields[NAM],
                        if (fields[NAM].contains("Lake")) lakeAttrs else placeAttrs
                    )
                    label.displayName = label.text!!
                    // Add the Label object to the RenderableLayer on the UI Thread (see onProgressUpdate)
                    publishProgress(label)
                    numPlacesCreated++
                }
            } catch (e: IOException) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_placenames file.")
            } finally {
                WWUtil.closeSilently(reader)
            }
        }

        /**
         * Called by doInBackground(); loads the VMAP0 World Highways data.
         */
        private fun loadHighways() {
            val attrs =
                ShapeAttributes.defaults()
            attrs.outlineColor.set(1.0f, 1.0f, 0.0f, 1.0f)
            attrs.outlineWidth= (3f)
            val highlightAttrs =
                ShapeAttributes.defaults()
            highlightAttrs.outlineColor.set(1.0f, 0.0f, 0.0f, 1.0f)
            highlightAttrs.outlineWidth = (7f)
            var reader: BufferedReader? = null
            try {
                val `in`: InputStream = getResources().openRawResource(R.raw.world_highways)
                reader = BufferedReader(InputStreamReader(`in`))
                // Process the header in the first line of the CSV file ...
                var line = reader.readLine()
                val headers =
                    Arrays.asList(*line.split(",").toTypedArray())
                val WKT = headers.indexOf("WKT")
                val HWY = headers.indexOf("Highway")
                // ... and process the remaining lines in the CSV
                val WKT_START = "\"LINESTRING ("
                val WKT_END = ")\""
                while (reader.readLine().also {
                        line = it
                    } != null) { // Extract the "well known text"  feature and the attributes
                    // e.g.: "LINESTRING (x.xxx y.yyy,x.xxx y.yyy)",text
                    val featureBegin = line.indexOf(WKT_START) + WKT_START.length
                    val featureEnd = line.indexOf(WKT_END, featureBegin)
                    val feature = line.substring(featureBegin, featureEnd)
                    val attributes = line.substring(featureEnd + WKT_END.length + 1)
                    // Buildup the Path. Coordinate tuples are separated by ",".
                    val positions: MutableList<Position> = ArrayList<Position>()
                    val tuples = feature.split(",").toTypedArray()
                    for (i in tuples.indices) { // The XY tuple components a separated by a space
                        val xy = tuples[i].split(" ").toTypedArray()
                        positions.add(Position.fromDegrees(xy[1].toDouble(), xy[0].toDouble(), 0.0))
                    }
                    val path = Path(
                        positions,
                        attrs
                    )
                    path.highlightAttributes  = (highlightAttrs)
                    path.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
                    path.pathType = (WorldWind.LINEAR)
                    path.followTerrain = (true) // essential for preventing long segments from intercepting ellipsoid.
                    path.displayName = (attributes)
                    publishProgress(path)
                    numHighwaysCreated++
                }
            } catch (e: IOException) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_highways file.")
            } finally {
                WWUtil.closeSilently(reader)
            }
        }

        /**
         * Called by doInBackground(); loads the VMAP0 World Political Areas data.
         */
        private fun loadCountriesFile() {
            val commonAttrs =
                ShapeAttributes.defaults()
            commonAttrs.interiorColor.set(1.0f, 1.0f, 0.0f, 0.5f)
            commonAttrs.outlineColor.set(0.0f, 0.0f, 0.0f, 1.0f)
            commonAttrs.outlineWidth = (3f)
            val highlightAttrs =
                ShapeAttributes.defaults()
            highlightAttrs.interiorColor.set(1.0f, 1.0f, 1.0f, 0.5f)
            highlightAttrs.outlineColor.set(1.0f, 1.0f, 1.0f, 1.0f)
            highlightAttrs.outlineWidth = (5f)
            var reader: BufferedReader? = null
            try {
                val `in`: InputStream = getResources().openRawResource(R.raw.world_political_boundaries)
                reader = BufferedReader(InputStreamReader(`in`))
                // Process the header in the first line of the CSV file ...
                var line = reader.readLine()
                val headers =
                    Arrays.asList(*line.split(",").toTypedArray())
                val GEOMETRY = headers.indexOf("WKT")
                val NAME = headers.indexOf("COUNTRY_NA")
                // ... and process the remaining lines in the CSV
                val WKT_START = "\"POLYGON ("
                val WKT_END = ")\""
                while (reader.readLine().also {
                        line = it
                    } != null) { // Extract the "well known text" feature and the attributes
// e.g.: "POLYGON ((x.xxx y.yyy,x.xxx y.yyy), (x.xxx y.yyy,x.xxx y.yyy))",text,more text,...
                    val featureBegin = line.indexOf(WKT_START) + WKT_START.length
                    val featureEnd = line.indexOf(WKT_END, featureBegin) + WKT_END.length
                    val feature = line.substring(featureBegin, featureEnd)
                    val attributes = line.substring(featureEnd + 1)
                    val fields = attributes.split(",").toTypedArray()
                    val polygon =
                        Polygon()
                    polygon.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
                    polygon.pathType = (WorldWind.LINEAR)
                    polygon.followTerrain =
                        (true) // essential for preventing long segments from intercepting ellipsoid.
                    polygon.displayName = (fields[1])
                    polygon.attributes = (ShapeAttributes.defaults(
                        commonAttrs
                    ))
                    polygon.attributes!!.interiorColor =
                        SimpleColor(
                            random.nextFloat(),
                            random.nextFloat(),
                            random.nextFloat(),
                            0.3f
                        )
                    polygon.highlightAttributes = (highlightAttrs)
                    // Process all the polygons within this feature by creating "boundaries" for each.
                    // Individual polygons are bounded by "(" and ")"
                    var polyStart = feature.indexOf("(")
                    while (polyStart >= 0) {
                        val polyEnd = feature.indexOf(")", polyStart)
                        val poly = feature.substring(polyStart + 1, polyEnd)
                        // Buildup the Polygon boundaries. Coordinate tuples are separated by ",".
                        val positions: MutableList<Position> = ArrayList<Position>()
                        val tuples = poly.split(",").toTypedArray()
                        for (i in tuples.indices) { // The XY tuple components a separated by a space
                            val xy = tuples[i].split(" ").toTypedArray()
                            positions.add(Position.fromDegrees(xy[1].toDouble(), xy[0].toDouble(), 0.0))
                        }
                        polygon.addBoundary(positions)
                        // Locate the next polygon in the feature
                        polyStart = feature.indexOf("(", polyEnd)
                    }
                    publishProgress(polygon)
                    numCountriesCreated++
                }
            } catch (e: IOException) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_highways file.")
            } finally {
                WWUtil.closeSilently(reader)
            }
        }
    }

    inner class PickController : BasicWorldWindowController() {
        protected var pickedObjects =
            ArrayList<Any>() // last picked objects from onDown events
        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the "pick" events.
         */
        protected var pickGestureDetector =
            GestureDetector(getApplicationContext(), object : SimpleOnGestureListener() {
                override fun onSingleTapUp(event: MotionEvent): Boolean {
                    pick(event) // Pick the object(s) at the tap location
                    return true
                }
            })

        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        override fun onTouchEvent(event: MotionEvent): Boolean {
            var consumed: Boolean = super.onTouchEvent(event)
            if (!consumed) {
                consumed = pickGestureDetector.onTouchEvent(event)
            }
            return consumed
        }

        /**
         * Performs a pick at the tap location.
         */
        fun pick(event: MotionEvent) {
            val PICK_REGION_SIZE = 40 // pixels
            // Forget our last picked objects
            togglePickedObjectHighlights()
            pickedObjects.clear()
            // Perform a new pick at the screen x, y
            val pickList = getWorldWindow().pickShapesInRect(
                event.x - PICK_REGION_SIZE / 2,
                event.y - PICK_REGION_SIZE / 2,
                PICK_REGION_SIZE.toFloat(), PICK_REGION_SIZE.toFloat()
            )
            // pickShapesInRect can return multiple objects, i.e., they're may be more that one 'top object'
            // So we iterate through the list instead of calling pickList.topPickedObject which returns the
            // arbitrary 'first' top object.
            for (i in 0 until pickList.count()) {
                if (pickList.pickedObjectAt(i)!!.isOnTop) {
                    pickedObjects.add(pickList.pickedObjectAt(i)!!.userObject!!)
                }
            }
            togglePickedObjectHighlights()
        }

        /**
         * Toggles the highlighted state of a picked object.
         */
        fun togglePickedObjectHighlights() {
            var message = ""
            for (pickedObject in pickedObjects) {
                if (pickedObject is Highlightable) {
                    val highlightable: Highlightable = pickedObject as Highlightable
                    highlightable.highlighted = (!highlightable.highlighted)
                    if (highlightable.highlighted) {
                        if (message.isNotEmpty()) {
                            message += ", "
                        }
                        message += (highlightable as Renderable).displayName
                    }
                }
            }
            if (!message.isEmpty()) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
            }
            getWorldWindow().requestRedraw()
        }
    }
}