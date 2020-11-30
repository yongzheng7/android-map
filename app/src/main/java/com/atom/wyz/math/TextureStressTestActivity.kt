package com.atom.wyz.math

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.atom.map.geom.Sector
import com.atom.map.layer.CartesianLayer
import com.atom.map.layer.RenderableLayer
import com.atom.map.layer.ShowTessellationLayer
import com.atom.map.renderable.ImageSource
import com.atom.map.renderable.SurfaceImage
import com.atom.map.util.Logger
import com.atom.wyz.base.BasicWorldWindActivity
import java.lang.String
import java.util.*

class TextureStressTestActivity : BasicWorldWindActivity(), Handler.Callback {

    protected var layer: RenderableLayer = RenderableLayer()

    protected var firstSector: Sector = Sector()

    protected var sector: Sector = Sector()

    protected var handler = Handler(this)

    protected var bitmap: Bitmap? = null

    companion object {

        protected val ADD_IMAGE = 1

        protected val ADD_IMAGE_INTERVAL = 1000

    }

    override fun handleMessage(msg: Message): Boolean {
        return if (msg.what == ADD_IMAGE) {
            addImage()
            msg.target.sendEmptyMessageDelayed(
                ADD_IMAGE,
                ADD_IMAGE_INTERVAL.toLong()
            )
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup the World Window to display the tessellation layer and a layer of surface images. We use a minimal
        // layer configuration in order to gather precise metrics on memory usage.
        getWorldWindow().layers.clearLayers()
        getWorldWindow().layers.addLayer(ShowTessellationLayer())
        getWorldWindow().layers.addLayer(CartesianLayer())
        getWorldWindow().layers.addLayer(layer)

        // Position the viewer so that the surface images will be visible as they're added.
        firstSector[35.0, 10.0, 0.5] = 0.5
        sector.set(firstSector)
        getWorldWindow().navigator.latitude = (37.5)
        getWorldWindow().navigator.longitude = (15.0)
        getWorldWindow().navigator.altitude = (1.0e6)

        // Allocate a 32-bit 1024 x 1024 bitmap that we'll use to create all of the OpenGL texture objects in this test.
        val colors = IntArray(1024 * 1024)
        Arrays.fill(colors, -0xff0100)
        bitmap = Bitmap.createBitmap(colors, 1024, 1024, Bitmap.Config.ARGB_8888)

    }

    protected fun addImage() {
        // Create an image source with a unique factory instance. This pattern is used in order to force World Wind to
        // allocate a new OpenGL texture object for each surface image from a single bitmap instance.
        val imageSource = ImageSource.fromBitmapFactory(object : ImageSource.BitmapFactory {
            override fun createBitmap(): Bitmap? {
                return bitmap
            }
        })
        // Add the surface image to this test's layer.
        layer.addRenderable(
            SurfaceImage(
                Sector(sector),
                imageSource
            )
        )
        getWorldWindow().requestRedraw()
        // Advance to the next surface image's location.
        if (sector.maxLongitude < firstSector.minLongitude + firstSector.deltaLongitude() * 20) {
            sector[sector.minLatitude, sector.minLongitude + sector.deltaLongitude() + 0.1, sector.deltaLatitude()] =
                sector.deltaLongitude()
        } else {
            sector[sector.minLatitude + sector.deltaLatitude() + 0.1, firstSector.minLongitude, sector.deltaLatitude()] =
                sector.deltaLongitude()
        }
    }

    protected fun printMemoryMetrics() {
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        val cache= getWorldWindow().renderResourceCache
        am.getMemoryInfo(mi)
        Logger.log(
            Logger.INFO, String.format(
                Locale.US,
                "totalMem=%,.0fKB availMem=%,.0fKB cacheCapacity=%,.0fKB cacheUsedCapacity=%,.0fKB",
                mi.totalMem / 1024.0,
                mi.availMem / 1024.0,
                cache?.capacity?.toDouble() ?: 0.0/ 1024.0,
                cache?.usedCapacity?.toDouble() ?: 0.0/ 1024.0
            )
        )
    }

    override fun onPause() {
        super.onPause()
        // Stop adding images when this Activity is paused.
        handler.removeMessages(ADD_IMAGE)
    }

    override fun onResume() {
        super.onResume()
        // Add images to the World Window at a regular interval.
        handler.sendEmptyMessageDelayed(
            ADD_IMAGE,
            ADD_IMAGE_INTERVAL.toLong()
        )
    }
}