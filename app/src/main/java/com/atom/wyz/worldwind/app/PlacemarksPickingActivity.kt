package com.atom.wyz.worldwind.app

import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
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

class PlacemarksPickingActivity : BasicWorldWindActivity() {

    companion object {

        private fun createAircraftPlacemark(position: Position): Placemark {
            return Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.air_fixwing)).apply {
                this.attributes.imageOffset = Offset.bottomCenter()
                this.attributes.imageScale = 2.0
                this.attributes.drawLeader = true
                this.highlightAttributes = PlacemarkAttributes(this.attributes).apply {
                    this.imageScale = 3.0
                }
            }
        }

        private fun createAirportPlacemark(position: Position, airportName: String): Placemark {
            return Placemark.createSimpleImage(position, ImageSource.fromResource(R.drawable.airport_terminal))
                .apply {
                    this.attributes.imageOffset = Offset.bottomCenter()
                    this.attributes.imageScale = 2.0
                    this.highlightAttributes = PlacemarkAttributes(this.attributes).apply {
                        this.imageScale = 3.0
                    }
                    this.displayName = airportName
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wwd: WorldWindow = getWorldWindow()
        val layer = RenderableLayer("Placemarks")
        wwd.layers.addLayer(layer)
        wwd.worldWindowController =
            (PickNavigateController(layer))
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

    inner class PickNavigateController(layer: RenderableLayer) : BasicWorldWindowController() {
        var PIXEL_TOLERANCE = 50f

        protected var pickedObject // last picked object from onDown events
                : Renderable? = null

        protected var selectedObject // last "selected" object from single tap
                : Renderable? = null


        val layer: RenderableLayer = layer

        protected var pickGestureDetector =
            GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent?): Boolean {
                    e?.let { pick(it) }
                    return false
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    toggleSelection() // Highlight the picked object
                    return true // By not consuming this event, we allow the ACTION_UP event to pass on the navigation gestures
                }

            })

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val consumed = pickGestureDetector.onTouchEvent(event)
            return if (!consumed) {
                super.onTouchEvent(event)
            } else consumed
        }

        fun pick(event: MotionEvent) {
            pickedObject = this.simulatedPicking(event.x, event.y)
        }

        fun simulatedPicking(pickX: Float, pickY: Float): Renderable? {
            val iterator: Iterator<Renderable> = layer.iterator()
            while (iterator.hasNext()) {
                val renderable = iterator.next()
                if (renderable is Placemark) { // Get the screen point for this placemark
                    val placemark = renderable
                    val position: Position = placemark.position ?: continue
                    val point = PointF()
                    if (wwd.geographicToScreenPoint(
                            position.latitude,
                            position.longitude,
                            position.altitude,
                            point
                        )
                    ) {
                        if (point.x <= pickX + PIXEL_TOLERANCE && point.x >= pickX - PIXEL_TOLERANCE && point.y <= pickY + PIXEL_TOLERANCE && point.y >= pickY - PIXEL_TOLERANCE
                        ) {
                            return placemark
                        }
                    }
                }
            }
            return null
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