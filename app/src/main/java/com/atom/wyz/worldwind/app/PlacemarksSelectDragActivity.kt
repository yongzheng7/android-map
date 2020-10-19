package com.atom.wyz.worldwind.app

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.controller.BasicWorldWindowController
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.geom.observer.LookAt
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.layer.render.ImageSource
import com.atom.wyz.worldwind.layer.render.Placemark
import com.atom.wyz.worldwind.layer.render.Renderable
import com.atom.wyz.worldwind.layer.render.attribute.PlacemarkAttributes
import com.atom.wyz.worldwind.layer.render.shape.Highlightable
import java.util.*

/**
 * 待定带学习
 */
class PlacemarksSelectDragActivity : BasicWorldWindActivity() {

    companion object {
        const val EDITABLE = "editable"
        const val MOVABLE = "movable"
        const val SELECTABLE = "selectable"
        const val AIRCRAFT_TYPE = "aircraft_type"
        const val AUTOMOTIVE_TYPE = "auotomotive_type"
         const val NORMAL_IMAGE_SCALE = 3.0
         const val HIGHLIGHTED_IMAGE_SCALE = 4.0

        val aircraftTypes = arrayOf(
            "Small Plane",
            "Twin Engine",
            "Passenger Jet", "Bomber",
            "Fighter Jet"
        )
        val automotiveTypes = arrayOf(
            "Car",
            "SUV",
            "4x4",
            "Truck",
            "Jeep",
            "Tank"
        )
        val aircraftIcons = intArrayOf(
            R.drawable.aircraft_small,
            R.drawable.aircraft_twin,
            R.drawable.aircraft_jet,
            R.drawable.aircraft_fighter,
            R.drawable.aircraft_bomber,
            R.drawable.aircraft_rotor
            )

        private val automotiveIcons = intArrayOf(
            R.drawable.vehicle_car,
            R.drawable.vehicle_suv,
            R.drawable.vehicle_4x4,
            R.drawable.vehicle_truck,
            R.drawable.vehicle_jeep,
            R.drawable.vehicle_tank
        )

        protected fun createAirportPlacemark(position: Position, airportName: String): Placemark {
            val placemark: Placemark =
                Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.airport_terminal))
            placemark.attributes.apply {
                this.imageOffset = (Offset.bottomCenter())
                this.imageScale = NORMAL_IMAGE_SCALE
            }
            placemark.displayName = airportName
            return placemark
        }

        protected fun createAircraftPlacemark(
            position: Position,
            aircraftName: String,
            aircraftType: String
        ): Placemark {
            require(aircraftIconMap.containsKey(aircraftType)) { "$aircraftType is not valid." }
            val placemark: Placemark = Placemark.createSimpleImage(
                position,
                ImageSource.fromResource(aircraftIconMap.get(aircraftType)!!)
            )
            placemark.attributes.apply {
                this.imageOffset = Offset.bottomCenter()
                this.imageScale = NORMAL_IMAGE_SCALE
                this.drawLeader = true
                this.leaderAttributes?.outlineWidth = 4f
            }
            placemark.highlightAttributes = (
                    PlacemarkAttributes.defaults(
                        placemark.attributes
                    )
                        .apply {
                        this.imageScale = HIGHLIGHTED_IMAGE_SCALE
                        this.imageColor =
                            SimpleColor(Color.YELLOW)
                    }
                    )
            placemark.displayName = (aircraftName)
            placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType)
            placemark.putUserProperty(EDITABLE, null)
            placemark.putUserProperty(MOVABLE, null)
            placemark.putUserProperty(SELECTABLE, null)
            return placemark
        }

        protected fun createAutomobilePlacemark(
            position: Position,
            name: String,
            automotiveType: String
        ): Placemark {
            require(automotiveIconMap.containsKey(automotiveType)) { "$automotiveType is not valid." }
            val placemark: Placemark = Placemark.createSimpleImage(
                position,
                ImageSource.fromResource(automotiveIconMap[automotiveType]!!)
            )
            placemark.attributes.apply {
                this.imageOffset = (Offset.bottomCenter())
                this.imageScale = NORMAL_IMAGE_SCALE
            }
            placemark.highlightAttributes = PlacemarkAttributes.defaults(
                placemark.attributes
            ).apply {
                this.imageScale = HIGHLIGHTED_IMAGE_SCALE
                this.imageColor =
                    SimpleColor(Color.YELLOW)

            }
            placemark.displayName = (name)
            placemark.altitudeMode = (WorldWind.CLAMP_TO_GROUND)
            placemark.putUserProperty(AUTOMOTIVE_TYPE, automotiveType)
            placemark.putUserProperty(SELECTABLE, null)
            placemark.putUserProperty(EDITABLE, null)
            placemark.putUserProperty(MOVABLE, null)
            return placemark
        }

        val aircraftIconMap = HashMap<String, Int>()
        val automotiveIconMap = HashMap<String, Int>()

    }

    private var controller: SelectDragNavigateController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in aircraftTypes.indices) {
            aircraftIconMap[aircraftTypes[i]] = aircraftIcons[i]
        }
        for (i in automotiveTypes.indices) {
            automotiveIconMap[automotiveTypes[i]] = automotiveIcons[i]
        }

        val wwd: WorldWindow = getWorldWindow()

        val layer = RenderableLayer("Placemarks")
        wwd.layers.addLayer(layer)
        // Create some placemarks and add them to the layer
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
            PlacemarksSelectDragActivity.createAutomobilePlacemark(
                Position.fromDegrees(
                    34.210,
                    -119.120,
                    0.0
                ), "Civilian Vehicle", automotiveTypes[1]
            )
        ) // suv

        layer.addRenderable(
            PlacemarksSelectDragActivity.createAutomobilePlacemark(
                Position.fromDegrees(
                    34.210,
                    -119.160,
                    0.0
                ), "Military Vehicle", automotiveTypes[4]
            )
        ) // jeep

        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.200,
                    -119.207,
                    1000.0
                ), "Commercial Aircraft", aircraftTypes[1]
            )
        ) // twin

        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.210,
                    -119.150,
                    2000.0
                ), "Military Aircraft", aircraftTypes[3]
            )
        ) // fighter

        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(
                    34.150,
                    -119.150,
                    500.0
                ), "Private Aircraft", aircraftTypes[0]
            )
        ) // small plane


        this.controller = SelectDragNavigateController()
        wwd.worldWindowController = controller!!

        val lookAt: LookAt = LookAt()
            .set(
            34.150, -119.150, 0.0, WorldWind.ABSOLUTE,
            2e4, 0.0, 45.0, 0.0
        )
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)
    }

    inner class SelectDragNavigateController() : BasicWorldWindowController() {

        var pickedObject: Renderable? = null

        var selectedObject: Renderable? = null

        var isDragging = false

        var isDraggingArmed = false

        val ray: Line = Line()

        val pickPoint: Vec3 = Vec3()

        val dragRefPt = PointF()


        protected var selectDragDetector = GestureDetector(applicationContext,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent?): Boolean {
                    event?.let {
                        pick(it)
                    }  // Pick the object(s) at the tap location
                    return false // By not consuming this event, we allow it to pass on to the navigation gesture handlers
                }

                override fun onSingleTapUp(event: MotionEvent?): Boolean { // This single-tap handler has a faster response time than onSingleTapConfirmed.
                    toggleSelection()
                    return false
                }
                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    // toggleSelection()
                    return super.onSingleTapConfirmed(event)
                }

                override fun onScroll(
                    downEvent: MotionEvent?,
                    moveEvent: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    return if (isDraggingArmed) {
                        drag(downEvent!!, moveEvent!!, distanceX, distanceY) // Move the selected object
                    } else false
                }

                override fun onDoubleTap(event: MotionEvent?): Boolean { // Note that double-tapping should not toggle a "selected" object's selected state
                    if (pickedObject !== selectedObject) {
                        toggleSelection()
                    } else {
                        edit() // Open the placemark editor
                        return true
                    }
                    return false
                }

                override fun onLongPress(e: MotionEvent?) {
                    e?.let { pick(it) }
                    contextMenu()
                }
            })

        override fun onTouchEvent(event: MotionEvent): Boolean {
            // Allow our select and drag handlers to process the event first. They'll set the state flags which will
            // either preempt or allow the event to be subsequently processed by the globe's navigation event handlers.
            var consumed: Boolean = this.selectDragDetector.onTouchEvent(event)

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

        fun pick(event: MotionEvent) {
            pickedObject = null

            val pickList = getWorldWindow().pick(event.x, event.y)

            // Examine the picked objects for Renderables
            val topPickedObject = pickList.topPickedObject()
            if (topPickedObject != null) {
                if (topPickedObject.userObject is Renderable) {
                    pickedObject = topPickedObject.userObject as Renderable
                }
            }

            isDraggingArmed = (pickedObject != null
                    && selectedObject === pickedObject
                    && selectedObject!!.hasUserProperty(MOVABLE))
        }

        fun toggleSelection() {
            // Test if last picked object is "selectable".  If not, retain the
            // currently selected object. To discard the current selection,
            // the user must pick another selectable object or the current object.
            if (pickedObject != null) {
                if (pickedObject!!.hasUserProperty(SELECTABLE)) {
                    val isNewSelection = pickedObject !== selectedObject
                    // Display the highlight or normal attributes to indicate the
                    // selected or unselected state respectively.
                    if (pickedObject is Highlightable) { // Only one object can be selected at time, deselect any previously selected object
                        if (isNewSelection && selectedObject is Highlightable) {
                            (selectedObject as Highlightable?)!!.highlighted = (false)
                        }
                        (pickedObject as Highlightable).highlighted = (isNewSelection)
                        getWorldWindow().requestRedraw()
                    }
                    // Track the selected object
                    selectedObject = if (isNewSelection) pickedObject else null
                } else {
                    Toast.makeText(applicationContext, "The picked object is not selectable.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        fun simpleDrag(event: MotionEvent) {
            if (isDraggingArmed && selectedObject != null) {
                if (selectedObject is Placemark) {
                    isDragging = true
                    val placemark = selectedObject as Placemark
                    val oldAltitude: Double = placemark.position?.altitude ?: 0.0
                    placemark.position?.let {
                        screenPointToGroundPosition(event.x, event.y, it)
                        it.altitude = oldAltitude
                        getWorldWindow().requestRedraw()
                    }
                }
            }
        }

        fun drag(downEvent: MotionEvent, moveEvent: MotionEvent, distanceX: Float, distanceY: Float) : Boolean {
            if (isDraggingArmed && selectedObject != null) {
                if (selectedObject is Placemark) { // Signal that dragging is in progress
                    isDragging = true
                    // First we compute the screen coordinates of the position's "ground" point.  We'll apply the
                    // screen X and Y drag distances to this point, from which we'll compute a new position,
                    // wherein we restore the original position's altitude.
                    val position: Position = (selectedObject as Placemark?)?.position ?: return false
                    val altitude = position.altitude
                    if (getWorldWindow().geographicToScreenPoint(
                            position.latitude,
                            position.longitude,
                            0.0 /*altitude*/,
                            dragRefPt
                        )
                    ) { // Update the placemark's ground position
                        if (screenPointToGroundPosition(
                                dragRefPt.x - distanceX,
                                dragRefPt.y - distanceY,
                                position
                            )
                        ) { // Restore the placemark's original altitude
                            position.altitude = altitude
                            // Reflect the change in position on the globe.
                            getWorldWindow().requestRedraw()
                            return true
                        }
                    }
                    // Probably clipped by near/far clipping plane or off the globe. The position was not updated. Stop the drag.
                    isDraggingArmed = false
                    return true // We consumed this event, even if dragging has been stopped.
                }
            }
            return false
        }
        fun cancelDragging() {
            isDragging = false
            isDraggingArmed = false
        }
        /**
         * Moves the selected object to the event's screen position.
         */
        fun bestDrag(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ) {
            if (isDraggingArmed && selectedObject != null) {
                if (selectedObject is Placemark) {
                    isDragging = true
                    val position: Position = (selectedObject as Placemark?)?.position ?: return
                    val altitude = position.altitude
                    if (!getWorldWindow().geographicToScreenPoint(
                            position.latitude,
                            position.longitude,
                            0.0 /*altitude*/,
                            dragRefPt
                        )
                    ) { // Probably clipped by near/far clipping plane.
                        isDragging = false
                        return
                    }
                    // Update the placemark's ground position...
                    if (!screenPointToGroundPosition(
                            dragRefPt.x - distanceX,
                            dragRefPt.y - distanceY,
                            position
                        )
                    ) { // Probably off the globe, so cancel the drag.
                        isDragging = false
                        return
                    }
                    // ... and restore the altitude
                    position.altitude = altitude
                    getWorldWindow().requestRedraw()
                }
            }
        }

        fun edit() {
            if (selectedObject is Placemark && selectedObject!!.hasUserProperty(EDITABLE)
            ) {
                val placemark = selectedObject as Placemark?
                // Pass the current aircraft type in a Bundle
                val args = Bundle()
                args.putString("title", "Select the " + placemark?.displayName + "'s type")
                if (placemark!!.hasUserProperty(AIRCRAFT_TYPE)) {
                    args.putString("vehicleKey", AIRCRAFT_TYPE)
                    args.putString(
                        "vehicleValue",
                        placemark.getUserProperty(AIRCRAFT_TYPE) as String?
                    )
                } else if (placemark.hasUserProperty(AUTOMOTIVE_TYPE)) {
                    args.putString("vehicleKey", AUTOMOTIVE_TYPE)
                    args.putString(
                        "vehicleValue",
                        placemark.getUserProperty(AUTOMOTIVE_TYPE) as String?
                    )
                }
                // The VehicleTypeDialog calls onFinished
                val dialog: VehicleTypeDialog = VehicleTypeDialog()
                dialog.setArguments(args)
                dialog.show(supportFragmentManager, "aircraft_type")
            } else {
                Toast.makeText(
                    applicationContext,
                    (if (selectedObject == null) "Object " else selectedObject?.displayName) + " is not editable.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        fun contextMenu() {
            Toast.makeText(
                applicationContext,
                (if (pickedObject == null) "Nothing" else pickedObject?.displayName) + " picked and "
                        + (if (selectedObject == null) "nothing" else selectedObject?.displayName) + " selected.",
                Toast.LENGTH_LONG
            ).show()
        }


        fun screenPointToGroundPosition(screenX: Float, screenY: Float, result: Position): Boolean {
            if (wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
                val globe = wwd.globe
                if (globe.intersect(ray, pickPoint)) {
                    globe.cartesianToGeographic(pickPoint.x, pickPoint.y, pickPoint.z, result)
                    return true
                }
            }
            return false
        }

    }

    class VehicleTypeDialog : DialogFragment() {
        interface PlacemarkAircraftTypeListener {
            fun onFinishedAircraftEditing(aircraftType: String?)
        }

         var selectedItem = -1

        var vehicleKey: String? = null

        var vehicleTypes: Array<String>? = null

        var vehicleIcons: HashMap<String, Int>? = null

        override fun onCreateDialog(argss: Bundle?): Dialog {
            val args = arguments
            val title = args?.getString("title", "") ?: ""
            vehicleKey = args?.getString("vehicleKey", "")
            if (vehicleKey == AIRCRAFT_TYPE) {
                vehicleTypes = aircraftTypes
                vehicleIcons = aircraftIconMap
            } else if (vehicleKey == AUTOMOTIVE_TYPE) {
                vehicleTypes = automotiveTypes
                vehicleIcons = automotiveIconMap
            }
            val type: String = args?.getString("vehicleValue", "") ?: ""
            for (i in vehicleTypes!!.indices) {
                if (type == vehicleTypes!![i]) {
                    this.selectedItem = i
                    break
                }
            }
            return AlertDialog.Builder(activity)
                .setTitle(title)
                .setSingleChoiceItems(
                    vehicleTypes, this.selectedItem,
                    DialogInterface.OnClickListener { dialog, which ->
                        selectedItem = which
                    }) // The OK button will update the selected placemark's aircraft type
                .setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which -> onFinished(vehicleTypes!![selectedItem]) }) // A null handler will close the dialog
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        fun onFinished(vehicleType: String) {
            val activity = activity as PlacemarksSelectDragActivity?
            if (activity!!.controller!!.selectedObject is Placemark) {
                val placemark = activity.controller!!.selectedObject as Placemark?
                val currentType = placemark!!.getUserProperty(vehicleKey!!) as String?
                if (currentType == vehicleType) {
                    return
                }
                // Update the placemark's icon attributes and vehicle type property.
                val imageSource = ImageSource.fromResource(vehicleIcons!![vehicleType]!!)
                placemark.putUserProperty(vehicleKey!!, vehicleType)
                placemark.attributes.imageSource = (imageSource)
                placemark.highlightAttributes?.imageSource = (imageSource)
                // Show the change
                activity.getWorldWindow().requestRedraw()
            }
        }
    }
}