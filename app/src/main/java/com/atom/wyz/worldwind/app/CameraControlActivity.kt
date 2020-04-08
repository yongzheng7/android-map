package com.atom.wyz.worldwind.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import com.atom.wyz.worldwind.CustomWorldWindowCameraController
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.globe.Globe
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CameraControlActivity : BasicWorldWindActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWorldWindow().worldWindowController = CustomWorldWindowCameraController()
        // Create a camera position above KOXR airport, Oxnard, CA
        val camera: Camera = Camera()
        camera.set(34.2, -119.2, 10000.0, WorldWind.ABSOLUTE, 90.0, 70.0, 0.0) // No roll
        // Apply the new camera position
        val globe: Globe = getWorldWindow().globe
        getWorldWindow().navigator.setAsCamera(globe, camera)
    }
}