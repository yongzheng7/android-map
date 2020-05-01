package com.atom.wyz.worldwind.app

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.atom.wyz.worldwind.BasicWorldWindowController
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.geom.Offset
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.Highlightable
import com.atom.wyz.worldwind.shape.PlacemarkAttributes

class PlacemarksPickingActivity : BasicWorldWindActivity() {

    companion object {
        private const val NORMAL_IMAGE_SCALE = 3.0
        private const val HIGHLIGHTED_IMAGE_SCALE = 4.0


        private fun createAircraftPlacemark(position: Position): Placemark {
            return Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.air_fixwing)).apply {
                this.attributes.imageOffset = Offset.bottomCenter()
                this.attributes.imageScale = NORMAL_IMAGE_SCALE
                this.attributes.drawLeader = true
                this.highlightAttributes = PlacemarkAttributes(this.attributes).apply {
                    this.imageScale = HIGHLIGHTED_IMAGE_SCALE
                }
            }
        }

        private fun createAirportPlacemark(position: Position, airportName: String): Placemark {
            return Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.airport_terminal))
                .apply {
                    this.attributes.imageOffset = Offset.bottomCenter()
                    this.attributes.imageScale = NORMAL_IMAGE_SCALE
                    this.highlightAttributes = PlacemarkAttributes(this.attributes).apply {
                        this.imageScale = HIGHLIGHTED_IMAGE_SCALE
                    }
                    this.displayName = airportName
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wwd: WorldWindow = getWorldWindow()

        wwd.worldWindowController = PickNavigateController()
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
            createAirportPlacemark(
                Position.fromDegrees(34.1193, -119.1196, 0.0),
                "Pt Mugu Naval Air Station"
            )
        )
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.15, -119.15, 2000.0)))
        // And finally, for this demo, position the viewer to look at the aircraft
        val lookAt: LookAt = LookAt().set(
            34.15, -119.15, 0.0, WorldWind.ABSOLUTE,
            2e4 /*range*/, 0.0 /*heading*/, 45.0 /*tilt*/, 0.0 /*roll*/
        )
        getWorldWindow().navigator.setAsLookAt(getWorldWindow().globe, lookAt)

    }

    inner class PickNavigateController() : BasicWorldWindowController() {

        protected var pickedObject : Any? = null

        protected var selectedObject: Any? = null


        protected var pickGestureDetector =
            GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent?): Boolean {
                    e?.let { pick(it) }
                    return false
                }

                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    toggleSelection() // Highlight the picked object
                    return false // By not consuming this event, we allow the ACTION_UP event to pass on the navigation gestures
                }

            })

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val consumed = pickGestureDetector.onTouchEvent(event)
            return if (!consumed) {
                super.onTouchEvent(event)
            } else consumed
        }

        fun pick(event: MotionEvent) {
            // Forget our last picked object
            pickedObject = null
            val pickList = getWorldWindow().pick(event.x, event.y)
            val pickedObject = pickList.topPickedObject()
            if (pickedObject != null) {
                this.pickedObject = pickedObject.userObject
            }
        }

        fun toggleSelection() {
            if (pickedObject is Highlightable) {
                val isNewSelection = pickedObject !== selectedObject
                if (isNewSelection && selectedObject is Highlightable) {
                    (selectedObject as Highlightable?)?.setHighlighted(false)
                }
                (pickedObject as Highlightable).setHighlighted(isNewSelection)
                getWorldWindow().requestRedraw()
                selectedObject = if (isNewSelection) pickedObject else null
            }
        }
    }
}