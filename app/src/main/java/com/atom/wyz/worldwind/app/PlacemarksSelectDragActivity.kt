package com.atom.wyz.worldwind.app

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.atom.wyz.worldwind.BasicWorldWindowController
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.*
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.render.Renderable
import com.atom.wyz.worldwind.shape.Highlightable
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
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

        val aircraftTypes = arrayOf(
            "Small Plane",
            "Twin Engine",
            "Passenger Jet",
            "Fighter Jet"
        )
        val aircraftIcons = intArrayOf(
            R.drawable.aircraft_small,
            R.drawable.aircraft_twin,
            R.drawable.aircraft_jet,
            R.drawable.aircraft_fighter
        )

        protected fun createAirportPlacemark(position: Position, airportName: String): Placemark {
            val placemark: Placemark =
                Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.airport_terminal))
            placemark.attributes.apply {
                this.imageOffset = (Offset.bottomCenter())
                this.imageScale = 2.0
            }
            placemark.highlightAttributes = (PlacemarkAttributes(placemark.attributes).apply {
                this.imageScale = (3.0)
            })
            placemark.displayName = airportName
            placemark.putUserProperty(SELECTABLE, null)
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
                this.imageScale = 2.0
                this.drawLeader = true
                this.leaderAttributes?.outlineWidth = 4f
            }
            placemark.highlightAttributes = (
                    PlacemarkAttributes(placemark.attributes).apply {
                        this.imageScale = (4.0)
                        this.imageColor = Color.YELLOW
                    }
                    )
            placemark.displayName = (aircraftName)
            placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType)
            placemark.putUserProperty(EDITABLE, null)
            placemark.putUserProperty(MOVABLE, null)
            placemark.putUserProperty(SELECTABLE, null)
            return placemark
        }

        val aircraftIconMap = HashMap<String, Int>()
    }

    private var controller: SelectDragNavigateController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in aircraftTypes.indices) {
            aircraftIconMap[aircraftTypes[i]] = aircraftIcons[i]
        }
        val wwd: WorldWindow = getWorldWindow()

        val layer = RenderableLayer("Placemarks")
        wwd.layers.addLayer(layer)
        layer.addRenderable(
            createAirportPlacemark(
                Position.fromDegrees(34.200, -119.207, 0.0),
                "Oxnard Airport"
            )
        )
        layer.addRenderable(
            createAirportPlacemark(
                Position.fromDegrees(34.2138, -119.0944, 0.0),
                "Camarillo Airport"
            )
        )

        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(34.200, -119.207, 1000.0),
                "Commercial",
                aircraftTypes[1]
            )
        )
        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(34.210, -119.150, 2000.0),
                "Military",
                aircraftTypes[3]
            )
        )
        layer.addRenderable(
            createAircraftPlacemark(
                Position.fromDegrees(34.250, -119.207, 1000.0),
                "Private",
                aircraftTypes[0]
            )
        )
        this.controller = SelectDragNavigateController(layer)
        wwd.worldWindowController = controller!!

        val lookAt: LookAt = LookAt().set(
            34.210, -119.150, 0.0, WorldWind.ABSOLUTE,
            2e4, 0.0, 45.0, 0.0
        )
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)
    }

    inner class SelectDragNavigateController(var layer: RenderableLayer) : BasicWorldWindowController(){
        var PIXEL_TOLERANCE = 50f

        protected var pickedObject: Renderable? = null

        var selectedObject: Renderable? = null

        protected var isDragging = false

        protected var isDraggingArmed = false

        private val ray: Line = Line()

        private val pickPoint: Vec3 = Vec3()


        protected var gestureDetector = GestureDetector(applicationContext,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent?): Boolean {
                    event?.let {
                        pick(it)
                    }  // Pick the object(s) at the tap location
                    return false // By not consuming this event, we allow it to pass on to the navigation gesture handlers
                }

                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    toggleSelection() // Highlight the picked object
                    return true
                }

                override fun onScroll(
                    downEvent: MotionEvent?,
                    moveEvent: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    moveEvent?.let {
                        drag(it)
                    }
                    // Move the selected object
                    return isDragging // Consume this event if dragging is active
                }

                override fun onDoubleTap(event: MotionEvent?): Boolean { // Note that double-tapping should not toggle a "selected" object's selected state
                    if (pickedObject !== selectedObject) {
                        toggleSelection()
                    }
                    edit() // Open the placemark editor
                    return true
                }

                override fun onLongPress(e: MotionEvent?) {
                    e ?.let {  pick(it)}
                    contextMenu()
                }
            })

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val consumed = gestureDetector.onTouchEvent(event)

            if (isDraggingArmed && event.action == MotionEvent.ACTION_UP) {
                isDraggingArmed = false
                isDragging = false
            }
            if (!consumed && !isDragging) {
                return super.onTouchEvent(event)
            }
            return consumed
        }

        fun pick(event: MotionEvent) {
            pickedObject = this.simulatedPicking(event.x, event.y)
            isDraggingArmed = (pickedObject != null
                    && selectedObject === pickedObject
                    && selectedObject!!.hasUserProperty(MOVABLE))
        }

        fun toggleSelection() {
            val hasUserProperty = pickedObject?.hasUserProperty(SELECTABLE) ?: false
            if (hasUserProperty) {
                val isNewSelection = pickedObject != selectedObject
                if (pickedObject is Highlightable) {
                    if (isNewSelection && selectedObject is Highlightable) {
                        (selectedObject as Highlightable).setHighlighted(false)
                    }
                    (pickedObject as Highlightable).setHighlighted(isNewSelection)
                    getWorldWindow().requestRedraw()
                }
                selectedObject = if (isNewSelection) pickedObject else null
            }
        }

        fun drag(event: MotionEvent) {
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

        fun edit() {
            val hasUserProperty = selectedObject?.hasUserProperty(EDITABLE) ?: false
            if (hasUserProperty) {
                val placemark = selectedObject as Placemark
                // Pass the current aircraft type in a Bundle
                val args = Bundle()
                args.putString("title", "Select the " + placemark.displayName.toString() + " Aircraft Type")
                args.putString("type", placemark.getUserProperty(AIRCRAFT_TYPE) as String?)
                val dialog = AircraftTypeDialog()
                dialog.arguments = args
                dialog.show(supportFragmentManager, "aircraft_type")
            }
        }

        fun contextMenu() {
            Toast.makeText(
                applicationContext,
                (if (pickedObject == null) "Nothing" else pickedObject?.displayName) + " picked"
                        + (if (pickedObject === selectedObject) " and " else " but not ") + "selected.",
                Toast.LENGTH_LONG
            ).show()
        }

        fun simulatedPicking(pickX: Float, pickY: Float): Renderable? {
            val iterator = layer.iterator()
            while (iterator.hasNext()) {
                val renderable = iterator.next()
                if (renderable is Placemark) { // Get the screen point for this placemark
                    val placemark = renderable
                    val position: Position = placemark.position ?: return null
                    val point = PointF()
                    if (wwd.geographicToScreenPoint(
                            position.latitude,
                            position.longitude,
                            position.altitude,
                            point
                        )
                    ) { // Test if the placemark's screen point is within the tolerance for picking
                        if (point.x <= pickX + PIXEL_TOLERANCE && point.x >= pickX - PIXEL_TOLERANCE && point.y <= pickY + PIXEL_TOLERANCE && point.y >= pickY - PIXEL_TOLERANCE
                        ) {
                            return placemark
                        }
                    }
                }
            }
            return null
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

    class AircraftTypeDialog : DialogFragment() {
        interface PlacemarkAircraftTypeListener {
            fun onFinishedAircraftEditing(aircraftType: String?)
        }

        private var selection = -1
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = getArguments()

            var title = ""
            var type = ""
            args?.let {
                title = it.getString("title", "")
                type = it.getString("type", "")
            }
            for (i in aircraftTypes.indices) {
                if (type === aircraftTypes[i]) {
                    selection = i
                    break
                }
            }
            // Create single selection list of aircraft types
            return AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setSingleChoiceItems(
                    aircraftTypes, selection,
                    { dialog, which -> selection = which })
                .setPositiveButton(android.R.string.yes, { dialog, which ->
                    onFinished(aircraftTypes[selection])
                }) // A null handler will close the dialog
                .setNegativeButton(android.R.string.no, null)
                .create()
        }

        fun onFinished(aircraftType: String) {
            val activity = activity as PlacemarksSelectDragActivity?
            if (activity!!.controller!!.selectedObject is Placemark) {
                val placemark = activity.controller!!.selectedObject as Placemark?
                val currentType =
                    placemark!!.getUserProperty(AIRCRAFT_TYPE) as String?
                if (currentType == aircraftType) {
                    return
                }
                // Update the placemark's icon and aircraft type property
                val imageSource =
                    ImageSource.fromResource(aircraftIconMap[aircraftType]!!)
                placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType)
                placemark.attributes.imageSource = (imageSource)
                placemark.highlightAttributes?.imageSource = (imageSource)
                // Show the change
                activity.getWorldWindow().requestRedraw()
            }
        }
    }
}