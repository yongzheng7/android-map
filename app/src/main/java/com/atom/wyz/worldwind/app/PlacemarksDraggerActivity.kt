package com.atom.wyz.worldwind.app

import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.atom.wyz.worldwind.BasicWorldWindowController
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.pick.PickedObject
import com.atom.wyz.worldwind.pick.PickedObjectList
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.render.Renderable
import com.atom.wyz.worldwind.render.SurfaceImage
import com.atom.wyz.worldwind.shape.Movable
import java.util.*

class PlacemarksDraggerActivity : BasicWorldWindActivity() {
    interface Dragger {
        fun drag(
            wwd: WorldWindow,
            movable: Movable,
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean
    }

    class SimpleDragger : Dragger {
        /**
         * Drags a Movable object to a new position determined by the events' x and y screen offsets.
         *
         * @param wwd       The WorldWindow screen object
         * @param movable   The object to be moved
         * @param downEvent Initial ACTION_DOWN event; not used
         * @param moveEvent Current ACTION_MOVE event; not used
         * @param distanceX delta x screen offset to move
         * @param distanceY delta y screen offset to move
         *
         * @return true if the object was moved, otherwise false.
         */
        override fun drag(
            wwd: WorldWindow,
            movable: Movable,
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // First we compute the screen coordinates of the position's ground point.  We'll apply the
            // screen X and Y drag distances to this point, from which we'll compute a new ground position,
            // wherein we restore the original position's altitude.
            // Get a 'copy' of the movable's reference position
            val position = Position(movable.getReferencePosition()!!)
            val screenPt = PointF()
            val altitude: Double = position.altitude
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
            // Shift the ground position's lat and lon to correspond to the screen offsets
            if (!screenPointToGroundPosition(
                    wwd,
                    screenPt.x - distanceX,
                    screenPt.y - distanceY,
                    position
                )
            ) {
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

        companion object {
            /**
             * Converts a screen point to the geographic coordinates on the globe. TODO: Move this method to the
             * WorldWindow.
             *
             * @param screenX X coordinate in Android screen coordinates
             * @param screenY Y coordinate in Android screen coordinates
             * @param result  Pre-allocated Position receives the geographic coordinates
             *
             * @return true if the screen point could be converted; false if the screen point is not on the globe
             */
            fun screenPointToGroundPosition(
                wwd: WorldWindow,
                screenX: Float,
                screenY: Float,
                result: Position
            ): Boolean {
                val ray = Line()
                val intersection = Vec3()
                if (wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
                    val globe: Globe = wwd.globe
                    if (globe.intersect(ray, intersection)) {
                        globe.cartesianToGeographic(intersection.x, intersection.y, intersection.z, result)
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * A simple picker for a WorldWindow that records the last picked top object.
     */
    class SimplePicker {
        /**
         * The last picked object. May be null.
         */
        var pickedObject: Any? = null

        /**
         * Performs a pick at the tap location.
         *
         * @param wwd   The WorldWindow that will perform the pick operation
         * @param event The event containing the picked screen location
         */
        fun pick(wwd: WorldWindow, event: MotionEvent) { // Forget our last picked object
            pickedObject = null
            // Perform a new pick at the screen x, y
            val pickList: PickedObjectList = wwd.pick(event.x, event.y)
            // Get the top-most object for our new picked object
            val topPickedObject: PickedObject? = pickList.topPickedObject()
            if (topPickedObject != null) {
                pickedObject = topPickedObject.userObject
            }
        }
    }

    /**
     * A simple gesture listener for a WorldWindow that dispatches select motion events to a picker and optional
     * dragger.
     */
    class PickDragListener
    /**
     * Constructor.
     *
     * @param wwd The WorldWindow associated with this Gesture Listener.
     */(protected var wwd: WorldWindow) : SimpleOnGestureListener() {
        protected var picker = SimplePicker()
        /**
         * onDown implements the picking behavior.
         *
         * @param event
         *
         * @return
         */
        override fun onDown(event: MotionEvent): Boolean { // Pick the object(s) at the tap location
            picker.pick(wwd, event)
            return false // By not consuming this event, we allow it to pass on to WorldWindow navigation gesture handlers
        }

        /**
         * onScroll implements the dragging behavior. This implementation only moves Movable Renderables that have a
         * "Dragger" in their user properties.
         *
         * @param downEvent
         * @param moveEvent
         * @param distanceX
         * @param distanceY
         *
         * @return
         */
        override fun onScroll(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (picker.pickedObject !is Renderable) {
                return false
            }
            if (picker.pickedObject !is Movable) {
                return false
            }
            val shape: Renderable? = picker.pickedObject as Renderable?
            val movable = picker.pickedObject as Movable?
            // Interrogate the shape for a Dragger property
            val dragger = shape?.getUserProperty(Dragger::class.java)?.let { it as Dragger } ?: let { null }
            // Invoke the shapes's dragger
            return dragger?.drag(wwd, movable!!, downEvent, moveEvent, distanceX, distanceY) ?: false
        }

    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    class CustomWorldWindowController(context: Context, wwd: WorldWindow?) :
        BasicWorldWindowController() {
        private val preNavGestureDetector: GestureDetector?
        private val postNavGestureDetector: GestureDetector? = null
        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        override fun onTouchEvent(event: MotionEvent): Boolean {
            var consumed = false
            if (preNavGestureDetector != null) {
                consumed = preNavGestureDetector.onTouchEvent(event)
            }
            if (!consumed) {
                consumed = super.onTouchEvent(event)
            }
            if (!consumed && postNavGestureDetector != null) {
                consumed = postNavGestureDetector.onTouchEvent(event)
            }
            return consumed
        }

        init {
            preNavGestureDetector = GestureDetector(context, PickDragListener(wwd!!))
        }
    }

    class MovableSurfaceImage(sector: Sector, imageSource: ImageSource) :
        SurfaceImage(sector, imageSource), Movable {
        /**
         * A position associated with the object that indicates its aggregate geographic position. The chosen position
         * varies among implementers of this interface. For objects defined by a list of positions, the reference
         * position is typically the first position in the list. For symmetric objects the reference position is often
         * the center of the object. In many cases the object's reference position may be explicitly specified by the
         * application.
         *
         * @return the object's reference position, or null if no reference position is available.
         */
        override fun getReferencePosition(): Position {
            val sector: Sector = super.sector
            return Position(sector.minLatitude, sector.minLongitude, 0.0)
        }

        /**
         * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
         * North.
         *
         * @param globe    the globe on which to move the shape.
         * @param position the new position of the shape's reference position.
         */
        override fun moveTo(globe: Globe, position: Position?) {
            val oldRef = getReferencePosition() ?: return

            val sector: Sector = super.sector
            val swCorner = Location(sector.minLatitude, sector.minLongitude)
            val nwCorner = Location(sector.maxLatitude, sector.minLongitude)
            val seCorner = Location(sector.minLatitude, sector.maxLongitude)

            val EAST = 90.0
            val WEST = 270.0
            val NORTH = 0.0
            val distanceRadians = oldRef.greatCircleDistance(swCorner)
            val azimuthDegrees = oldRef.greatCircleAzimuth(swCorner)
            val widthRadians = swCorner.rhumbDistance(seCorner)
            val heightRadians = swCorner.rhumbDistance(nwCorner)

            // Compute a new positions for the SW corner
            position!!.greatCircleLocation(azimuthDegrees, distanceRadians, swCorner)

            // Compute the SE corner, using the original width
            swCorner.rhumbLocation(EAST, widthRadians, seCorner)
            if (Location.locationsCrossAntimeridian(
                    Arrays.asList(
                        *arrayOf(
                            swCorner,
                            seCorner
                        )
                    )
                )
            ) {
                // TODO: create issue regarding Sector Antimeridian limitation
                // There's presently no support for placing SurfaceImages crossing the Anti-meridian
                // Snap the image to the other side of the date line
                val dragAzimuth = oldRef.greatCircleAzimuth(position)
                if (dragAzimuth < 0) { // Set the East edge of the sector to the dateline
                    seCorner.set(seCorner.latitude, 180.0)
                    seCorner.rhumbLocation(WEST, widthRadians, swCorner)
                } else { // Set the West edge of the sector to the dateline
                    swCorner.set(swCorner.latitude, (-180).toDouble())
                    swCorner.rhumbLocation(EAST, widthRadians, seCorner)
                }
            }
            // Compute the NW corner with the original height
            swCorner.rhumbLocation(NORTH, heightRadians, nwCorner)

            // Compute the delta lat and delta lon values from the new SW position
            val dLat = nwCorner.latitude - swCorner.latitude
            val dLon = seCorner.longitude - swCorner.longitude

            // Update the image's sector
            super.sector[swCorner.latitude, swCorner.longitude, dLat] = dLon

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get a reference to the WorldWindow view
        val wwd = getWorldWindow()
        // Override the World Window's built-in navigation behavior by adding picking support.
        wwd.worldWindowController = (CustomWorldWindowController(applicationContext, wwd))
        // Add a layer for placemarks to the WorldWindow
        val layer = RenderableLayer("Placemarks")
        wwd.layers.addLayer(layer)
        // Configure a Surface Image to display an Android resource showing the NASA logo.
        val smallImage: SurfaceImage =
            MovableSurfaceImage(Sector(34.2, -119.2, 0.1, 0.12), ImageSource.fromResource(R.drawable.nasa_logo))
        val bigImage: SurfaceImage =
            MovableSurfaceImage(Sector(36.0, -120.0, 5.0, 6.0), ImageSource.fromResource(R.drawable.nasa_logo))
        smallImage.putUserProperty(Dragger::class.java, SimpleDragger())
        bigImage.putUserProperty(Dragger::class.java, SimpleDragger())
        layer.addRenderable(smallImage)
        layer.addRenderable(bigImage)
        // Create a few placemarks with highlight attributes and add them to the layer
        layer.addRenderable(
            PlacemarksDraggerActivity.createAirportPlacemark(
                Position.fromDegrees(
                    34.2000,
                    -119.2070,
                    0.0
                ), "Oxnard Airport"
            )
        )
        layer.addRenderable(
            PlacemarksDraggerActivity.createAirportPlacemark(
                Position.fromDegrees(
                    34.2138,
                    -119.0944,
                    0.0
                ), "Camarillo Airport"
            )
        )
        layer.addRenderable(
            PlacemarksDraggerActivity.createAirportPlacemark(
                Position.fromDegrees(
                    34.1193,
                    -119.1196,
                    0.0
                ), "Pt Mugu Naval Air Station"
            )
        )
        layer.addRenderable(
            PlacemarksDraggerActivity.createAircraftPlacemark(
                Position.fromDegrees(
                    34.200,
                    -119.207,
                    1000.0
                )
            )
        )
        layer.addRenderable(
            PlacemarksDraggerActivity.createAircraftPlacemark(
                Position.fromDegrees(
                    34.210,
                    -119.150,
                    2000.0
                )
            )
        )
        layer.addRenderable(
            PlacemarksDraggerActivity.createAircraftPlacemark(
                Position.fromDegrees(
                    34.150,
                    -119.150,
                    3000.0
                )
            )
        )
        // Position the viewer to look near the airports
        val lookAt =
            LookAt().set(
                34.15,
                -119.15,
                0.0,
                WorldWind.ABSOLUTE,
                2e4 /*range*/,
                0.0 /*heading*/,
                45.0 /*tilt*/,
                0.0 /*roll*/
            )
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
            placemark.putUserProperty(Dragger::class.java, SimpleDragger())
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