package com.atom.wyz.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.NonNull
import androidx.core.util.Pools
import com.atom.map.WorldHelper
import com.atom.map.WorldWind
import com.atom.map.WorldWindow
import com.atom.map.controller.WorldWindowController
import com.atom.map.geom.Location
import com.atom.map.geom.Position
import com.atom.map.geom.observer.Camera
import com.atom.map.layer.Layer
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.render.ImageSource
import com.atom.map.layer.render.Placemark
import com.atom.map.layer.render.attribute.PlacemarkAttributes
import com.atom.map.util.*
import com.atom.wyz.BasicWorldWindActivity
import com.atom.wyz.worldwind.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BasicPerformanceBenchmarkActivity : BasicWorldWindActivity() {

    companion object {

        protected const val FRAME_INTERVAL = 67 // 67 millis; 15 frames per second

        protected lateinit var activityHandler : Handler

        protected var commandExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        fun getNewCommandExecutor(): ExecutorService {
            return commandExecutor
        }

        fun runOnActivityThread(@NonNull command: Runnable) {
            activityHandler.post(command)
        }

        fun isActivityThread(): Boolean {
            return Thread.currentThread() === Looper.getMainLooper().thread
        }
    }

    class FrameMetricsTask(private val wwd: WorldWindow, val isStart: Boolean = true) : Runnable {
        override fun run() {
            // 统计时长
            if (!isActivityThread()) {
                runOnActivityThread(this)
            } else {
                isStart.yes {
                    this.wwd.frameMetrics.reset()
                }.otherwise {
                    Log.e("BasicPerformance", wwd.frameMetrics.toString())
                }
            }
        }
    }

    class SleepTask(var durationMillis: Long) : Runnable {
        override fun run() {
            try {
                Thread.sleep(durationMillis)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    class SetCameraTask private constructor() : Runnable {

        companion object {
            private val pool = Pools.SynchronizedPool<SetCameraTask>(10)
            fun obtain(wwd: WorldWindow, camera: Camera): SetCameraTask {
                var command = pool.acquire()
                if (command == null) {
                    command = SetCameraTask()
                }
                return command.set(wwd, camera)
            }
        }

        private var wwd: WorldWindow? = null
        private val camera: Camera = Camera()

        private operator fun set(wwd: WorldWindow, camera: Camera): SetCameraTask {
            this.wwd = wwd
            this.camera.set(camera)
            return this
        }

        private fun reset(): SetCameraTask {
            wwd = null
            return this
        }

        override fun run() {
            wwd?.also {
                Log.e("BasicPerformance" , "SetCameraTask > $camera")
                it.navigator.setAsCamera(it.globe, camera)
                it.requestRedraw()
            }
            pool.release(reset())
        }

    }

    class AnimateCameraTask(wwd: WorldWindow, end: Camera, steps: Int) : Runnable {
        private var wwd: WorldWindow = wwd
        private var endCamera: Camera = Camera().set(end)
        private var steps = steps
        private var endPos: Position = Position().set(end.latitude, end.longitude, end.altitude)
        private var beginCamera: Camera = Camera()
        private var curCamera: Camera = Camera()
        private var beginPos: Position = Position()
        private var curPos: Position = Position()


        override fun run() {
            wwd.navigator.getAsCamera(wwd.globe, beginCamera)
            beginPos.set(beginCamera.latitude, beginCamera.longitude, beginCamera.altitude)
            for (i in 0 until steps) {
                val amount = i.toDouble() / (steps - 1).toDouble()
                beginPos.interpolateAlongPath(WorldWind.GREAT_CIRCLE, amount, endPos, curPos)
                curCamera.latitude = curPos.latitude
                curCamera.longitude = curPos.longitude
                curCamera.altitude = curPos.altitude
                curCamera.heading =
                    WWMath.interpolateAngle360(amount, beginCamera.heading, endCamera.heading)
                curCamera.tilt =
                    WWMath.interpolateAngle180(amount, beginCamera.tilt, endCamera.tilt)
                curCamera.roll =
                    WWMath.interpolateAngle180(amount, beginCamera.roll, endCamera.roll)

                runOnActivityThread(
                    SetCameraTask.obtain(
                        wwd,
                        curCamera
                    )
                )
                try {
                    Thread.sleep(FRAME_INTERVAL.toLong())
                } catch (ignored: InterruptedException) {
                }
            }
        }

    }

    class NoOpWorldWindowController : WorldWindowController {
        override var world: WorldHelper?
            get() = null
            set(wwd) {}

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityHandler = Handler(Looper.getMainLooper())
        getWorldWindow().worldWindowController = (NoOpWorldWindowController())
        // Add a layer containing a large number of placemarks.
        getWorldWindow().layers.addLayer(createPlacemarksLayer())
    }


    override fun onStart() {
        super.onStart()
        val arc = Location(37.415229, -122.06265)
        val gsfc = Location(38.996944, -76.848333)
        val esrin = Location(41.826947, 12.674122)
        val exec: Executor = getNewCommandExecutor()
        exec.execute(
            SleepTask(1000)
        )
        exec.execute(
            FrameMetricsTask(getWorldWindow())
        )
        // After a 1/2 second delay, fly to NASA Ames Research Center over 100 frames.
        var cam = Camera(
            arc.latitude,
            arc.longitude,
            10e3,
            WorldWind.ABSOLUTE,
            0.0,
            0.0,
            0.0
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        // After a 1/2 second delay, rotate the camera to look at NASA Goddard Space Flight Center over 50 frames.
        var azimuth= arc.greatCircleAzimuth(gsfc)
        cam = Camera(
            arc.latitude,
            arc.longitude,
            10e3,
            WorldWind.ABSOLUTE,
            azimuth,
            70.0,
            0.0
        )
        exec.execute(
            SleepTask(
                500
            )
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                50
            )
        )

        var midLoc: Location = arc.interpolateAlongPath(WorldWind.GREAT_CIRCLE, 0.5, gsfc, Location())
        azimuth = midLoc.greatCircleAzimuth(gsfc)
        exec.execute(
            SleepTask(
                500
            )
        )
        cam = Camera(
            midLoc.latitude,
            midLoc.longitude,
            1000e3,
            WorldWind.ABSOLUTE,
            azimuth,
            0.0,
            0.0
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        cam = Camera(
            gsfc.latitude,
            gsfc.longitude,
            10e3,
            WorldWind.ABSOLUTE,
            azimuth,
            70.0,
            0.0
        )

        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        // After a 1/2 second delay, rotate the camera to look at ESA Centre for Earth Observation over 50 frames.
        azimuth = gsfc.greatCircleAzimuth(esrin)
        cam = Camera(
            gsfc.latitude,
            gsfc.longitude,
            10e3,
            WorldWind.ABSOLUTE,
            azimuth,
            90.0,
            0.0
        )
        exec.execute(
            SleepTask(
                500
            )
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                50
            )
        )
        // After a 1/2 second delay, fly the camera to ESA Centre for Earth Observation over 200 frames.
        midLoc = gsfc.interpolateAlongPath(WorldWind.GREAT_CIRCLE, 0.5, esrin, Location())
        exec.execute(
            SleepTask(
                500
            )
        )
        cam = Camera(
            midLoc.latitude,
            midLoc.longitude,
            1000e3,
            WorldWind.ABSOLUTE,
            azimuth,
            60.0,
            0.0
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        cam = Camera(
            esrin.latitude,
            esrin.longitude,
            100e3,
            WorldWind.ABSOLUTE,
            azimuth,
            30.0,
            0.0
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        // After a 1/2 second delay, back the camera out to look at ESA Centre for Earth Observation over 100 frames.
        cam = Camera(
            esrin.latitude,
            esrin.longitude,
            2000e3,
            WorldWind.ABSOLUTE,
            0.0,
            0.0,
            0.0
        )
        exec.execute(
            SleepTask(
                500
            )
        )
        exec.execute(
            AnimateCameraTask(
                getWorldWindow(),
                cam,
                100
            )
        )
        // After a 1 second delay, log the frame statistics associated with this test.
        exec.execute(
            SleepTask(
                1000
            )
        )
        exec.execute(FrameMetricsTask(getWorldWindow(), false))
    }


    override fun onStop() {
        super.onStop()
        commandExecutor.shutdownNow()
    }

    protected fun createPlacemarksLayer(): Layer {
        val layer = RenderableLayer("Placemarks")
        val attrs: Array<PlacemarkAttributes> = arrayOf(
            PlacemarkAttributes.withImage(
                ImageSource.fromResource(R.drawable.air_fixwing)
            ),
            PlacemarkAttributes.withImage(
                ImageSource.fromResource(R.drawable.airplane)
            ),
            PlacemarkAttributes.withImage(
                ImageSource.fromResource(R.drawable.airport)
            ),
            PlacemarkAttributes.withImage(
                ImageSource.fromResource(R.drawable.airport_terminal)
            )
        )
        var reader: BufferedReader? = null
        try {
            val `in` = this.resources.openRawResource(R.raw.ntad_place)
            reader = BufferedReader(InputStreamReader(`in`))

            var line = reader.readLine()
            val headers = listOf(*line.split(",").toTypedArray())
            val LAT = headers.indexOf("LAT")
            val LON = headers.indexOf("LON")
            val NA3 = headers.indexOf("NA3")
            val USE = headers.indexOf("USE")
            // Read the remaining lines
            var attrIndex = 0
            while (reader.readLine().also { line = it } != null) {
                val fields = line.split(",").toTypedArray()
                if (fields[NA3].startsWith("US") && fields[USE] == "49") { // display USA Civilian/Public airports
                    val pos = Position.fromDegrees(fields[LAT].toDouble(), fields[LON].toDouble(), 0.0)
                    layer.addRenderable(
                        Placemark(
                            pos,
                            attrs[attrIndex++ % attrs.size]
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database")
        } finally {
            WWUtil.closeSilently(reader)
        }
        return layer
    }
}