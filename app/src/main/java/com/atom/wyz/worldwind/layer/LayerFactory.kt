package com.atom.wyz.worldwind.layer

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.atom.wyz.worldwind.WorldWind
import com.atom.wyz.worldwind.geom.Sector
import com.atom.wyz.worldwind.ogc.WmsCapabilities
import com.atom.wyz.worldwind.ogc.WmsTileFactory
import com.atom.wyz.worldwind.util.LevelSet
import com.atom.wyz.worldwind.util.Logger
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.util.concurrent.RejectedExecutionException

open class LayerFactory() {
    interface Callback {
        fun creationSucceeded(factory: LayerFactory, layer: Layer)
        fun creationFailed(
            factory: LayerFactory,
            layer: Layer,
            ex: Throwable?
        )
    }

    protected var mainLoopHandler = Handler(Looper.getMainLooper())

    fun createFromGeoPackage(
        pathName: String,
        callback: Callback
    ): Layer {
        val layer = RenderableLayer()
        layer.pickEnabled = (false)
        val task = GeoPackageAsyncTask(
                this,
                pathName,
                layer,
                callback
            )
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }
        return layer
    }

    fun createFromWms(
        serviceAddress: String,
        layerNames: String,
        callback: Callback
    ): Layer {
        val layer = RenderableLayer()
        layer.pickEnabled = (false)
        val task = WmsAsyncTask(this, serviceAddress, layerNames, layer, callback)
        try {
            WorldWind.taskService.execute(task)
        } catch (logged: RejectedExecutionException) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged)
        }

        return layer
    }

    private fun createGeoPackageLayerAsync(
        pathName: String?,
        layer: Layer?,
        callback: Callback?
    ) {

    }

    private fun createWmsLayerAsync(
        serviceAddress: String,
        layerNames: String,
        layer: Layer,
        callback: Callback
    ) {
        // Retrieve and parse the WMS capabilities at the specified service address, looking for the named layers
        // specified by the comma-delimited layerNames
        val serviceUri = Uri.parse(serviceAddress).buildUpon()
            .appendQueryParameter("REQUEST", "GetCapabilities")
            .appendQueryParameter("SERVICE", "WMS")
            .appendQueryParameter("VERSION", "1.3.0")
            .build()

        val conn = URL(serviceUri.toString()).openConnection()
        conn.connectTimeout = 3000
        conn.readTimeout = 30000
        val inputStream: InputStream = BufferedInputStream(conn.getInputStream())

        // Parse and read capabilities document
        val wmsCapabilities = WmsCapabilities.getCapabilities(inputStream)

        // Establish Version
        val version = wmsCapabilities.getVersion()

        // TODO work with multiple layer names
        val wmsLayerCapabilities = wmsCapabilities.getLayerByName(layerNames)
            ?: throw IllegalArgumentException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createWmsLayerAsync",
                    "Provided layer did not match available layers"
                )
            )

        val getCapabilitiesRequestUrl =
            wmsCapabilities.getRequestURL("GetMap", "Get")
                ?: throw IllegalStateException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createWmsLayerAsync",
                        "Unable to resolve GetCapabilities URL"
                    )
                )

        val wmsTileFactory = WmsTileFactory(
            getCapabilitiesRequestUrl,
            version!!,
            layerNames,
            ""
        )

        val coordinateSystems: Set<String> =
            wmsLayerCapabilities.getReferenceSystem() ?: throw RuntimeException(
                Logger.makeMessage(
                    "LayerFactory",
                    "createWmsLayerAsync",
                    "Coordinate systems not compatible"
                )
            )
        when {
            coordinateSystems.contains("CRS:84") -> {
                wmsTileFactory.coordinateSystem = ("CRS:84")
            }
            coordinateSystems.contains("EPSG:4326") -> {
                wmsTileFactory.coordinateSystem = ("EPSG:4326")
            }
            else -> {
                throw RuntimeException(
                    Logger.makeMessage(
                        "LayerFactory",
                        "createWmsLayerAsync",
                        "Coordinate systems not compatible"
                    )
                )
            }
        }

        val imageFormats =
            wmsCapabilities.getImageFormats()
        if (imageFormats!!.contains("image/png")) {
            wmsTileFactory.imageFormat = ("image/png")
        } else {
            wmsTileFactory.imageFormat = (imageFormats.iterator().next())
        }

        var sector = wmsLayerCapabilities.getGeographicBoundingBox()
        if (sector == null) {
            sector = Sector().setFullSphere()
        }

        val levels = Math.max(1, wmsLayerCapabilities.getNumberOfLevels(512))

        val tiledSurfaceImage = TiledSurfaceImage()
        tiledSurfaceImage.tileFactory = (wmsTileFactory)
        val levelSet = LevelSet(sector, 90.0, levels, 512, 512)
        tiledSurfaceImage.levelSet = (levelSet)

        mainLoopHandler.post {
            val renderableLayer = layer as RenderableLayer
            renderableLayer.addRenderable(tiledSurfaceImage)
            callback.creationSucceeded(this@LayerFactory, layer)
        }
    }

    protected class GeoPackageAsyncTask(
        private var factory: LayerFactory,
        private var pathName: String,
        private var layer: Layer,
        private var callback: Callback
    ) : Runnable {
        override fun run() {
            try {
                factory.createGeoPackageLayerAsync(pathName, layer, callback)
            } catch (ex: Throwable) {
                callback.creationFailed(factory, layer, ex)
            }
        }
    }

    protected class WmsAsyncTask(
        private val factory: LayerFactory,
        private val serviceAddress: String,
        private val layerNames: String,
        private val layer: Layer,
        private val callback: Callback
    ) :
        Runnable {
        override fun run() {
            try {
                factory.createWmsLayerAsync(
                    serviceAddress,
                    layerNames,
                    layer,
                    callback
                )
            } catch (ex: Throwable) {
                callback.creationFailed(factory, layer, ex)
            }
        }
    }
}