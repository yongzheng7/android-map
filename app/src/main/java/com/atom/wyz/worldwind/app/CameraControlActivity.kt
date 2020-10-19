package com.atom.wyz.worldwind.app

import android.os.Bundle
import com.atom.wyz.worldwind.controller.CustomWorldWindowCameraController
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.observer.Camera
import com.atom.wyz.worldwind.globe.Globe

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