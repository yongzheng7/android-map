package com.atom.wyz

import android.os.Bundle
import com.atom.map.WorldWind
import com.atom.map.controller.CustomWorldWindowCameraController
import com.atom.map.geom.observer.Camera
import com.atom.map.globe.Globe

class CameraControlActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWorldWindow().worldWindowController =
            CustomWorldWindowCameraController()
        // Create a camera position above KOXR airport, Oxnard, CA
        val camera: Camera =
            Camera()
        camera.set(34.2, -119.2, 10000.0, WorldWind.ABSOLUTE, 90.0, 70.0, 0.0) // No roll
        // Apply the new camera position
        val globe: Globe = getWorldWindow().globe
        getWorldWindow().navigator.setAsCamera(globe, camera)
    }
}