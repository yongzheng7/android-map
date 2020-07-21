package com.atom.wyz.worldwind.app

import android.util.Log
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.globe.Globe
import com.atom.wyz.worldwind.layer.CartesianLayer
import com.atom.wyz.worldwind.layer.ShowTessellationLayer
import com.atom.wyz.worldwind.ogc.Wcs100ElevationCoverage

class WcsElevationFragment : BasicGlobeActivity() {
    override fun createWorldWindow(): WorldWindow {
        val wwd: WorldWindow = super.createWorldWindow()
        wwd.layers.addLayer(CartesianLayer())
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
        // Position the camera to look at the Sangre de Cristo Mountains
        this.positionView(wwd)
        return wwd
    }

    protected fun positionView(wwd: WorldWindow) {
        val mtRainier = Position(37.577227, -105.485845, 4374.0)
        val eye = Position(37.5, -105.4, 5000.0)

        // Compute heading and distance from peak to eye
        val globe: Globe = wwd.globe
        val heading: Double = eye.greatCircleAzimuth(mtRainier)
        val distanceRadians: Double = mtRainier.greatCircleDistance(eye)
        val distance: Double =
            distanceRadians * globe.getRadiusAt(mtRainier.latitude, mtRainier.longitude)

        // Compute camera settings
        val altitude: Double = eye.altitude - mtRainier.altitude
        val range = Math.sqrt(altitude * altitude + distance * distance)
        val tilt =
            Math.toDegrees(Math.atan(distance / eye.altitude))

        // Apply the new view
        val camera = Camera()
        camera.set(
            eye.latitude,
            eye.longitude,
            eye.altitude,
            WorldWind.ABSOLUTE,
            heading,
            tilt,
            0.0 /*roll*/
        )
        wwd.navigator.setAsCamera(globe, camera)
    }

}