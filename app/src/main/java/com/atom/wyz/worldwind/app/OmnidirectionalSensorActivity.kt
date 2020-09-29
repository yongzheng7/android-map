package com.atom.wyz.worldwind.app

import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.LookAt
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.geom.SimpleColor
import com.atom.wyz.worldwind.layer.CartesianLayer
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.layer.ShowTessellationLayer
import com.atom.wyz.worldwind.layer.render.ImageSource
import com.atom.wyz.worldwind.layer.render.OmnidirectionalSightline
import com.atom.wyz.worldwind.layer.render.Placemark
import com.atom.wyz.worldwind.layer.render.attribute.ShapeAttributes
import com.atom.wyz.worldwind.ogc.Wcs100ElevationCoverage
import com.atom.wyz.worldwind.util.Logger

class OmnidirectionalSensorActivity : BasicGlobeActivity() {

    override fun createWorldWindow(): WorldWindow {
        // Let the super class (BasicGlobeFragment) do the creation
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.addLayer(ShowTessellationLayer())
        wwd.layers.addLayer(CartesianLayer())
        Logger.log(Logger.ERROR , Logger.makeMessage("OmnidirectionalSensorActivity" , "createWorldWindow" ,"OmnidirectionalSensorActivity"))
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

        val position = Position(46.230, -122.190, 2500.0)
        val visibleAttributes =
            ShapeAttributes.defaults()
        visibleAttributes.interiorColor = (SimpleColor(
            0f,
            1f,
            0f,
            0.5f
        ))
        val occludedAttributes =
            ShapeAttributes.defaults()
        occludedAttributes.interiorColor = (SimpleColor(
            0.1f,
            0.1f,
            0.1f,
            0.8f
        ))
        // Create the sensor
        val sensor =
            OmnidirectionalSightline(
                position,
                10000f
            )
        // Add the attributes
        sensor.attributes = (visibleAttributes)
        sensor.occludeAttributes = (occludedAttributes)

        // Create a layer for the sensor
        val sensorLayer = RenderableLayer()
        sensorLayer.addRenderable(sensor)
        wwd.layers.addLayer(sensorLayer)
        // Create a Placemark to visualize the position of the sensor
        createPlacemark(position, sensorLayer)
        // Position the camera to look at the sensor coverage
        positionView(wwd)
        return wwd
    }

    protected fun createPlacemark(position: Position, layer: RenderableLayer) {
        val placemark = Placemark(position)
        placemark.attributes.imageSource = (ImageSource.fromResource(R.drawable.aircraft_fixwing))
        placemark.attributes.imageScale = (2.0)
        placemark.attributes.drawLeader = (true)
        layer.addRenderable(placemark)
    }

    protected fun positionView(wwd: WorldWindow) {
        val lookAt: LookAt = LookAt().set(
            46.230,
            -122.190,
            500.0,
            WorldWind.ABSOLUTE,
            1.5e4 /*range*/,
            45.0 /*heading*/,
            70.0 /*tilt*/,
            0.0 /*roll*/
        )
        wwd.navigator.setAsLookAt(this.wwd.globe, lookAt)
    }
}