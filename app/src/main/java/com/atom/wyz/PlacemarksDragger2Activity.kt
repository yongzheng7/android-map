package com.atom.wyz

import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.controller.BasicWorldWindowController
import com.atom.map.geom.Offset
import com.atom.map.geom.Position
import com.atom.map.geom.Sector
import com.atom.map.geom.observer.LookAt
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.Placemark
import com.atom.map.layer.render.SurfaceImage
import com.atom.map.layer.render.shape.Movable
import com.atom.wyz.worldwind.R
import java.util.*

class PlacemarksDragger2Activity : BasicWorldWindActivity() {
    /**
     * A naive gesture listener for a WorldWindow that dispatches motion events to a picker and dragger.
     */
    class NaivePickDragListener
    /**
     * Constructor.
     *
     * @param wwd The WorldWindow to be associated with this Gesture Listener.
     */(protected var wwd: WorldWindow) : SimpleOnGestureListener() {
        /**
         * The last picked object. May be null.
         */
        protected var pickedObject: Any? = null

        /**
         * Implements the picking behavior; picks the object(s) at the tap location.
         *
         * @param event ACTION_DOWN event
         *
         * @return false; by not consuming this event, we allow it to pass on to WorldWindow navigation gesture handlers
         */
        override fun onDown(event: MotionEvent): Boolean {
            pick(event)
            return false
        }

        /**
         * Implements the naive dragging behavior: all "Movable" objects can be dragged.
         *
         * @param downEvent not used.
         * @param moveEvent not used.
         * @param distanceX X offset
         * @param distanceY Y offset
         *
         * @return The result of a Dragger's drag operation; otherwise false
         */
        override fun onScroll(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (pickedObject !is Movable) {
                return false
            }
            val movable = pickedObject as Movable?
            return drag(movable, distanceX, distanceY)
        }

        /**
         * Performs a pick operation at the event's screen x,y position.
         *
         * @param event Typically an ACTION_DOWN event
         */
        protected fun pick(event: MotionEvent) { // Forget our last picked object
            pickedObject = null
            // Perform a new pick at the screen x, y
            val pickList = wwd.pick(event.x, event.y)
            // Get the top-most object for our new picked object
            val topPickedObject = pickList.topPickedObject()
            if (topPickedObject != null) {
                pickedObject = topPickedObject.userObject
            }
        }

        protected fun drag(
            movable: Movable?,
            distanceX: Float,
            distanceY: Float
        ): Boolean { // First we compute the screen coordinates of the position's ground point.  We'll apply the
// screen X and Y drag distances to this point, from which we'll compute a new ground position,
// wherein we restore the original position's altitude.
// Get a 'copy' of the movable's reference position
            val position = Position(movable!!.getReferencePosition()!!)
            val screenPt = PointF()
            val altitude = position.altitude
            // Get the screen x,y of the ground position (e.g., the base of the leader line for above ground Placemarks)
            if (!wwd.geographicToScreenPoint(
                    position.latitude,
                    position.longitude,
                    0.0 /*altitude*/,
                    screenPt
                )
            ) { // Probably clipped by near/far clipping plane.
                return false
            }
            // Shift the ground position's lat and lon to correspond to the screen x and y offsets ...
            if (!wwd.screenPointToGeographic(screenPt.x - distanceX, screenPt.y - distanceY, position)) {
                return false // Probably off the globe
            }
            // ... and restore the altitude
            position.altitude = altitude
            // Finally, perform the actual move on the object
            movable.moveTo(wwd.globe, position)
            // Request a redraw to visualize the change in position
            wwd.requestRedraw()
            // Consume the event
            return true
        }

    }

    class CustomWorldWindowController : BasicWorldWindowController() {
        protected var gestureDetectors = ArrayList<GestureDetector>()
        protected var isDragging = false
        fun addGestureDetector(gestureDetector: GestureDetector) {
            gestureDetectors.add(gestureDetector)
        }

        fun removeGestureDetector(gestureDetector: GestureDetector?) {
            gestureDetectors.remove(gestureDetector)
        }

