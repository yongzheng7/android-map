package com.atom.wyz.worldwind.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.core.util.Pools
import com.atom.wyz.worldwind.R
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.WorldWindow
import com.atom.wyz.worldwind.WorldWindowController
import com.atom.wyz.worldwind.geom.Camera
import com.atom.wyz.worldwind.geom.Location
import com.atom.wyz.worldwind.geom.Position
import com.atom.wyz.worldwind.layer.Layer
import com.atom.wyz.worldwind.layer.LayerList
import com.atom.wyz.worldwind.layer.RenderableLayer
import com.atom.wyz.worldwind.render.ImageSource
import com.atom.wyz.worldwind.render.Placemark
import com.atom.wyz.worldwind.shape.PlacemarkAttributes
import com.atom.wyz.worldwind.util.Logger
import com.atom.wyz.worldwind.util.WWMath
import com.atom.wyz.worldwind.util.WWUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class BasicPerformanceBenchmarkActivity : BasicWorldWindActivity() {
    class ClearFrameMetricsCommand(wwd: WorldWindow) : Runnable {
        protected var wwd: WorldWindow
        override fun run() {
            if (!isActivityThread()) {
                runOnActivityThread(this)
            } else {
                wwd.frameMetrics.reset()
            }
        }

        init {
            this.wwd = wwd
        }
    }

    class LogFrameMetricsCommand(wwd: WorldWindow) : Runnable {
        protected var wwd: WorldWindow
        override fun run() {
            if (!isActivityThread()) {
                runOnActivityThread(this)
            } else {
                Logger.log(Logger.INFO, wwd.frameMetrics.toString())
            }
        }

        init {
            this.wwd = wwd
        }
    }

    class SleepCommand(protected var durationMillis: Long) : Runnable {
        override fun run() {
            BasicPerformanceBenchmarkActivity.sleepQuietly(durationMillis)
        }

    }

    class SetCameraCommand private constructor() : Runnable {
        private var wwd: WorldWindow? = null
        private val camera: Camera? = Camera()
        private operator fun set(wwd: WorldWindow?, camera: Camera?): SetCameraCommand? {
            this.wwd = wwd
            this.camera?.set(camera)
            return this
        }

        private fun reset(): SetCameraCommand {
            wwd = null
            return this
        }

        override fun run() {
            wwd?.navigator?.setAsCamera(wwd?.globe, camera)
            wwd?.requestRender()
            pool.release(reset())
        }

        companion object {
            private val pool = Pools.SynchronizedPool<SetCameraCommand?>(10)
            fun obtain(wwd: WorldWindow?, camera: Camera?): SetCameraCommand? {
                var command = pool.acquire()
                if (command == null) {
                    command = SetCameraCommand()
                }
                return command.set(wwd, camera)
            }
        }
    }

    class AnimateCameraCommand(wwd: WorldWindow, end: Camera, steps: Int) : Runnable {
        protected var wwd: WorldWindow
        protected var beginCamera: Camera = Camera()
        protected var endCamera: Camera = Camera()
        protected var curCamera: Camera = Camera()
        protected var beginPos: Position = Position()
        protected var endPos: Position = Position()
        protected var curPos: Position = Position()
        protected var steps: Int
        override fun run() {
            wwd.navigator.getAsCamera(wwd.globe, beginCamera)
            beginPos.set(beginCamera.latitude, beginCamera.longitude, beginCamera.altitude)
            for (i in 0 until steps) {
                val amount = i.toDouble() / (steps - 1).toDouble()
                beginPos.interpolateAlongPath( WorldWind.GREAT_CIRCLE, amount,endPos, curPos)
                curCamera.latitude = curPos.latitude
                curCamera.longitude = curPos.longitude
                curCamera.altitude = curPos.altitude
                curCamera.heading =
                    WWMath.interpolateAngle360(amount, beginCamera.heading, endCamera.heading)
                curCamera.tilt = WWMath.interpolateAngle180(amount, beginCamera.tilt, endCamera.tilt)
                curCamera.roll = WWMath.interpolateAngle180(amount, beginCamera.roll, endCamera.roll)
                val setCommand: Runnable? = SetCameraCommand.obtain(wwd, curCamera)
                BasicPerformanceBenchmarkActivity.runOnActivityThread(setCommand)
                BasicPerformanceBenchmarkActivity.sleepQuietly(BasicPerformanceBenchmarkActivity.FRAME_INTERVAL.toLong())
            }
        }

        init {
            this.wwd = wwd
            endCamera.set(end)
            endPos.set(end.latitude, end.longitude, end.altitude)
            this.steps = steps
        }
    }

    class NoOpWorldWindowController : WorldWindowController {
        override var worldWindow: WorldWindow?
            get() = null
            set(wwd) {}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wwd.worldWindowController = (NoOpWorldWindowController())
        // Add a layer containing a large number of placemarks.
        val layers: LayerList = getWorldWindow().layers
        val index: Int = layers.indexOfLayerNamed("Atmosphere")
        layers.addLayer(index, createPlacemarksLayer())
        // Create location objects for the places used in this test.
        val arc = Location(37.415229, -122.06265)
        val gsfc = Location(38.996944, -76.848333)
        val esrin = Location(41.826947, 12.674122)
        // After a 1 second initial delay, clear the frame statistics associated with this test.
        val exec: Executor = BasicPerformanceBenchmarkActivity.commandExecutor
        exec.execute(SleepCommand(1000))
        exec.execute(ClearFrameMetricsCommand(wwd))
        // After a 1/2 second delay, fly to NASA Ames Research Center over 100 frames.
        var cam = Camera(arc.latitude, arc.longitude, 10e3, WorldWind.ABSOLUTE, 0.0, 0.0, 0.0)
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        // After a 1/2 second delay, rotate the camera to look at NASA Goddard Space Flight Center over 50 frames.
        var azimuth: Double = arc.greatCircleAzimuth(gsfc)
        cam = Camera(arc.latitude, arc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 70.0, 0.0)
        exec.execute(SleepCommand(500))
        exec.execute(AnimateCameraCommand(wwd, cam, 50))
        // After a 1/2 second delay, fly the camera to NASA Goddard Space Flight Center over 200 frames.
        var midLoc: Location = arc.interpolateAlongPath(WorldWind.GREAT_CIRCLE, 0.5,gsfc,  Location())
        azimuth = midLoc.greatCircleAzimuth(gsfc)
        exec.execute(SleepCommand(500))
        cam = Camera(midLoc.latitude, midLoc.longitude, 1000e3, WorldWind.ABSOLUTE, azimuth, 0.0, 0.0)
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        cam = Camera(gsfc.latitude, gsfc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 70.0, 0.0)
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        // After a 1/2 second delay, rotate the camera to look at ESA Centre for Earth Observation over 50 frames.
        azimuth = gsfc.greatCircleAzimuth(esrin)
        cam = Camera(gsfc.latitude, gsfc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 90.0, 0.0)
        exec.execute(SleepCommand(500))
        exec.execute(AnimateCameraCommand(wwd, cam, 50))
        // After a 1/2 second delay, fly the camera to ESA Centre for Earth Observation over 200 frames.
        midLoc = gsfc.interpolateAlongPath(WorldWind.GREAT_CIRCLE, 0.5, esrin, Location())
        exec.execute(SleepCommand(500))
        cam = Camera(midLoc.latitude, midLoc.longitude, 1000e3, WorldWind.ABSOLUTE, azimuth, 60.0, 0.0)
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        cam = Camera(esrin.latitude, esrin.longitude, 100e3, WorldWind.ABSOLUTE, azimuth, 30.0, 0.0)
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        // After a 1/2 second delay, back the camera out to look at ESA Centre for Earth Observation over 100 frames.
        cam = Camera(esrin.latitude, esrin.longitude, 2000e3, WorldWind.ABSOLUTE, 0.0, 0.0, 0.0)
        exec.execute(SleepCommand(500))
        exec.execute(AnimateCameraCommand(wwd, cam, 100))
        // After a 1 second delay, log the frame statistics associated with this test.
        exec.execute(SleepCommand(1000))
        exec.execute(LogFrameMetricsCommand(wwd))
    }

    protected fun createPlacemarksLayer(): Layer {
        val layer = RenderableLayer("Placemarks")
        val attrs: Array<PlacemarkAttributes> = arrayOf<PlacemarkAttributes>(
            PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.air_fixwing)),
            PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.airplane)),
            PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.airport)),
            PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.airport_terminal))
        )
        var reader: BufferedReader? = null
        try {
            val `in` = this.resources.openRawResource(R.raw.arpt2)
            reader = BufferedReader(InputStreamReader(`in`))
            var line: String
            var attrIndex = 0
            while (reader.readLine().also { line = it } != null) {
                val locationString = line.split(",").toTypedArray()
                val pos: Position = Position.fromDegrees(locationString[0].toDouble(), locationString[1].toDouble(), 0.0)
                layer.addRenderable(Placemark(pos, attrs[attrIndex++ % attrs.size]))
            }
        } catch (e: Exception) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database")
        } finally {
            WWUtil.closeSilently(reader)
        }
        return layer
    }

    companion object {

        protected const val FRAME_INTERVAL = 67 // 67 millis; 15 frames per second


        protected var commandExecutor: Executor =
            Executors.newSingleThreadExecutor()

        protected var activityHandler = Handler(Looper.getMainLooper())


        fun runOnActivityThread(@NonNull command: Runnable?) {
            activityHandler.post(command)
        }

        fun isActivityThread(): Boolean {
            return Thread.currentThread() === Looper.getMainLooper().thread
        }

        fun sleepQuietly(durationMillis: Long) {
            try {
                Thread.sleep(durationMillis)
            } catch (ignored: InterruptedException) {
            }
        }

    }
}