package com.atom.wyz.worldwind.app

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.atom.wyz.worldwind.BasicWorldWindowController
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.layer.ShowTessellationLayer
import com.atom.wyz.worldwind.ogc.Wcs100ElevationCoverage
import com.atom.wyz.worldwind.pick.PickedObjectList
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.OmnidirectionalSightline
import com.atom.wyz.worldwind.shape.ShapeAttributes

class OmnidirectionalSensor2Activity : BasicWorldWindActivity()  {
    /**
     * The sensor which evaluates line of sight
     */
    lateinit var sensor: OmnidirectionalSightline

    /**
     * A Placemark representing the position of the sensor
     */
    lateinit var sensorPlacemark: Placemark

    /**
     * A custom WorldWindowController object that handles the select, drag and navigation gestures.
     */
    lateinit var controller: SimpleSelectDragNavigateController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wwd.layers.addLayer(ShowTessellationLayer())
        // Specify the bounding sector - provided by the WCS
        val coverageSector = Sector.fromDegrees(-83.0, -180.0, 173.0, 360.0)
        coverageSector.setFullSphere()
        // Specify the version 1.0.0 WCS address
        val serviceAddress = "https://worldwind26.arc.nasa.gov/wcs"
        // Specify the coverage name
        val coverage = "aster_v2"
        // Create an elevation coverage from a version 1.0.0 WCS
        val aster = Wcs100ElevationCoverage(
            coverageSector,
            12,
            serviceAddress,
            coverage
        )
        // Add the coverage to the Globes elevation model
        val addCoverage = wwd.globe.elevationModel.addCoverage(aster)
        Log.e("WcsElevationFragment","addCoverage > $addCoverage")

        // Initialize attributes for the OmnidirectionalSensor
        val viewableRegions = ShapeAttributes()
        viewableRegions.interiorColor= (Color(0f, 1f, 0f, 1f))
        val blockedRegions = ShapeAttributes()
        blockedRegions.interiorColor = (Color(0.1f, 0.1f, 0.1f, 1f))

        // Initialize the OmnidirectionalSensor and Corresponding Placemark
        val pos = Position(46.202, -122.190, 500.0)
        sensor = OmnidirectionalSightline(pos, 10000f)
        sensor.attributes = (viewableRegions)
        sensor.occludeAttributes = (blockedRegions)
        sensor.altitudeMode = (WorldWind.RELATIVE_TO_GROUND)
        sensorPlacemark = Placemark(pos)
        sensorPlacemark.altitudeMode = (WorldWind.RELATIVE_TO_GROUND)
        sensorPlacemark.attributes
            .imageSource = (ImageSource.fromResource(R.drawable.aircraft_fixwing))
        sensorPlacemark.attributes.imageScale = (2.0)
        sensorPlacemark.attributes.drawLeader = (true)

        // Establish a layer to hold the sensor and placemark
        val sensorLayer = RenderableLayer()
        sensorLayer.addRenderable(sensor)
        sensorLayer.addRenderable(sensorPlacemark)
        wwd.layers.addLayer(sensorLayer)