        /**
         * Delegates events to the pre and post navigation handlers.
         */
        override fun onTouchEvent(event: MotionEvent): Boolean {
            var consumed = false
            // Allow an application defined GestureDetector to process events before
// the globe's navigation handlers to.  Typically, picking and dragging
// occurs here.
            for (gestureDetector in gestureDetectors) {
                consumed = gestureDetector.onTouchEvent(event)
                // Android doesn't send all ACTION_MOVE events to onScroll! If a drag
// operation becomes too slow for Android it doesn't call onScroll.
// Thus the message is not consumed and it is passed on to the navigation
// gesture handler to be interpreted as a pan gesture. To prevent this
// we have to implement an isDragging state property.
                if (consumed && event.action == MotionEvent.ACTION_MOVE) { // If we've consumed an ACTION_MOVE (onScroll) message
// then assume that we must be dragging.
                    isDragging = true
                }
            }
            // Any ACTION_UP message means we're not dragging
            if (event.action == MotionEvent.ACTION_UP) { // If we're not dragging, then ACTION_MOVE messages are navigation gestures.
                isDragging = false
            }
            // If we're dragging, we preempt the globe's pan navigation behavior.
// When disabled, this setting prevents ACTION_MOVE messages that weren't
// sent to onScroll while dragging from being interpreted as pan gestures.
//super.panRecognizer.setEnabled(!isDragging);
// Unconsumed messages are sent to the globe's navigation gesture handlers
            if (!consumed && !isDragging) {
                consumed = super.onTouchEvent(event)
            }
            return consumed
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get a reference to the WorldWindow view
        // Get a reference to the WorldWindow view
        val wwd = getWorldWindow()

        // Add picking and dragging support to the World Window...

        // Create a WorldWindController that will dispatch MotionEvents to our GestureDetector(s)
        // Add picking and dragging support to the World Window...
// Create a WorldWindController that will dispatch MotionEvents to our GestureDetector(s)
        val worldWindowController: CustomWorldWindowController =
            CustomWorldWindowController()

        // Add a gesture detector to the WorldWindowController that handles for pick and drag events
        // Add a gesture detector to the WorldWindowController that handles for pick and drag events
        worldWindowController.addGestureDetector(GestureDetector(applicationContext,
            NaivePickDragListener(wwd)
        ))

        // Replace the default WorldWindowController with our own
        // Replace the default WorldWindowController with our own
        wwd.worldWindowController  = (worldWindowController)
        // TODO: make this controller the BasicWorldWindowController

        // Add a layer for placemarks to the WorldWindow
        // TODO: make this controller the BasicWorldWindowController
// Add a layer for placemarks to the WorldWindow
        val layer = RenderableLayer("Renderables")
        wwd.layers.addLayer(layer)

        // Create SurfaceImages to display an Android resource showing the NASA logo.
        // Create SurfaceImages to display an Android resource showing the NASA logo.
        val smallImage =
            SurfaceImage(
                Sector(
                    34.2,
                    -119.2,
                    0.1,
                    0.12
                ),
                ImageSource.fromResource(R.drawable.nasa_logo)
            )
        val bigImage =
            SurfaceImage(
                Sector(
                    36.0,
                    -120.0,
                    5.0,
                    10.0
                ),
                ImageSource.fromResource(R.drawable.nasa_logo)
            )

        layer.addRenderable(smallImage)
        layer.addRenderable(bigImage)

        // Create a few placemarks with highlight attributes and add them to the layer
        // Create a few placemarks with highlight attributes and add them to the layer
        layer.addRenderable(
            createAirportPlacemark(
                Position.fromDegrees(
                    34.2000,
                    -119.2070,
                    0.0
                ), "Oxnard Airport"
            )
        )
        layer.addRenderable(
            createAirportPlacemark(
                Position.fromDegrees(
                    34.2138,
                    -119.0944,
                    0.0
                ), "Camarillo Airport"
            )
        )
        layer.addRenderable(
            createAirportPlacemark(
                Position.fromDegrees(
                    34.1193,
                    -119.1196,
                    0.0
                ), "Pt Mugu Naval Air Station"
            )
        )
        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.200,
                    -119.207,
                    1000.0
                )
            )
        )
        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.210,
                    -119.150,
                    2000.0
                )
            )
        )
        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.150,
                    -119.150,
                    3000.0
                )
            )
        )

        // Position the viewer to look near the airports
        // Position the viewer to look near the airports
        val lookAt: LookAt =
            LookAt().set(34.15, -119.15, 0.0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0.0 /*heading*/, 45.0 /*tilt*/, 0.0 /*roll*/)
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)

    }

    companion object {
        /**
         * Helper method to create aircraft placemarks.
         */
        private fun createAircraftPlacemark(position: Position): Placemark {
            val placemark: Placemark =
                Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.aircraft_fighter))
            placemark.attributes.apply {
                this.imageOffset = Offset.bottomCenter()
                this.imageScale = 2.0
                this.drawLeader = true
            }
            return placemark
        }

        /**
         * Helper method to create airport placemarks.
         */
        private fun createAirportPlacemark(position: Position, airportName: String): Placemark {
            val placemark: Placemark =
                Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.airport_terminal))
            placemark.attributes.apply {
                this.imageOffset = Offset.bottomCenter()
                this.imageScale = 2.0
            }
            placemark.displayName = airportName
            return placemark
        }
    }
}