        // Override the WorldWindow's built-in navigation behavior with conditional dragging support.
        controller = SimpleSelectDragNavigateController()
        wwd.worldWindowController = (controller)
        // And finally, for this demo, position the viewer to look at the sensor position
        val lookAt: LookAt = LookAt().set(
            pos.latitude,
            pos.longitude,
            pos.altitude,
            WorldWind.ABSOLUTE,
            2e4 /*range*/,
            0.0 /*heading*/,
            45.0 /*tilt*/,
            0.0 /*roll*/
        )
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)
    }

   inner class SimpleSelectDragNavigateController : BasicWorldWindowController() {
        protected var isDragging = false
        protected var isDraggingArmed = false
        private val dragRefPt = PointF()

        /**
         * Pre-allocated to avoid memory allocations
         */
        private val ray: Line = Line()

        /**
         * Pre-allocated to avoid memory allocations
         */
        private val pickPoint: Vec3 = Vec3()

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the drag gestures.
         */
        protected var selectDragDetector =
            GestureDetector(getApplicationContext(), object : SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent): Boolean {
                    pick(event) // Pick the object(s) at the tap location
                    return false // By not consuming this event, we allow it to pass on to the navigation gesture handlers
                }

                override fun onScroll(
                    downEvent: MotionEvent,
                    moveEvent: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    return if (isDraggingArmed) {
                        drag(downEvent, moveEvent, distanceX, distanceY)
                    } else false
                }
            })

        /**
         * Delegates events to the select/drag handlers or the native World Wind navigation handlers.
         */
       override fun onTouchEvent(event: MotionEvent): Boolean {
            // Allow our select and drag handlers to process the event first. They'll set the state flags which will
            // either preempt or allow the event to be subsequently processed by the globe's navigation event handlers.
            var consumed = selectDragDetector.onTouchEvent(event)

            // Is a dragging operation started or in progress? Any ACTION_UP event cancels a drag operation.
            if (isDragging && event.action == MotionEvent.ACTION_UP) {
                isDragging = false
                isDraggingArmed = false
            }
            // Preempt the globe's pan navigation recognizer if we're dragging
            super.panRecognizer.enabled = (!isDragging)

            // Pass on the event on to the default globe navigation handlers
            if (!consumed) {
                consumed = super.onTouchEvent(event)
            }
            return consumed
        }

        /**
         * Moves the selected object to the event's screen position.
         *
         * @return true if the event was consumed
         */
        fun drag(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isDraggingArmed) {
                // Signal that dragging is in progress
                isDragging = true

                // First we compute the screen coordinates of the position's "ground" point.  We'll apply the
                // screen X and Y drag distances to this point, from which we'll compute a new position,
                // wherein we restore the original position's altitude.
                val position: Position = sensorPlacemark.getReferencePosition()!!
                val altitude = position.altitude
                if (getWorldWindow().geographicToScreenPoint(
                        position.latitude,
                        position.longitude,
                        0.0,
                        dragRefPt
                    )
                ) {
                    // Update the placemark's ground position
                    if (screenPointToGroundPosition(
                            dragRefPt.x - distanceX,
                            dragRefPt.y - distanceY,
                            position
                        )
                    ) {
                        // Restore the placemark's original altitude
                        position.altitude = altitude
                        // Move the sensor
                        sensor.position = (position)
                        // Reflect the change in position on the globe.
                        getWorldWindow().requestRedraw()
                        return true
                    }
                }
                // Probably clipped by near/far clipping plane or off the globe. The position was not updated. Stop the drag.
                isDraggingArmed = false
                return true // We consumed this event, even if dragging has been stopped.
            }
            return false
        }

        /**
         * Performs a pick at the tap location and conditionally arms the dragging flag, so that dragging can occur if
         * the next event is an onScroll event.
         */
        fun pick(event: MotionEvent) {

            // Perform the pick at the screen x, y
            val pickList: PickedObjectList = getWorldWindow().pick(event.x, event.y)

            // Examine the picked objects for Renderables
            val topPickedObject = pickList.topPickedObject()
            // There is only one placemark on the globe and
            isDraggingArmed = topPickedObject != null && topPickedObject.userObject is Placemark
        }

        /**
         * Converts a screen point to the geographic coordinates on the globe.
         *
         * @param screenX X coordinate
         * @param screenY Y coordinate
         * @param result  Pre-allocated Position receives the geographic coordinates
         *
         * @return true if the screen point could be converted; false if the screen point is not on the globe
         */
        fun screenPointToGroundPosition(
            screenX: Float,
            screenY: Float,
            result: Position
        ): Boolean {
            if (wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
                val globe: Globe = wwd.globe
                if (globe.intersect(ray, pickPoint)) {
                    globe.cartesianToGeographic(
                        pickPoint.x,
                        pickPoint.y,
                        pickPoint.z,
                        result
                    )
                    return true
                }
            }
            return false
        }
    }

}